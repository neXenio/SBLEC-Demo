package com.nexenio.sblecdemo;

import android.app.Application;

import com.nexenio.sblec.Sblec;
import com.nexenio.sblec.receiver.PayloadReceiver;
import com.nexenio.sblec.sender.PayloadSender;

import timber.log.Timber;

public class DemoApplication extends Application {

    private static final int COMPANY_ID = Sblec.COMPANY_ID_UNASSIGNED;

    private PayloadSender payloadSender;

    private PayloadReceiver payloadReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        Sblec sblec = Sblec.getInstance();
        payloadSender = sblec.getOrCreatePayloadSender(this, COMPANY_ID);
        payloadReceiver = sblec.getOrCreatePayloadReceiver(this, COMPANY_ID);
    }

    public PayloadSender getPayloadSender() {
        return payloadSender;
    }

    public PayloadReceiver getPayloadReceiver() {
        return payloadReceiver;
    }

}
