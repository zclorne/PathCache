package com.scu.zclorne;

import com.scu.zclorne.kdtree.KDTree;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CacheTest {
    Random random = new Random();
    DataFileManager dataFileManager = new DataFileManager();
    Graph<Vertex, Edge> graph;
    HashMap<String, Vertex> vertexs;
    HashMap<String, Edge> edges;
    List<double[]> pois;
    KDTree kdTree;
    CacheEPC cache;

    public CacheTest(Graph<Vertex, Edge> graph, HashMap<String, Vertex> vertexs, HashMap<String, Edge> edges, List<double[]> pois, KDTree kdTree) {
        this.graph = graph;
        this.vertexs = vertexs;
        this.edges = edges;
        this.pois = pois;
        this.kdTree = kdTree;
    }

    public CacheTest() {
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
     * 构建无向图
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

//    void cacheCapacityTestParallel() throws IOException, InterruptedException {
//        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 8, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//
//        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
//        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
//        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
//        for (int i = 500000; i <= 2500000; i += 250000) {
//            int finalI = i;
//            tpe.execute(() -> {
//                try {
//                    countSubShortestPath(10000, finalI, 3000, "cache capacity");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        tpe.shutdown();
//        // 线程池执行完毕
//        while (true) {
//            TimeUnit.MINUTES.sleep(30);
//            if (tpe.isTerminated()) {
//                break;
//            }
//        }
//        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
//        dataFileManager.writeData("------------------------------------------------------------------------");
//    }
//
//    void cacheTestParallel() throws IOException, InterruptedException {
//        ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//
//        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
//        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
//        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
//
//        for (int i = 1000; i <= 5000; i += 500) {
//            int finalI = i;
//            tpe.execute(() -> {
//                try {
//                    countSubShortestPath(10000, 2500000, finalI, "path queries");
//                    // 执行查询任务
//                    executeQuery(10000, 2500000, finalI, "path queries");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        for (int i = 150000; i <= 500000; i += 50000) {
//            int finalI = i;
//            tpe.execute(() -> {
//                try {
//                    countSubShortestPath(10000, finalI, 3000, "cache capacity");
//                    executeQuery(10000, finalI, 3000, "cache capacity");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        for (int i = 500000; i <= 4000000; i += 500000) {
//            int finalI = i;
//            tpe.execute(() -> {
//                try {
//                    countSubShortestPath(finalI, 2500000, 3000, "historical queries");
//                    executeQuery(finalI, 2500000, 3000, "historical queries");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//        tpe.shutdown();
//        // 线程池执行完毕
//        while (true) {
//            TimeUnit.MINUTES.sleep(30);
//            if (tpe.isTerminated()) {
//                break;
//            }
//        }
//        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
//        dataFileManager.writeData("------------------------------------------------------------------------");
//    }
}
