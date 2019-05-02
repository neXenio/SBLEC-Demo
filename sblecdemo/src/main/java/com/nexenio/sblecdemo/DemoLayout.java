package com.nexenio.sblecdemo;

import com.google.android.material.snackbar.Snackbar;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexenio.sblec.Sblec;
import com.nexenio.sblec.receiver.ReceiverPayload;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class DemoLayout extends RelativeLayout implements DemoView<DemoPresenter> {

    @NonNull
    protected DemoPresenter presenter;

    private AppCompatImageView iconImageView;
    private RelativeLayout backgroundRelativeLayout;
    private TextView titleTextView;
    private TextView subTitleTextView;

    @Nullable
    private Snackbar missingPermissionsErrorSnackbar;

    @Nullable
    private Snackbar bluetoothDisabledErrorSnackbar;

    @Nullable
    private Snackbar locationServicesDisabledSnackbar;

    public DemoLayout(Context context) {
        super(context);
        initialize();
    }

    public DemoLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        presenter = createPresenter();

        inflate(getContext(), R.layout.demo_layout, this);

        iconImageView = findViewById(R.id.iconImageView);
        iconImageView.setOnClickListener(v -> presenter.onIconChangeInvoked());

        backgroundRelativeLayout = findViewById(R.id.backgroundRelativeLayout);
        backgroundRelativeLayout.setOnClickListener(v -> presenter.onColorChangeInvoked());

        titleTextView = findViewById(R.id.titleTextView);
        subTitleTextView = findViewById(R.id.subTitleTextView);

    }

    @Override
    public void onViewStarted() {
        presenter.onViewStarted();
    }

    @Override
    public void onViewStopped() {
        presenter.onViewStopped();
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void visualizeDemoPayload(@NonNull DemoPayloadWrapper demoPayloadWrapper) {
        showIconWithIndex(demoPayloadWrapper.getIconIndex());
        showColorWithIndex(demoPayloadWrapper.getColorIndex());

        ReceiverPayload receiverPayload = demoPayloadWrapper.getReceiverPayload();
        if (receiverPayload != null) {
            // payload has been received from another device
            visualizeDeviceIdHashCode(receiverPayload.getDeviceIdHashCode());
        } else {
            // payload has been created by this device
            visualizeDeviceIdHashCode(Sblec.getDeviceIdHashCode(getContext()));
        }
    }

    private void showIconWithIndex(int iconIndex) {
        iconImageView.setImageDrawable(getIconDrawable(iconIndex));
    }

    private Drawable getIconDrawable(int iconIndex) {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.direction_icons);
        iconIndex = iconIndex % typedArray.length();
        Drawable drawable = typedArray.getDrawable(iconIndex);
        typedArray.recycle();
        return drawable;
    }

    private void showColorWithIndex(int colorIndex) {
        backgroundRelativeLayout.setBackgroundColor(getColorResourceId(colorIndex));
    }

    private int getColorResourceId(int colorIndex) {
        int[] colors = getResources().getIntArray(R.array.material_design_colors);
        colorIndex = colorIndex % colors.length;
        return colors[colorIndex];
    }

    private void visualizeDeviceIdHashCode(int deviceIdHashCode) {
        String readableDeviceName = String.format("0x%08X", deviceIdHashCode);
        titleTextView.setText(getContext().getString(R.string.status_updated_by_device, readableDeviceName));
    }

    @Override
    public void showMissingPermissionsError() {
        missingPermissionsErrorSnackbar = Snackbar.make(backgroundRelativeLayout, R.string.error_missing_permissions, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_grant_permission, v -> presenter.onRequestMissingPermissionsInvoked());
        missingPermissionsErrorSnackbar.show();
    }

    @Override
    public void hideMissingPermissionsError() {
        if (missingPermissionsErrorSnackbar != null) {
            missingPermissionsErrorSnackbar.dismiss();
        }
    }

    @Override
    public void showBluetoothDisabledError() {
        bluetoothDisabledErrorSnackbar = Snackbar.make(backgroundRelativeLayout, R.string.error_bluetooth_disabled, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_enable, v -> presenter.onEnableBluetoothInvoked());
        bluetoothDisabledErrorSnackbar.show();
    }

    @Override
    public void hideBluetoothDisabledError() {
        if (bluetoothDisabledErrorSnackbar != null) {
            bluetoothDisabledErrorSnackbar.dismiss();
        }
    }

    @Override
    public void showLocationServicesDisabledError() {
        locationServicesDisabledSnackbar = Snackbar.make(backgroundRelativeLayout, R.string.error_location_services_disabled, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_enable, v -> presenter.onEnableLocationServicesInvoked());
        locationServicesDisabledSnackbar.show();
    }

    @Override
    public void hideLocationServicesDisabledError() {
        if (locationServicesDisabledSnackbar != null) {
            locationServicesDisabledSnackbar.dismiss();
        }
    }

    @Override
    public void showSendingFailedError(@NonNull Throwable throwable) {
        String error = getContext().getString(R.string.error_sending_failed);
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideSendingFailedError() {
        // meh
    }

    @Override
    public void showReceivingFailedError(@NonNull Throwable throwable) {
        String error = getContext().getString(R.string.error_receiving_failed);
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideReceivingFailedError() {
        // meh
    }

    @NonNull
    @Override
    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        throw new IllegalStateException("Unable to get Activity from context");
    }

    @NonNull
    @Override
    public DemoPresenter createPresenter() {
        return new DemoPresenter<>(this);
    }

}
