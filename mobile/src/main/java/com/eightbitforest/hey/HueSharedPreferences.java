package com.eightbitforest.hey;

import android.content.Context;
import android.content.SharedPreferences;


public class HueSharedPreferences {
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
    private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP = "LastConnedtedIP";
    private SharedPreferences sharedPreferences = null;

    private SharedPreferences.Editor sharedPreferencesEditor = null;

    public HueSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    public String getLastConnectedUsername() {
        return sharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
    }

    public boolean setLastConnectedUsername(String username) {
        sharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
        return sharedPreferencesEditor.commit();
    }

    public String getLastConnectedIp() {
        return sharedPreferences.getString(LAST_CONNECTED_IP, "");
    }

    public boolean setLastConnectedIp(String ip) {
        sharedPreferencesEditor.putString(LAST_CONNECTED_IP, ip);
        return sharedPreferencesEditor.commit();
    }
}