package com.github.joaogalli.capstonepluris;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by joaog on 14/01/2017.
 */
public class FirebaseUtils {

    public static String getUidOrGoToLogin(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            context.startActivity(new Intent(context, SignInActivity.class));
            return null;
        } else {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    public static void logout(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            FirebaseAuth.getInstance().signOut();
        }
    }

}
