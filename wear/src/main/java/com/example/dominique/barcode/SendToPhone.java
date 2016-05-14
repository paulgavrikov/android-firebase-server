package com.example.dominique.barcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SendToPhone extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleClient;
    private TextView text;
    private static final String responseName = "0";
    private static final int responseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_to_phone);

        text = (TextView) findViewById(R.id.connectText);

        // Build a new GoogleApiClient for the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        text.setText("Connecting ...");
        googleClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        text.setText("Connected");
        String message = "Gesture recognized";
        new SendToDataLayerThread("/path", message).start();
    }

    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int errorCode) {
        text.setText("Connection suspendened. Code: " + errorCode);
        Intent returnIntent = new Intent();
        returnIntent.putExtra(responseName, WearMainActivity.CONNECTION_FAIL);
        setResult(WearMainActivity.CONNECTION_SUSPEND, returnIntent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        text.setText(connectionResult.getErrorMessage());
        Intent returnIntent = new Intent();
        returnIntent.putExtra(responseName, WearMainActivity.CONNECTION_FAIL);
        setResult(WearMainActivity.CONNECTION_FAIL, returnIntent);
        finish();
    }

    class SendToDataLayerThread extends Thread {

        private String path;
        private String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            final Intent returnIntent = new Intent();
            returnIntent.putExtra(responseName, responseCode);
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            if(nodes.getNodes().isEmpty()) {
                setResult(WearMainActivity.NO_TARGETS, returnIntent);
                finish();
            }
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient,
                        node.getId(), path, message.getBytes()).await();
                if(!result.getStatus().isSuccess()) {
                    setResult(WearMainActivity.NOT_ALL_RECEIVED, returnIntent);
                    finish();
                }
            }
            setResult(WearMainActivity.ALL_RECEIVED, returnIntent);
            finish();
        }
    }
}