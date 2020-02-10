package org.deskconn.servicefinder;

import java.io.Serializable;
import java.util.Map;

public class Service implements Serializable {
    private String mHostname;
    private String mHostIP;
    private int mPort;
    public Map<String, String> mProperties;

    public Service(String hostName, String hostIP, int port, Map<String, String> properties) {
        mHostname = hostName;
        mHostIP = hostIP;
        mPort = port;
        mProperties = properties;
    }

    public String getHostName() {
        return mHostname;
    }

    public String getHostIP() {
        return mHostIP;
    }

    public int getPort() {
        return mPort;
    }

    public String getProperty(String name) {
        return mProperties.getOrDefault(name, null);
    }
}
