package org.deskconn.servicefinder;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

public class ServiceFinder implements WiFi.StateListener, ServiceTypeListener, ServiceListener {

    private static final int PENDING_TYPE_NONE = 0;
    private static final int PENDING_TYPE_ALL = 1;
    private static final int PENDING_TYPE_ONE = 2;

    private JmDNS mDNS;
    private List<String> mTypes = new ArrayList<>();
    private WiFi mWiFi;
    private List<ServiceListener> mListeners = new ArrayList<>();
    private int mPendingType;
    private String mQueuedType;
    private ExecutorService mExecutor;
    private WifiManager.MulticastLock mMulticastLock;

    public ServiceFinder(Context context) {
        mExecutor = Executors.newSingleThreadExecutor();
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mMulticastLock = manager.createMulticastLock(getClass().getName());
        mWiFi = new WiFi(context);
        mWiFi.addStateListener(this);
        mWiFi.trackState();
    }

    private void grabMulticastLock() {
        mMulticastLock.setReferenceCounted(true);
        mMulticastLock.acquire();
    }

    private void releaseMulticastLock() {
        if (mMulticastLock.isHeld()) {
            mMulticastLock.release();
        }
    }

    public interface ServiceListener {
        void onFound(String type, Service service);
        void onLost(String type, String name);
    }

    public void addServiceListener(ServiceListener listener) {
        System.out.println(mListeners.size());
        mListeners.add(listener);
    }

    public void removeServiceListener(ServiceListener listener) {
        mListeners.remove(listener);
    }

    public void discoverAll() {
        if (mPendingType != PENDING_TYPE_NONE) {
            return;
        }
        if (mWiFi.hasIP()) {
            discoverAll(mWiFi.getIP());
        } else {
            mPendingType = PENDING_TYPE_ALL;
        }
    }

    private void discoverAll(String ip) {
        mExecutor.submit(() -> {
            try {
                grabMulticastLock();
                mDNS = JmDNS.create(InetAddress.getByName(ip), getClass().getName());
                mDNS.addServiceTypeListener(this);
            } catch (IOException e) {
                releaseMulticastLock();
                e.printStackTrace();
            }
        });
    }

    public void discover(String type) {
        if (mPendingType != PENDING_TYPE_NONE) {
            return;
        }
        if (mWiFi.hasIP()) {
            discover(type, mWiFi.getIP());
        } else {
            mPendingType = PENDING_TYPE_ONE;
            mQueuedType = type;
        }
    }

    public void cleanup() {
        if (mDNS != null) {
            mTypes.forEach(service -> mDNS.removeServiceListener(service, ServiceFinder.this));
            mDNS.removeServiceTypeListener(this);
            mDNS = null;
        }
        releaseMulticastLock();
    }

    private void discover(String type, String ip) {
        mExecutor.submit(() -> {
            try {
                grabMulticastLock();
                mDNS = JmDNS.create(InetAddress.getByName(ip), getClass().getName());
                mDNS.addServiceListener(type, this);
            } catch (IOException e) {
                releaseMulticastLock();
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onConnect(String ip) {
        if (mPendingType == PENDING_TYPE_ONE) {
            discover(mQueuedType, ip);
        } else if (mPendingType == PENDING_TYPE_ALL) {
            discoverAll(ip);
        }
        mPendingType = PENDING_TYPE_NONE;
    }

    @Override
    public void onDisconnect() {
        cleanup();
    }

    @Override
    public void serviceTypeAdded(ServiceEvent event) {
        mDNS.addServiceListener(event.getType(), this);
        mTypes.add(event.getType());
    }

    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent event) {}

    @Override
    public void serviceAdded(ServiceEvent event) {}

    @Override
    public void serviceRemoved(ServiceEvent event) {
        String type = event.getType();
        String name = event.getName();
        Handler handler = new Handler(Looper.getMainLooper());
        for (ServiceListener listener: mListeners) {
            handler.post(() -> listener.onLost(type, name));
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        String type = event.getType();
        ServiceInfo info = event.getInfo();
        Map<String, String> properties = new HashMap<>();
        Enumeration<String> keys = info.getPropertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, info.getPropertyString(key));
        }
        String host = info.getInet4Addresses()[0].getHostAddress();
        Service service = new Service(event.getName(), host, info.getPort(), properties);
        Handler handler = new Handler(Looper.getMainLooper());
        for (ServiceListener listener: mListeners) {
            handler.post(() -> listener.onFound(type, service));
        }
    }
}
