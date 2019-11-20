package com.scu.zclorne;

import com.scu.zclorne.kdtree.KDTree;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Task {
    public static void main(String[] args) throws IOException, InterruptedException {
        Task task = new Task();
        task.buildGraph();
        task.cacheTestParallel();
//        task.singleTest();
    }

    DataFileManager dataFileManager = new DataFileManager();
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

    void singleTest() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        CacheTest test = new CacheTest(graph, vertexs, edges, pois, kdTree);
        test.countSubShortestPath(10000, 2500000, "single test");
        test.executeQuery(10000, 2500000, 3000, "single test");
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        dataFileManager.writeData("------------------------------------------------------------------------");
    }

    void cacheTestParallel() throws IOException, InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");

        historyTask(tpe);
        cacheTask(tpe);
        pathTask(tpe);

        tpe.shutdown();
        // 线程池执行完毕
        while (true) {
            TimeUnit.MINUTES.sleep(30);
            if (tpe.isTerminated()) {
                break;
            }
        }
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        dataFileManager.writeData("------------------------------------------------------------------------");
    }

    private void historyTask(ThreadPoolExecutor tpe) {
        for (int i = 5000; i <= 15000; i += 1000) {
            int finalI = i;
            tpe.execute(() -> {
                try {
                    CacheTest test = new CacheTest(graph, vertexs, edges, pois, kdTree);
                    test.countSubShortestPath(finalI, 2500000, "historical queries");
                    test.executeQuery(finalI, 2500000, 3000, "historical queries");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void cacheTask(ThreadPoolExecutor tpe) {
        for (int i = 500000; i <= 4000000; i += 500000) {
            int finalI = i;
            tpe.execute(() -> {
                try {
                    CacheTest test = new CacheTest(graph, vertexs, edges, pois, kdTree);
                    test.countSubShortestPath(10000, finalI, "cache capacity");
                    test.executeQuery(10000, finalI, 3000, "cache capacity");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void pathTask(ThreadPoolExecutor tpe) throws IOException {
        CacheTest test = new CacheTest(graph, vertexs, edges, pois, kdTree);
        test.countSubShortestPath(10000, 2500000, "path queries");
        for (int i = 1000; i <= 5000; i += 500) {
            int finalI = i;
            tpe.execute(() -> {
                // 执行查询任务
                test.executeQuery(10000, 2500000, finalI, "path queries");
            });
        }
    }
}
