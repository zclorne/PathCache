package com.scu.zclorne;

import org.jgrapht.GraphPath;

import java.util.HashSet;
import java.util.Set;

public class ShortestPath {
    String pathID;
    private GraphPath dp;
    private int sharingAbility = 0;
    private Set<String> canAnswer = new HashSet<>();
    //sharing ability per edge
    private double sape;

    public ShortestPath(String pathID) {
        this.pathID=pathID;
    }

    public double getSape() {
        return sape;
    }

    public void setSape(double sape) {
        this.sape = sape;
    }

    ShortestPath(String pathID, GraphPath dp) {
        this.pathID = pathID;
        this.dp = dp;
    }

    GraphPath getDp() {
        return dp;
    }

    void setDp(GraphPath dp) {
        this.dp = dp;
    }

    public Set<String> getCanAnswer() {
        return canAnswer;
    }

    public void setCanAnswer(Set<String> canAnswer) {
        this.canAnswer = canAnswer;
    }

    void incrementSA() {
        sharingAbility++;
    }

    double calculateSAPE() {
        int edgeSize = dp.getEdgeList().size();
        if (edgeSize == 0) {
            return sape = -1;
        }
        return sape = sharingAbility * 1.0 / edgeSize;
    }

    /**
     * 当前最短路径是否包含了最短路径s
     * s的起点与终点只要均在当前最短路径的点集中即为包含返回true
     */
    boolean isCoverPath(ShortestPath s) {

        return this.dp.getVertexList().contains(s.getDp().getStartVertex())
                && this.dp.getVertexList().contains(s.getDp().getEndVertex());
    }

    /**
     * 分析与其余最短路径的关系
     * 若有包含关系，SA自增，并将s加入可应答路径列表中
     *
     * @param s
     */
    void analysisWithOtherPath(ShortestPath s) {
        if (isCoverPath(s)) {
            incrementSA();
            canAnswer.add(s.getPathID());
        }
    }

    public int getSharingAbility() {
        return sharingAbility;
    }

    @Override
    public int hashCode() {
        return pathID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShortestPath) {
            return ((ShortestPath) obj).pathID==pathID;
        }
        return false;
    }

    public String getPathID() {
        return pathID;
    }

    public void setPathID(String pathID) {
        this.pathID = pathID;
    }
}
