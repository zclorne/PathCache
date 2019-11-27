package com.scu.zclorne;

import com.scu.zclorne.kdtree.KDTree;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CacheBeijingTest {
    Random random = new Random();
    DataManager dataFileManager;
    Graph<Vertex, Edge> graph;
    HashMap<String, Vertex> vertexs;
    HashMap<String, Edge> edges;
    List<double[]> pois;
    KDTree kdTree;
    CacheEPC cache;

    public CacheBeijingTest(Graph<Vertex, Edge> graph, HashMap<String, Vertex> vertexs, HashMap<String, Edge> edges, List<double[]> pois, KDTree kdTree, DataManager dataFileManager) {
        this.graph = graph;
        this.vertexs = vertexs;
        this.edges = edges;
        this.pois = pois;
        this.kdTree = kdTree;
        this.dataFileManager = dataFileManager;
    }

    public CacheBeijingTest() {
    }

    /**
     * 计算覆盖子路径
     */
    void countSubShortestPath(int historicalQueries, int cacheCapacity, String type) throws IOException {
        //1. 无向图
//        buildGraph();
        //2. 构建历史路径查询记录
        cache = new CacheEPC(cacheCapacity);
        for (int i = 0; i < historicalQueries; i++) {
            double[] coordinate = pois.get(random.nextInt(pois.size()));

            Object nearestO = kdTree.nearest(new double[]{coordinate[0], coordinate[1]});
            Object nearestD = kdTree.nearest(new double[]{coordinate[2], coordinate[3]});
//            Object nearestO = kdTree.nearest(new double[]{0, 0});
//            Object nearestD = kdTree.nearest(new double[]{116, 40});
            //vertexs中随机取出两个点，在无向图中查找
            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get(nearestO.toString()), vertexs.get(nearestD.toString()));
            cache.getShortestPath().add(new ShortestPath(i + "", path));
        }
        //3. 构建缓存
//        cache.init();
        cache.calculateSharingAbility();
        cache.calSubpPer();
    }
}
