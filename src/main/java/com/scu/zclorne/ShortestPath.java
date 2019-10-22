package com.scu.zclorne;

import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.List;

public class ShortestPath {
    private GraphPath dp;
    private List<Edge> storeEdge = new ArrayList<>();
    private int sharingAbility = 1;
    private List<ShortestPath> canAnswer = new ArrayList<>();
    //sharing ability per edge
    private double sape;

    public double getSape() {
        return sape;
    }

    public void setSape(double sape) {
        this.sape = sape;
    }

    ShortestPath(GraphPath dp) {
        this.dp = dp;
    }

    GraphPath getDp() {
        return dp;
    }

    void setDp(GraphPath dp) {
        this.dp = dp;
    }

    public List<ShortestPath> getCanAnswer() {
        return canAnswer;
    }

    public void setCanAnswer(List<ShortestPath> canAnswer) {
        this.canAnswer = canAnswer;
    }

    void incrementSA() {
        sharingAbility++;
    }

    double calculateSAPE() {
        return sape = sharingAbility * 1.0 / dp.getEdgeList().size();
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
            canAnswer.add(s);
        }
    }


}
