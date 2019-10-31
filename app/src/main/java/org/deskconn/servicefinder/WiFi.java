package org.deskconn.servicefinder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

public class WiFi {

    private List<StateListener> mListeners = new ArrayList<>();
    private ConnectivityManager mCManager;
    private boolean mTracking;
    private String mIP;

    public WiFi(Context context) {
        mCManager = context.getSystemService(ConnectivityManager.class);
    }

    public interface StateListener {
        void onConnect(String ip);
        void onDisconnect();
    }

    public void addStateListener(StateListener listener) {
        mListeners.add(listener);
        if (mIP != null) {
            listener.onConnect(mIP);
        }
    }

    public void removeStateListener(StateListener listener) {
        mListeners.remove(listener);
    }

    private String getNetworkIP(Network network) {
        LinkProperties properties = mCManager.getLinkProperties(network);
        List<LinkAddress> addresses = properties.getLinkAddresses();
        for (LinkAddress linkAddress: addresses) {
            if (linkAddress.getAddress() instanceof Inet4Address) {
                return linkAddress.getAddress().getHostName();
            }
        }
        return null;
    }

    public void trackState() {
        if (isTracking()) {
            return;
        }
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        mCManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                mIP = getNetworkIP(network);
                mListeners.forEach(listener -> listener.onConnect(mIP));
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                mIP = null;
                mListeners.forEach(StateListener::onDisconnect);
            }
        });
        mTracking = true;
    }

    public boolean hasIP() {
        return mIP != null;
    }

    public String getIP() {
        return mIP;
    }

    public boolean isTracking() {
        return mTracking;
    }
}
