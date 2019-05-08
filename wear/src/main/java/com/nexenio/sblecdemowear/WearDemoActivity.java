package com.nexenio.sblecdemowear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.nexenio.sblecdemo.DemoLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class WearDemoActivity extends WearableActivity {

    private DemoLayout demoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_demo);

        Timber.plant(new Timber.DebugTree());
        demoLayout = findViewById(R.id.demoLayout);

        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        demoLayout.onViewStarted();
    }

    @Override
    protected void onStop() {
        demoLayout.onViewStopped();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        demoLayout.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        demoLayout.onActivityResult(requestCode, resultCode, data);
    }

}
