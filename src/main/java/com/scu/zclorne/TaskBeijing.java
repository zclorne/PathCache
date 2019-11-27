package com.scu.zclorne;

import com.scu.zclorne.kdtree.KDTree;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class TaskBeijing {
    DataManager dataFileManager = new DataBeiJing();
    Graph<Vertex, Edge> graph;
    HashMap<String, Vertex> vertexs;
    HashMap<String, Edge> edges;
    List<double[]> pois;
    KDTree kdTree;

    /**
     * 构建无向图
     *
     * @throws IOException
     */
    void buildGraph() throws IOException {
        vertexs = dataFileManager.readNode();
        edges = dataFileManager.readEdge(vertexs);
        pois = dataFileManager.readPOI();
        kdTree = new KDTree(2);

        graph = new DefaultUndirectedWeightedGraph<>(Edge.class);
        vertexs.values().forEach((s) -> {
            graph.addVertex(s);
            // 构建kd-tree
            kdTree.insert(new double[]{s.getLongitude(), s.getLatitude()}, s.getNid());
        });

        edges.values().forEach((s) -> {
            graph.addEdge(s.getO(), s.getD(), s);
            graph.setEdgeWeight(s, s.getDistance());
        });
    }

    void countSubPath(int historicalQueries) throws IOException {
        CacheBeijingTest cacheBeijingTest = new CacheBeijingTest(graph, vertexs, edges, pois, kdTree, dataFileManager);
        cacheBeijingTest.countSubShortestPath(historicalQueries,0,"subpath count");
    }
}
