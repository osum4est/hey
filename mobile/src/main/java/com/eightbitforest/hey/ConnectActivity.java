package com.eightbitforest.hey;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final boolean EMULATOR_MODE = false;
    public static final String TAG = "hey";
    private PHHueSDK phHueSDK;
    private HueSharedPreferences preferences;
    private AccessPointListAdapter adapter;

    private boolean lastSearchWasIP;
    private PHSDKListener listener = new PHSDKListener() {
        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {

        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String s) {
            phHueSDK.getNotificationManager().unregisterSDKListener(this);
            phHueSDK.getNotificationManager().registerSDKListener(MainActivity.instance);

            phHueSDK.setSelectedBridge(phBridge);
            phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            preferences.setLastConnectedIp(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
            preferences.setLastConnectedUsername(s);

            finish();
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            phHueSDK.startPushlinkAuthentication(phAccessPoint);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogManager.getInstance().showProgress(ConnectActivity.this,
                            getString(R.string.authenticate_title),
                            getString(R.string.authenticate_summary), 30);
                }
            });
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            if (list != null && list.size() > 0) {
                phHueSDK.getAccessPointsFound().clear();
                phHueSDK.getAccessPointsFound().addAll(list);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endLoading();
                        adapter.updateData(phHueSDK.getAccessPointsFound());
                    }
                });
            }
        }

        private void showError(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogManager.getInstance().closeProgress();
                    ((SwipeRefreshLayout) findViewById(R.id.refresh)).setRefreshing(false);
                    DialogManager.getInstance().showError(ConnectActivity.this, message);
                }
            });
        }

        @Override
        public void onError(int code, String message) {

            if (code == PHHueError.NO_CONNECTION) {
                showError(getString(R.string.error_no_connection));
            } else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
                DialogManager.getInstance().updateProgress(1);
            } else if (code == PHHueError.AUTHENTICATION_FAILED) {
                showError(getString(R.string.error_authentication));
            } else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
                showError(getString(R.string.error_push_link));
            } else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                if (!lastSearchWasIP) {
                    phHueSDK = PHHueSDK.getInstance();
                    PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                    sm.search(false, false, true);
                    lastSearchWasIP = true;
                } else {
                    showError(getString(R.string.error_no_bridges));
                }
            }
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        phHueSDK = MainActivity.phHueSDK;
        preferences = MainActivity.hueSharedPreferences;

        phHueSDK.getNotificationManager().registerSDKListener(listener);

        adapter = new AccessPointListAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());

        ListView accessPointList = (ListView) findViewById(R.id.list_view);
        accessPointList.setOnItemClickListener(this);
        accessPointList.setAdapter(adapter);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.updateData(new ArrayList<PHAccessPoint>());
                bridgeSearch(true);
            }
        });
        bridgeSearch(false);
    }

    public void endLoading() {
        if (DialogManager.getInstance().isProgressOpen())
            DialogManager.getInstance().closeProgress();
        else
            ((SwipeRefreshLayout) findViewById(R.id.refresh)).setRefreshing(false);

    }

    public void bridgeSearch(boolean refreshLayout) {
        if (!refreshLayout)
            DialogManager.getInstance().showProgress(ConnectActivity.this,
                    getString(R.string.search_title),
                    getString(R.string.search_summary));

        if (EMULATOR_MODE) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress("10.222.2.3:80");
            lastAccessPoint.setUsername("newdeveloper");
            phHueSDK.connect(lastAccessPoint);

        } else {
            PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
            sm.search(true, true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PHAccessPoint accessPoint = (PHAccessPoint) adapter.getItem(position);

        PHBridge bridge = phHueSDK.getSelectedBridge();

        if (bridge != null) {
            String connectedIP = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
            if (connectedIP != null) {
                phHueSDK.disconnect(bridge);
            }
        }
        phHueSDK.connect(accessPoint);

    }

    @Override
    public void onBackPressed() {
    }

    public class AccessPointListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<PHAccessPoint> accessPoints;

        public AccessPointListAdapter(Context context, List<PHAccessPoint> accessPoints) {
            inflater = LayoutInflater.from(context);
            this.accessPoints = accessPoints;
        }

        @Override
        public int getCount() {
            return accessPoints.size();
        }

        @Override
        public Object getItem(int position) {
            return accessPoints.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            BridgeListItem item;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);

                item = new BridgeListItem();
                item.bridgeIp = (TextView) convertView.findViewById(R.id.text1);
                item.bridgeMac = (TextView) convertView.findViewById(R.id.text2);

                convertView.setTag(item);
            } else {
                item = (BridgeListItem) convertView.getTag();
            }

            PHAccessPoint accessPoint = accessPoints.get(position);
            item.bridgeIp.setText(getString(R.string.ip) + " " + accessPoint.getIpAddress());
            item.bridgeMac.setText(getString(R.string.mac) + " " + accessPoint.getMacAddress());
            return convertView;
        }

        public void updateData(List<PHAccessPoint> accessPoints) {
            this.accessPoints = accessPoints;
            notifyDataSetChanged();
        }

        class BridgeListItem {
            private TextView bridgeIp;
            private TextView bridgeMac;
        }
    }
}
