package com.teamagly.friendizer.utils;

import android.os.Bundle;

import com.facebook.android.*;
import com.facebook.android.Facebook.DialogListener;

/**
 * Skeleton base class for RequestListeners, providing default error handling. Applications should handle these error conditions.
 */
public class BaseDialogListener implements DialogListener {

    @Override
    public void onFacebookError(FacebookError e) {
    	e.printStackTrace();
    }

    @Override
    public void onError(DialogError e) {
    	e.printStackTrace();
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onComplete(Bundle values) {
    }
}