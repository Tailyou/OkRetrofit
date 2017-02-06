package com.hengda.frame.httputil;

import android.os.Bundle;

import com.hengda.frame.httputil.update.CheckCallback;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.frame.httputil.update.CheckUpdateActivity;

public class MainActivity extends CheckUpdateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNewVersion(new CheckCallback() {
            @Override
            public void hasNewVersion(CheckResponse checkResponse) {
                showHasNewVersionDialog(checkResponse);
            }

            @Override
            public void isAlreadyLatestVersion() {
                showVersionInfoDialog();
            }
        });

    }

}
