package com.nexenio.sblecdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.nexenio.sblec.Sblec;
import com.nexenio.sblec.payload.PayloadIdFilter;
import com.nexenio.sblec.receiver.PayloadReceiver;
import com.nexenio.sblec.receiver.ReceiverPayload;
import com.nexenio.sblec.sender.PayloadSender;
import com.nexenio.sblec.sender.SenderPayload;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * The presenter for a {@link DemoView}. It will use the {@link Sblec} library to send and receive
 * payloads (i.e. the {@link DemoPayloadWrapper}).
 */
public class DemoPresenter<View extends DemoView> {

    /**
     * Company identifiers are unique numbers assigned by the Bluetooth SIG to member companies
     * requesting one.
     *
     * This value may be used in the internal and interoperability tests before a Company ID has
     * been assigned. This value shall not be used in shipping end products.
     *
     * @see <a href="https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers">Bluetooth
     *         Specification: Company Identifiers</a>
     */
    private static final int COMPANY_ID = Sblec.COMPANY_ID_UNASSIGNED;

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static final int REQUEST_ENABLE_LOCATION_SERVICES = 3;

    private View view;

    /**
     * Used for sending {@link SenderPayload}s to nearby devices.
     */
    private PayloadSender payloadSender;

    /**
     * Used for receiving {@link ReceiverPayload} from nearby devices.
     */
    private PayloadReceiver payloadReceiver;

    private CompositeDisposable compositeDisposable;
    private Disposable sendDemoPayloadDisposable;
    private Disposable receiveDemoPayloadsDisposable;

    @Nullable
    private DemoPayloadWrapper lastDemoPayloadWrapper;

    public DemoPresenter(View view) {
        this.view = view;
        Sblec sblec = Sblec.getInstance();
        payloadSender = sblec.getOrCreatePayloadSender(view.getContext(), COMPANY_ID);
        payloadReceiver = sblec.getOrCreatePayloadReceiver(view.getContext(), COMPANY_ID);
    }

    public void onViewStarted() {
        Timber.d("onViewStarted() called");
        compositeDisposable = new CompositeDisposable();

        checkPermissions();
        receiveDemoPayloads();
    }

    public void onViewStopped() {
        Timber.d("onViewStopped() called");
        compositeDisposable.dispose();
    }

    public void onRequestMissingPermissionsInvoked() {
        Timber.d("onRequestMissingPermissionsInvoked() called");
        requestMissingPermissions();
    }

    public void onEnableBluetoothInvoked() {
        Timber.d("onEnableBluetoothInvoked() called");
        enableBluetooth();
    }

    public void onEnableLocationServicesInvoked() {
        Timber.d("onEnableLocationServicesInvoked() called");
        enableLocationServices();
    }

    public void onIconChangeInvoked() {
        Timber.d("onIconChangeInvoked() called");
        DemoPayloadWrapper demoPayloadWrapper = createDemoPayloadWrapper();
        demoPayloadWrapper.setIconIndex(getRandomIconIndex());
        sendDemoPayload(demoPayloadWrapper);
    }

    public void onColorChangeInvoked() {
        Timber.d("onColorChangeInvoked() called");
        DemoPayloadWrapper demoPayloadWrapper = createDemoPayloadWrapper();
        demoPayloadWrapper.setColorIndex(getRandomColorIndex());
        sendDemoPayload(demoPayloadWrapper);
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult() called with: requestCode = [%s], permissions = [%s], grantResults = [%s]", requestCode, permissions, grantResults);
        if (requestCode != REQUEST_PERMISSIONS) {
            return;
        }

        boolean allGranted = true;
        for (int grantResult : grantResults) {
            allGranted = allGranted && (grantResult == PackageManager.PERMISSION_GRANTED);
        }

        if (!allGranted) {
            view.showMissingPermissionsError();
        } else {
            view.hideMissingPermissionsError();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Timber.d("onActivityResult() called with: requestCode = [%s], resultCode = [%s], data = [%s]", requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH || requestCode == REQUEST_ENABLE_LOCATION_SERVICES) {
            receiveDemoPayloads();
        }
    }

    /**
     * Will attempt to send the specified {@link DemoPayloadWrapper} to nearby devices.
     *
     * Note that the sending will continue until the {@link #sendDemoPayloadDisposable} gets
     * disposed or an error occurred (e.g. because Bluetooth gets disabled).
     */
    private void sendDemoPayload(@NonNull DemoPayloadWrapper demoPayloadWrapper) {
        Timber.d("sendDemoPayload() called");
        if (sendDemoPayloadDisposable != null && !sendDemoPayloadDisposable.isDisposed()) {
            compositeDisposable.remove(sendDemoPayloadDisposable);
        }

        sendDemoPayloadDisposable = demoPayloadWrapper
                .toSenderPayload()
                .flatMapCompletable(payload -> payloadSender.send(payload))
                .doOnSubscribe(disposable -> {
                    view.hideSendingFailedError();
                    view.visualizeDemoPayload(demoPayloadWrapper);
                    lastDemoPayloadWrapper = demoPayloadWrapper;
                })
                .doOnError(throwable -> view.showSendingFailedError(throwable))
                .subscribe(
                        () -> Timber.i("Demo payload sending completed"),
                        throwable -> {
                            Timber.w(throwable, "Unable to send demo payload");
                            performTroubleshooting();
                        }
                );

        compositeDisposable.add(sendDemoPayloadDisposable);
    }

    /**
     * Will attempt to receive {@link DemoPayloadWrapper}s sent by nearby devices.
     *
     * Note that the receiving will continue until the {@link #receiveDemoPayloadsDisposable} gets
     * disposed or an error occurred (e.g. because Bluetooth gets disabled).
     */
    private void receiveDemoPayloads() {
        Timber.d("receiveDemoPayloads() called");
        if (receiveDemoPayloadsDisposable != null && !receiveDemoPayloadsDisposable.isDisposed()) {
            compositeDisposable.remove(receiveDemoPayloadsDisposable);
        }

        receiveDemoPayloadsDisposable = payloadReceiver.receive()
                .filter(new PayloadIdFilter(DemoPayloadWrapper.ID))
                .map(DemoPayloadWrapper::new)
                .doOnNext(demoPayloadWrapper -> Timber.v("Received demo payload: %s", demoPayloadWrapper))
                .filter(demoPayloadWrapper -> demoPayloadWrapper.getTimestamp() > getLastDemoPayloadUpdateTimestamp())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> view.hideReceivingFailedError())
                .doOnError(throwable -> view.showReceivingFailedError(throwable))
                .subscribe(
                        demoPayloadWrapper -> {
                            Timber.d("Received new demo payload: %s", demoPayloadWrapper);

                            lastDemoPayloadWrapper = demoPayloadWrapper;
                            view.visualizeDemoPayload(demoPayloadWrapper);

                            // no need to send an outdated payload
                            if (sendDemoPayloadDisposable != null && !sendDemoPayloadDisposable.isDisposed()) {
                                compositeDisposable.remove(sendDemoPayloadDisposable);
                            }
                        },
                        throwable -> {
                            Timber.w(throwable, "Unable to receive demo payloads");
                            performTroubleshooting();
                        }
                );

        compositeDisposable.add(receiveDemoPayloadsDisposable);
    }

    private void performTroubleshooting() {
        Timber.d("performTroubleshooting() called");
        checkPermissions();
        checkBluetoothEnabled();
        checkLocationServicesEnabled();
    }

    /*
        Permissions
     */

    private void checkPermissions() {
        Timber.d("checkPermissions() called");
        int bluetoothPermission = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.BLUETOOTH);
        int locationPermission = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (bluetoothPermission != PackageManager.PERMISSION_GRANTED || locationPermission != PackageManager.PERMISSION_GRANTED) {
            view.showMissingPermissionsError();
        } else {
            view.hideMissingPermissionsError();
        }
    }

    private void requestMissingPermissions() {
        Timber.d("requestMissingPermissions() called");
        String[] permissions = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(view.getActivity(), permissions, REQUEST_PERMISSIONS);
    }

    /*
        Bluetooth
     */

    private void checkBluetoothEnabled() {
        Timber.d("checkBluetoothEnabled() called");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            view.showBluetoothDisabledError();
        } else {
            view.hideBluetoothDisabledError();
        }
    }

    private void enableBluetooth() {
        Timber.d("enableBluetooth() called");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        view.getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    }

    /*
        Location Services
     */

    private void checkLocationServicesEnabled() {
        Timber.d("checkLocationServicesEnabled() called");
        boolean enabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
            enabled = locationManager.isLocationEnabled();
        } else {
            ContentResolver contentResolver = view.getContext().getContentResolver();
            int locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            enabled = locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        if (!enabled) {
            view.showLocationServicesDisabledError();
        } else {
            view.hideLocationServicesDisabledError();
        }
    }

    private void enableLocationServices() {
        Timber.d("enableLocationServices() called");
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        view.getActivity().startActivityForResult(intent, REQUEST_ENABLE_LOCATION_SERVICES);
    }

    /*
        Demo Payload
     */

    private DemoPayloadWrapper createDemoPayloadWrapper() {
        if (lastDemoPayloadWrapper != null) {
            return new DemoPayloadWrapper(lastDemoPayloadWrapper);
        } else {
            int iconIndex = getRandomIconIndex();
            int colorIndex = getRandomColorIndex();
            return new DemoPayloadWrapper(iconIndex, colorIndex);
        }
    }

    private int getRandomIconIndex() {
        int previousIndex = lastDemoPayloadWrapper != null ? lastDemoPayloadWrapper.getIconIndex() : 0;
        return getRandomIndex(previousIndex, 100);
    }

    private int getRandomColorIndex() {
        int previousIndex = lastDemoPayloadWrapper != null ? lastDemoPayloadWrapper.getColorIndex() : 0;
        return getRandomIndex(previousIndex, 100);
    }

    private static int getRandomIndex(int previousIndex, int maximumIndex) {
        int randomIndex;
        do {
            randomIndex = (int) Math.round(Math.random() * maximumIndex);
        } while (randomIndex == previousIndex);
        return randomIndex;
    }

    private long getLastDemoPayloadUpdateTimestamp() {
        return lastDemoPayloadWrapper != null ? lastDemoPayloadWrapper.getTimestamp() : 0;
    }

}
