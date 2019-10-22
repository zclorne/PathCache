package com.scu.zclorne;

public class Edge {
    private String eid;
    private Vertex o;//origin node
    private Vertex d;//destination node
    private double distance;

    public Edge(String eid) {
        this.eid = eid;
    }

    public Edge() {
    }

    public Edge(String eid, Vertex o, Vertex d, double distance) {
        this.eid = eid;
        this.o = o;
        this.d = d;
        this.distance = distance;
    }

    public Edge(String eid, Vertex o, Vertex d) {
        this.eid = eid;
        this.o = o;
        this.d = d;
    }

    @Override
    public String toString() {
        return "("+o+","+d+")";
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Vertex getO() {
        return o;
    }

    public void setO(Vertex o) {
        this.o = o;
    }

    public Vertex getD() {
        return d;
    }

    public void setD(Vertex d) {
        this.d = d;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }
}
