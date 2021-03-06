package org.mindapps.paatupaadava.p2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import org.mindapps.paatupaadava.p2p.IpDiscovery;
import org.mindapps.paatupaadava.utils.NetworkAdapter;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getName();

    private WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private PeerListListener networkAdapter;
    private NetworkInfo networkInfo;
    private IpDiscovery ipDiscovery;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, NetworkAdapter networkAdapter, Activity activity) {
        this.manager = manager;
        this.channel = channel;
        this.networkAdapter = networkAdapter;
        ipDiscovery = new IpDiscovery(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:

                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if (manager != null) {
                    Log.i(TAG, "P2P Change Happened. Requesting peers");
                    manager.requestPeers(channel, networkAdapter);
                }
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                Log.i(TAG, "Wifi P2P Connection changed action. Network info");

                if (manager != null) {
                    this.networkInfo = intent
                            .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected()) {
                        manager.requestConnectionInfo(channel, ipDiscovery);
                    }
                }

                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Log.i(TAG, "Wifi P2P this device changed");
                break;
        }
    }
}
