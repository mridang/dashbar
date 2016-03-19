package com.mridang.dashbar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.util.Log;

import com.google.android.apps.dashclock.ExtensionManager;
import com.google.android.apps.dashclock.Utils;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.android.apps.dashclock.api.host.DashClockHost;
import com.google.android.apps.dashclock.api.host.ExtensionListing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class that handles the showing and updating of notifications by interfacing with
 * Dashclock using the Dashclock Host API.
 * <br />
 * The service is intended to be always running and is automatically started when the device starts.
 */
public class NotificationService extends Service {

    /**
     * Custom binder class used for allowing the preference activity to bind to this service so that it
     * may be configured on the fly
     */
    public class LocalBinder extends Binder {

        public NotificationService getServerInstance() {
            return NotificationService.this;
        }

    }

    private NotificationManager mNotifier;
    private NotificationHost mHost;
    /**
     * The instance of the binder class used by the activity
     */
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        this.mNotifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.mHost = new NotificationHost(this);
    }

    /**
     * Updated the notification data copy and also updates the notification in the notification
     * shade.
     * <br />
     * All the extension data is consumed and used for building the notification. The title of the
     * extension data becomes the notification's title, the body of the extension data becomes the
     * notification's content, the click intent of the extension data becomes the notification's
     * click intent, the icon of the extension data becomes the notification's icon and the status
     * of the extension data becomes the notifications info only if the status does not match the
     * title or the body (to prevent redundant information cluttering up the notification.)
     *
     * @param component the component name of the extension
     * @param data      the data passed from the extension
     */
    @SuppressWarnings("deprecation")
    private void update(ComponentName component, ExtensionData data) {
        if (data != null && data.visible()) {

            int colour = getResources().getColor(R.color.notification_color);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setColor(colour);
            notification.setCategory(NotificationCompat.CATEGORY_SERVICE);
            notification.setPriority(Integer.MIN_VALUE);
            notification.setOnlyAlertOnce(true);
            notification.setOngoing(true);
            notification.setShowWhen(true);

            PendingIntent click = PendingIntent.getActivity(getApplicationContext(), 0, data.clickIntent(), 0);
            Bitmap icon = Utils.loadExtensionIcon(getApplicationContext(), component, data.icon(), null, colour);
            notification.setStyle(new BigTextStyle().bigText(data.expandedBody()));
            notification.setSmallIcon(R.drawable.ic_notification);
            notification.setContentTitle(data.expandedTitle());
            notification.setContentText(data.expandedBody());
            notification.setGroup("dashbar");

            if (data.status() != null && !data.status().equalsIgnoreCase(data.expandedBody())
                    && !data.status().equalsIgnoreCase(data.expandedTitle())) {
                notification.setContentInfo(data.status());
            }
            notification.setContentIntent(click);
            notification.setLargeIcon(icon);
            mNotifier.notify(component.getPackageName(), 1, notification.build());
        }
    }

    /**
     * Starts the service in a sticky mode so that it is always running and is automatically
     * restarted when it is stopped
     *
     * @see Service#onStartCommand(Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Binder method of the service which returns null as there is no need to bind to the service
     * from the activity or anything else
     *
     * @see Service#onBind(Intent)
     */
    @Override
    public IBinder onBind(Intent intReason) {
        return mBinder;
    }

    /**
     * Destroy method that is used to destroy the notification host to stop listening for updates
     * and free up unused resources
     *
     * @see Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        this.mHost.destroy();
    }

    /**
     * Refresh method that is used to inform the notification host to refresh all the extensions as
     * the configuration has changed.
     */
    public void refresh() {
        this.mNotifier.cancelAll();
        this.mHost.onAvailableExtensionsChanged();
    }

    /**
     * Notification host class that interfaces with the Dashclock API to get the installed
     * extensions and monitor changes to the extensions or their data.
     */
    private static class NotificationHost extends DashClockHost {

        private static final String TAG = "NotificationHost";
        private final NotificationService mService;

        /**
         * Host constructor that takes an instance of the notification service which ultimately
         * manages the notifications
         *
         * @param service the notification service
         * @throws SecurityException
         */
        private NotificationHost(NotificationService service) throws SecurityException {
            super(service.getApplicationContext());
            this.mService = service;

        }

        @Override
        protected void onExtensionDataChanged(ComponentName extension) {
            Log.d(TAG, "Extension data changed for " + extension.flattenToString());
            mService.update(extension, getExtensionData(extension));
        }

        @Override
        public void onAvailableExtensionsChanged() {
            Log.d(TAG, "Available extensions has changed. Updating");
            super.onAvailableExtensionsChanged();

            List<ComponentName> active = ExtensionManager.getInstance(mService).getInternalActiveExtensionNames();
            Set<ComponentName> worldReadableExtensions = new HashSet<>();
            for (ExtensionListing info : getAvailableExtensions(!areNonWorldReadableExtensionsVisible())) {
                if (active.contains(info.componentName())) {
                    worldReadableExtensions.add(info.componentName());
                    mService.update(info.componentName(), getExtensionData(info.componentName()));
                }
            }
            listenTo(worldReadableExtensions);
        }
    }
}
