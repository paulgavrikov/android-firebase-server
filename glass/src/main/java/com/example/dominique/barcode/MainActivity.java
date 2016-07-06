package com.example.dominique.barcode;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessaging;
import com.motioncoding.firebaseserver.FirebaseServer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("glass");
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestBarcode();
    }

    private void requestBarcode() {
        new FirebaseServer("AIzaSyCXmt761UPr1z3DvHDY2t9Sfrne4lEnsD4").sendDataToTopic("central", FirebaseServer.stringToMap("cmd", "SCAN"));
    }
}
