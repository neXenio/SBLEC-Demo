package com.nexenio.sblecdemo;

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

    @NonNull
    Context getContext();

    @NonNull
    Presenter createPresenter();

}
