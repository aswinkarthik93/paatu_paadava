package org.mindapps.paatupaadava.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.mindapps.paatupaadava.server.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

import static org.mindapps.paatupaadava.server.Server.PORT;

public class AsyncSendFileTask extends AsyncTask<Void, Integer, Boolean> {

    private final String TAG = this.getClass().getName();

    private final byte[] data;
    private Iterator<String> clientIpPoolIterator;
    private ProgressDialog dialog;

    public AsyncSendFileTask(byte[] data, Context context, Iterator<String> clientIpPoolsIterator) {
        this.data = data;
        clientIpPoolIterator = clientIpPoolsIterator;
        this.dialog = new ProgressDialog(context);
    }

    protected void onPreExecute() {
        this.dialog.setMessage("Sending files to peers");
        Log.i(TAG, "Starting async task");
        this.dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.i(TAG, "Doing in background for peers iterator ");

        while (clientIpPoolIterator.hasNext()) {
            String peer = clientIpPoolIterator.next();
            Log.i(TAG, "Selected peer " + peer);
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            try {
                socket = new Socket(peer, PORT);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(RequestHandler.INCOMING_FILE);
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
                dataOutputStream.flush();

                Log.i(TAG, "Write complete for peer " + peer);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

    }
}