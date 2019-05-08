package com.nexenio.sblecdemoapp;

import android.content.Intent;
import android.os.Bundle;

import com.nexenio.sblecdemo.DemoLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

public class MobileDemoActivity extends AppCompatActivity {

    private DemoLayout demoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(com.nexenio.sblecdemo.R.string.app_name);
        setContentView(R.layout.activity_mobile_demo);

        Timber.plant(new Timber.DebugTree());
        demoLayout = findViewById(R.id.demoLayout);
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
        super.onActivityResult(requestCode, resultCode, data);
        demoLayout.onActivityResult(requestCode, resultCode, data);
    }

}
