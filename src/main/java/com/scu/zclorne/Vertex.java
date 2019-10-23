package com.scu.zclorne;

public class Vertex {
    private String nid;
    private double longitude;
    private double latitude;

    public Vertex() {
    }

    public Vertex(String nid, double longitude, double latitude) {
        this.nid = nid;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Vertex(String nid) {
        this.nid = nid;
    }

    @Override
    public String toString() {
        return nid;
    }

    @Override
    public int hashCode() {
        return nid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vertex){
            return nid.equals(((Vertex) obj).nid);
        }
        return false;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
