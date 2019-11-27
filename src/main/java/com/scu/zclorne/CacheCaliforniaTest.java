package com.scu.zclorne;

import com.scu.zclorne.kdtree.KDTree;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CacheCaliforniaTest {
    Random random = new Random();
    DataManager dataFileManager;
    Graph<Vertex, Edge> graph;
    HashMap<String, Vertex> vertexs;
    HashMap<String, Edge> edges;
    List<double[]> pois;
    KDTree kdTree;
    CacheEPC cache;

    public CacheCaliforniaTest(Graph<Vertex, Edge> graph, HashMap<String, Vertex> vertexs, HashMap<String, Edge> edges, List<double[]> pois, KDTree kdTree, DataManager dataFileManager) {
        this.graph = graph;
        this.vertexs = vertexs;
        this.edges = edges;
        this.pois = pois;
        this.kdTree = kdTree;
        this.dataFileManager = dataFileManager;
    }

    public CacheCaliforniaTest() {
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
            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            //vertexs中随机取出两个点，在无向图中查找
            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get(nearestO.toString()), vertexs.get(nearestD.toString()));
            cache.getShortestPath().add(new ShortestPath(i + "", path));
        }
        //3. 构建缓存
        cache.init();

    }

    /**
     * 执行查询任务
     *
     * @param historicalQueries
     * @param cacheCapacity
     * @param pathQueries
     * @param type
     */
    void executeQuery(int historicalQueries, int cacheCapacity, int pathQueries, String type) {
        int count = 0;
        long taskStartTime = System.currentTimeMillis();
        //4. 执行查询任务
        for (int i = 0; i < pathQueries; i++) {
            //pois中随机取出两个点，使用kd-tree找出无向图中的映射点
//            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
//            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));

            double[] o = pois.get(random.nextInt(pois.size()));
            double[] d = pois.get(random.nextInt(pois.size()));
            if (cache.findShortestPath(new Vertex("", o[0], o[1]), new Vertex("", d[0], d[1]))) {
                count++;
            }

//            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
//            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));
//            if (cache.findShortestPath(vertexs.get(nearestO), vertexs.get(nearestD))) {
//                count++;
//            }
        }
        long taskEndTime = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("#.####%");
        double cacheHitRatio = count * 1.0 / pathQueries;
        String testResult = "historical queries: " + historicalQueries + ", cache capacity: " + cacheCapacity + " , queries: " + pathQueries + ".  cache hit ratio: " + df.format(cacheHitRatio) + " , response time: " + (taskEndTime - taskStartTime) + " ms.";
        System.out.println(testResult);
        dataFileManager.writeData("type:" + type + " --- " + testResult);
    }
}
