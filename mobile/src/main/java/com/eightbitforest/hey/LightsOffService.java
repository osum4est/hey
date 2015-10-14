package com.eightbitforest.hey;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;

public class LightsOffService extends Service implements PHSDKListener {

    PHHueSDK phHueSDK;
    boolean on;
    SharedPreferences preferences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (intent != null) {
            on = false;
            if (intent.getExtras() != null)
                if (intent.getExtras().getBoolean("on", false))
                    on = true;

            bridgeConnect();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void turnOffLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        PHLightState lightState = new PHLightState();

        if (on)
            lightState.setOn(true);
        else
            lightState.setOn(false);

        for (PHLight light : bridge.getResourceCache().getAllLights()) {
            String id = light.getUniqueId();
            if (id == null || preferences.getBoolean(light.getUniqueId(), true))
                bridge.updateLightState(light, lightState);
        }
    }

    public void playSound() {
        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.who);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
            }
        });


    }

    public void bridgeConnect() {
        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(this);

        HueSharedPreferences preferences = new HueSharedPreferences(this);
        String lastIpAddress = preferences.getLastConnectedIp();
        String lastUsername = preferences.getLastConnectedUsername();

        if (lastIpAddress != null && !lastIpAddress.equals("") &&
                lastUsername != null && !lastUsername.equals("")) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
                phHueSDK.connect(lastAccessPoint);
            } else {
                connectionSuccess();
            }
        } else {
            connectionFailure();
        }
    }

    public void toast(final String text) {

        Log.i(ConnectActivity.TAG, text);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connectionFailure() {
        //toast("Connection to hue failed. Please open the app and make sure you're connected properly");
    }

    public void connectionSuccess() {
        if (on)
            toast(getString(R.string.lights_on));
        else
            toast(getString(R.string.lights_off));

        if (preferences.getBoolean("sound", true))
            playSound();

        turnOffLights();
    }

    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

    }

    @Override
    public void onBridgeConnected(PHBridge phBridge, String s) {
        phHueSDK.setSelectedBridge(phBridge);
        connectionSuccess();
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        connectionFailure();
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {

    }

    @Override
    public void onError(int i, String s) {
        connectionFailure();
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
}
