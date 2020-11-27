package nitya.androidwebview_full;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class NetworkConnectivity {

    @SuppressLint("StaticFieldLeak")
    private static nitya.androidwebview_full.NetworkConnectivity sharedNetworkConnectivity = null;

    private Activity activity = null;

    private final Handler handler = new Handler();

    private boolean stopRequested = false;
    private boolean monitorStarted = false;

    private static final int NETWORK_CONNECTION_YES = 1;
    private static final int NETWORK_CONNECTION_NO = -1;
    private static final int NETWORK_CONNECTION_UKNOWN = 0;

    private int connected = NETWORK_CONNECTION_UKNOWN;

    public static final int MONITOR_RATE_WHEN_CONNECTED_MS = 5000;
    public static final int MONITOR_RATE_WHEN_DISCONNECTED_MS = 1000;

    private final List<NetworkMonitorListener> networkMonitorListeners = new ArrayList<NetworkMonitorListener>();

    private NetworkConnectivity() {
    }

    public synchronized static nitya.androidwebview_full.NetworkConnectivity sharedNetworkConnectivity() {
        if (sharedNetworkConnectivity == null) {
            sharedNetworkConnectivity = new nitya.androidwebview_full.NetworkConnectivity();
        }

        return sharedNetworkConnectivity;
    }

    public void configure(Activity activity) {
        this.activity = activity;
    }

    public synchronized void startNetworkMonitor() {
        if (this.activity == null) {
            return;
        }

        if (monitorStarted) {
            return;
        }

        stopRequested = false;
        monitorStarted = true;

        (new Thread(new Runnable() {
            @Override
            public void run() {
                doCheckConnection();
            }
        })).start();

    }

    public synchronized void stopNetworkMonitor() {
        stopRequested = true;
        monitorStarted = false;
    }

    public void addNetworkMonitorListener(NetworkMonitorListener l) {
        this.networkMonitorListeners.add(l);
        this.notifyNetworkMonitorListener(l);
    }

    public boolean removeNetworkMonitorListener(NetworkMonitorListener l) {
        return this.networkMonitorListeners.remove(l);
    }

    private void doCheckConnection() {

        Runnable runnable = null;
        if (stopRequested) {
            return;
        }

        final boolean connectedBool = this.isConnected();
        final int _connected = (connectedBool ? NETWORK_CONNECTION_YES
                : NETWORK_CONNECTION_NO);

        if (this.connected != _connected) {

            this.connected = _connected;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyNetworkMonitorListeners();
                }
            });
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                doCheckConnection();
            }
        };

        handler.postDelayed(runnable,
                (connectedBool ? MONITOR_RATE_WHEN_CONNECTED_MS
                        : MONITOR_RATE_WHEN_DISCONNECTED_MS));
    }

    public boolean isConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return netInfo != null && netInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private void notifyNetworkMonitorListener(NetworkMonitorListener l) {
        try {
            if (this.connected == NETWORK_CONNECTION_YES) {
                l.connectionEstablished();
            } else if (this.connected == NETWORK_CONNECTION_NO) {
                l.connectionLost();
            } else {
                l.connectionCheckInProgress();
            }
        } catch (Exception ignored) {
        }
    }

    private void notifyNetworkMonitorListeners() {
        for (NetworkMonitorListener l : this.networkMonitorListeners) {
            this.notifyNetworkMonitorListener(l);
        }
    }

}
