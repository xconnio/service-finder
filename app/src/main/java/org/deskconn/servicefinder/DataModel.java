package org.deskconn.servicefinder;

import java.io.Serializable;

public class DataModel implements Serializable {


    private String type;
    private String totalTypes;
    private String hostName;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotalTypes() {
        return totalTypes;
    }

    public void setTotalTypes(String totalTypes) {
        this.totalTypes = totalTypes;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}