/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mridang.dashbar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.apps.dashclock.configuration.ConfigureExtensionsFragment;

import net.nurik.roman.dashclock.R;

/**
 * The primary widget configuration activity. Serves as an interstitial when adding the widget, and
 * shows when pressing the settings button in the widget.
 */
public class ConfigurationActivity extends AppCompatActivity implements OnClickListener {

    private boolean isBounded;
    private NotificationService backgroundService;

    /**
     * Connection class between the activity and the service to be able to invoke service methods
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            isBounded = false;
            backgroundService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            isBounded = true;
            backgroundService = ((NotificationService.LocalBinder) service).getServerInstance();
        }
    };

    /**
     * Handles the creation of the activity by setting up the default fragment into focus, tinting
     * the status bar and starting the background service if not started
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(getApplicationContext(), NotificationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        setContentView(R.layout.activity_configure);

        Toolbar appBar = (Toolbar) findViewById(R.id.app_bar);
        appBar.setNavigationIcon(R.drawable.ic_action_done);
        appBar.setNavigationContentDescription(R.string.done);
        appBar.setNavigationOnClickListener(this);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.theme_primary_dark));

        getFragmentManager().beginTransaction()
                .replace(R.id.content_container, new ConfigureExtensionsFragment())
                .commitAllowingStateLoss();
    }

    /**
     * Click event invoked when the back button is pressed and caused the activity to close and exit
     * @see OnClickListener#onClick(View)
     */
    @Override
    public void onClick(View v) {
        finish();
    }

    /**
     * Stop method invoked when the activity is stopped and unbinds the current activity from the
     * background service.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (isBounded) {
            unbindService(mConnection);
            isBounded = false;
        }
    }

    /**
     * Pause method which when the back button is pressed, the background service is made to refresh
     * the extensions
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isBounded) {
            backgroundService.refresh();
        }
    }
}
