package com.eightbitforest.hey;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearableLightsOffService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        startService(new Intent(this, LightsOffService.class));
    }
}
