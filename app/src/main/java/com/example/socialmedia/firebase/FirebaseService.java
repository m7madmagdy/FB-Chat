package com.example.socialmedia.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseService extends FirebaseMessagingService {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if (user != null) {
            updateToken(token);
        }
        Log.e("TOKEN", token);

    }

    private void updateToken(String tokenRefresh) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        dbRef.child(user.getUid()).setValue(token);
    }
}
