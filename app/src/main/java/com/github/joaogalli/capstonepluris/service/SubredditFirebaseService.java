package com.github.joaogalli.capstonepluris.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.SignInActivity;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joaog on 21/01/2017.
 */

public class SubredditFirebaseService {

    private static final String TAG = SubredditFirebaseService.class.getSimpleName();
    private DatabaseReference mDatabase;

    private Context mContext;

    public SubredditFirebaseService(Context mContext) {
        this.mContext = mContext;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void registerForUidSubreddits(ValueEventListener valueEventListener) {
        String uid = FirebaseUtils.getUidOrGoToLogin(mContext);
        DatabaseReference child = mDatabase.child("subreddits").child(uid);
//        child.addListenerForSingleValueEvent(this);
        child.addValueEventListener(valueEventListener);

    }

    private String getUid() {
        return FirebaseUtils.getUidOrGoToLogin(mContext);
    }

    public void exists(Subreddit subreddit, ValueEventListener valueEventListener) {
        String uid = getUid();
        Log.i(TAG, "subreddit = " + subreddit);
        mDatabase.child("subreddits").child(uid)
                .orderByChild("displayName")
                .equalTo(subreddit.getDisplayName())
                .addListenerForSingleValueEvent(valueEventListener);
    }

    public void add(Subreddit subreddit) {
        String uid = getUid();

        String key = mDatabase.child("subreddits").child(uid).push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();

        if (uid != null) {
            childUpdates.put("/subreddits/" + uid + "/" + key, subreddit.toMap());
            mDatabase.updateChildren(childUpdates);
        } else {
            Toast.makeText(mContext, R.string.your_login_expired, Toast.LENGTH_SHORT).show();
            FirebaseUtils.logout(mContext);
            mContext.startActivity(new Intent(mContext, SignInActivity.class));
        }
    }

    public void remove(DataSnapshot dataSnapshot, DatabaseReference.CompletionListener completionListener) {
        if (dataSnapshot.getKey() != null) {
            DatabaseReference subreddits = mDatabase.child("subreddits").child(getUid()).child(dataSnapshot.getKey());
            if (completionListener == null)
                subreddits.removeValue();
            else
                subreddits.removeValue(completionListener);
        }
    }

}
