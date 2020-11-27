package nitya.androidwebview_full;

public interface NetworkMonitorListener {

    void connectionEstablished();
    void connectionLost();
    void connectionCheckInProgress();
}
