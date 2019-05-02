package com.nexenio.sblecdemo;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

public interface DemoView<Presenter extends DemoPresenter> {

    void visualizeDemoPayload(@NonNull DemoPayloadWrapper demoPayloadWrapper);

    void showMissingPermissionsError();

    void hideMissingPermissionsError();

    void showBluetoothDisabledError();

    void hideBluetoothDisabledError();

    void showLocationServicesDisabledError();

    void hideLocationServicesDisabledError();

    void showSendingFailedError(@NonNull Throwable throwable);

    void hideSendingFailedError();

    void showReceivingFailedError(@NonNull Throwable throwable);

    void hideReceivingFailedError();

    void onViewStarted();

    void onViewStopped();

    @NonNull
    Context getContext();

    @NonNull
    Activity getActivity();

    @NonNull
    Presenter createPresenter();

}
