package org.mindapps.paatupaadava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.mindapps.paatupaadava.server.Server;
import org.mindapps.paatupaadava.utils.MP3Player;
import org.mindapps.paatupaadava.utils.NetworkAdapter;

import java.io.File;

import static android.content.Intent.ACTION_PICK;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


public class MainActivity extends Activity implements ChannelListener {

    // Constants
    private final IntentFilter intentFilter = new IntentFilter();
    private final int SELECT_SONG_REQUEST_CODE = 10;

    // Related to p2p connections
    private WifiBroadcastReceiver receiver;
    private WifiP2pManager manager;

    //File downloaded
    private File downloadedSong;


    private Channel channel;
    //Utils
    private MP3Player player;

    private NetworkAdapter networkAdapter;
    //Logging
    private final String TAG = this.getClass().getName();
    private Server sever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sockets wont work without these
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        player = new MP3Player();
        networkAdapter = new NetworkAdapter();
        sever = new Server(this);
        sever.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(receiver == null)
            receiver = new WifiBroadcastReceiver(manager, channel, networkAdapter);

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChannelDisconnected() {

    }

    public void selectSong(View view) {

        Log.i(TAG, "Song selection in progress");

        Intent intent = new Intent(ACTION_PICK, EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_SONG_REQUEST_CODE);
    }

    public void schedulePlay(View view) {

    }


    //activityResultForSelectSong

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "On activity result for request code " + requestCode + " with status " + resultCode + " and data " + data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_SONG_REQUEST_CODE:
                    networkAdapter.sendFileToPeers(MainActivity.this, data.getData());
                    break;
            }
        }

    }


    public void scheduleStop(View view) {
        Log.i(TAG, "scheduling stop");
        player.stopSongIfAnyPlaying();
    }

    public void setDownloadedSong(File downloadedSong) {
        this.downloadedSong = downloadedSong;
    }

    public void discoverPeers(View view) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Peer discovery successful. List will refresh in a moment");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.i(TAG, "Failed to discover. Try again.");
            }
        });
    }
}
