package com.eightbitforest.hey;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PHSDKListener {

    public static PHHueSDK phHueSDK;
    public static PHBridge phBridge;
    public static HueSharedPreferences hueSharedPreferences;

    public static MainActivity instance;
    SettingsFragment settingsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        phHueSDK = PHHueSDK.create();
        phHueSDK.setAppName("com.eightbitforest.hey");
        phHueSDK.setDeviceName(Build.MODEL);
        phHueSDK.getNotificationManager().registerSDKListener(this);
        hueSharedPreferences = new HueSharedPreferences(this);

        tryQuickConnect();
        refreshSettings();
    }

    public void openBridgeConnect() {
        DialogManager.getInstance().closeProgress();
        phHueSDK.getNotificationManager().unregisterSDKListener(this);
        startActivity(new Intent(this, ConnectActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSettings();
    }

    public void refreshSettings() {
        ArrayList<String> lights = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        phBridge = phHueSDK.getSelectedBridge();
        if (phBridge != null) {
            for (PHLight light : phBridge.getResourceCache().getAllLights()) {
                lights.add(light.getName());
                ids.add(light.getUniqueId());
            }
        }

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("lights", lights);
            bundle.putStringArrayList("ids", ids);
            settingsFragment.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, settingsFragment)
                    .commit();
        } else {
            settingsFragment.refresh(lights, ids);
        }
    }

    public void tryQuickConnect() {
        HueSharedPreferences preferences = new HueSharedPreferences(this);
        String lastIpAddress = preferences.getLastConnectedIp();
        String lastUsername = preferences.getLastConnectedUsername();

        if (lastIpAddress != null && !lastIpAddress.equals("") &&
                lastUsername != null && !lastUsername.equals("")) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
                DialogManager.getInstance().showProgress(this,
                        getString(R.string.connecting_title),
                        getString(R.string.connecting_summary));
                phHueSDK.connect(lastAccessPoint);
            }
        } else
            openBridgeConnect();
    }

    public void quickConnectError() {
        final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                openBridgeConnect();
            }
        };


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogManager.getInstance().showError(MainActivity.this,
                        getString(R.string.error_last_known),
                        clickListener);
            }
        });

    }

    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

    }

    @Override
    public void onBridgeConnected(PHBridge phBridge, String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogManager.getInstance().closeProgress();
                refreshSettings();
            }
        });
        phHueSDK.setSelectedBridge(phBridge);
        phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        quickConnectError();
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {

    }

    @Override
    public void onError(int i, String s) {
        Log.e(ConnectActivity.TAG, s);
        if (i == PHMessageType.BRIDGE_NOT_FOUND ||
                i == PHHueError.BRIDGE_NOT_RESPONDING)
            quickConnectError();
    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {

    }

    @Override
    public void onConnectionLost(PHAccessPoint phAccessPoint) {

    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {

    }

    @Override
    protected void onDestroy() {
        if (phHueSDK.isHeartbeatEnabled(phBridge))
            phHueSDK.disableAllHeartbeat();
        phHueSDK.disconnect(phBridge);
        phHueSDK.destroySDK();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        refreshSettings();
    }
}
