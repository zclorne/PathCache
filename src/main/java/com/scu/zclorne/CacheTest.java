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

    public static void main(String[] args) throws IOException, InterruptedException {
//        test(10000,500000, 3000,"path queries");
//        cacheTestParallel();
//        cacheCapacityTestParallel();
        countSubShortestPath(10000,500000, 3000,"single test");
    }

    /**
     * 计算覆盖子路径
     */
    private static void countSubShortestPath(int historicalQueries, int cacheCapacity, int pathQueries, String type) throws IOException {
        //1. 无向图
        DataFileManager dataFileManager = new DataFileManager();
//        DataManager dataFileManager = new TestDataFileManager();

        HashMap<String, Vertex> vertexs = dataFileManager.readNode();
        HashMap<String, Edge> edges = dataFileManager.readEdge(vertexs);
        List<double[]> pois = dataFileManager.readPOI();
        KDTree kdTree = new KDTree(2);

        Graph<Vertex, Edge> graph = new DefaultUndirectedWeightedGraph<>(Edge.class);
        vertexs.values().forEach((s) -> {
            graph.addVertex(s);
            // 构建kd-tree
            kdTree.insert(new double[]{s.getLongitude(), s.getLatitude()}, s.getNid());
        });

        edges.values().forEach((s) -> {
            graph.addEdge(s.getO(), s.getD(), s);
            graph.setEdgeWeight(s, s.getDistance());
        });
//        graph.addEdge(vertexs.get("99"),vertexs.get("82"),new Edge("300",vertexs.get("99"),vertexs.get("82")));
//        graph.addEdge(vertexs.get("99"),vertexs.get("59"),new Edge("301",vertexs.get("99"),vertexs.get("59")));
//        graph.addEdge(vertexs.get("99"),vertexs.get("8"),new Edge("302",vertexs.get("99"),vertexs.get("8")));
//        graph.addEdge(vertexs.get("99"),vertexs.get("200"),new Edge("303",vertexs.get("99"),vertexs.get("200")));

        //        int historicalQueries = 10000;
//        int cacheCapacity = 500000;
//        int pathQueries = 5000;
        Random random = new Random();
        CacheEPC cache = new CacheEPC(cacheCapacity);
        //2. 构建历史路径查询记录
        for (int i = 0; i < historicalQueries; i++) {
            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get(nearestO.toString()), vertexs.get(nearestD.toString()));
            //vertexs中随机取出两个点，在无向图中查找
//            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size())));
            cache.getShortestPath().add(new ShortestPath(i + "", path));
        }

//        cache.calculateSharingAbility();

        //3. 构建缓存
        cache.init();
//        HashSet<ShortestPath> shortests = new HashSet<>();
//        for (ShortestPath shortestPath : cache.getShortestPath()) {
//            shortests.addAll(shortestPath.getCanAnswer());
//        }
//        System.out.println("子路径条数：" + shortests.size());
//        System.out.println("最短路径条数：" + cache.getShortestPath().size());
//        System.out.println(shortests.size() * 1.0 / historicalQueries);
//        System.out.println("缓存的条数：" + cache.cacheShortestSize());
        int count = 0;
        long taskStartTime = System.currentTimeMillis();
        //4. 执行查询任务
        for (int i = 0; i < pathQueries; i++) {
            //pois中随机取出两个点，使用kd-tree找出无向图中的映射点
//            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
//            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));

//            double[] o = pois.get(random.nextInt(pois.size()));
//            double[] d = pois.get(random.nextInt(pois.size()));
//            if (cache.findShortestPath(new Vertex("",o[0],o[1]), new Vertex("",d[0],d[1]))) {
//                count++;
//            }

            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            if (cache.findShortestPath(vertexs.get(nearestO), vertexs.get(nearestD))) {
                count++;
            }
        }
        long taskEndTime = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("#.####%");
        double cacheHitRatio = count * 1.0 / pathQueries;
        String testResult = "historical queries: " + historicalQueries + ", cache capacity: " + cacheCapacity + " , queries: " + pathQueries + ".  cache hit ratio: " + df.format(cacheHitRatio) + " , response time: " + (taskEndTime - taskStartTime) + " ms.";
        System.out.println(testResult);
        dataFileManager.writeData("type:" + type + " --- " + testResult);
    }

    private static void cacheCapacityTestParallel() throws IOException, InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 8, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        DataManager dataFileManager = new DataFileManager();

        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        for (int i = 50000; i <= 2500000; i += 50000) {
            int finalI = i;
            tpe.execute(() -> {
                try {
                    countSubShortestPath(10000, finalI, 3000, "cache capacity");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

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

    private static void cacheTestParallel() throws IOException, InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 8, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        DataFileManager dataFileManager = new DataFileManager();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");

        for (int i = 1000; i <= 5000; i += 500) {
            int finalI = i;
            tpe.execute(() -> {
                try {
//                    test(10000, 500000, finalI, "path queries");
                    countSubShortestPath(10000, 500000, finalI, "path queries");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 150000; i <= 500000; i += 50000) {
            int finalI = i;
            tpe.execute(() -> {
                try {
//                    test(10000, finalI, 3000, "cache capacity");
                    countSubShortestPath(10000, finalI, 3000, "cache capacity");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 5000; i <= 15000; i += 1000) {
            int finalI = i;
            tpe.execute(() -> {
                try {
//                    test(finalI,500000,3000,"historical queries");
                    countSubShortestPath(finalI, 500000, 3000, "historical queries");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
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

    static void cacheTestSerial() throws IOException {
        DataFileManager dataFileManager = new DataFileManager();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        dataFileManager.writeData("----------------------------path queries--------------------------------");
        for (int i = 1000; i <= 5000; i += 500) {
            test(10000, 500000, i, "path queries");
        }
        dataFileManager.writeData("---------------------------cache capacity-------------------------------");
        for (int i = 150000; i <= 500000; i += 50000) {
            test(10000, i, 3000, "cache capacity");
        }
        dataFileManager.writeData("-------------------------historical queries--- -------------------------");
        for (int i = 5000; i <= 15000; i += 1000) {
            test(i, 500000, 3000, "historical queries");
        }
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");
        dataFileManager.writeData("------------------------------------------------------------------------");
    }

    /**
     * 查询测试
     *
     * @param historicalQueries 生成cache的历史查询条数
     * @param cacheCapacity     cache容量
     * @param pathQueries       查询条数
     */
    private static void test(int historicalQueries, int cacheCapacity, int pathQueries, String type) throws IOException {
        //1. 构建无向图、kd-tree

        //数据文件管理
        DataFileManager dataFileManager = new DataFileManager();
        HashMap<String, Vertex> vertexs = dataFileManager.readNode();
        HashMap<String, Edge> edges = dataFileManager.readEdge(vertexs);
        List<double[]> pois = dataFileManager.readPOI();

        KDTree kdTree = new KDTree(2);
        Graph<Vertex, Edge> graph = new DefaultUndirectedWeightedGraph<>(SupplierUtil.createSupplier(Vertex.class), SupplierUtil.createSupplier(Edge.class));
        vertexs.values().forEach((s) -> {
            graph.addVertex(s);
            // 构建kd-tree
            kdTree.insert(new double[]{s.getLongitude(), s.getLatitude()}, s.getNid());
        });

        edges.values().forEach((s) -> {
            graph.addEdge(s.getO(), s.getD(), s);
            graph.setEdgeWeight(s, s.getDistance());
        });

        Random random = new Random();
        CacheEPC cache = new CacheEPC(cacheCapacity);

//        //2. 构建历史路径查询记录
//        for (int i = 0; i < historicalQueries; i++) {
//            //vertexs中随机取出两个点，在无向图中查找
//            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size())));
//            cache.getShortestPath().add(new ShortestPath(path));
//        }

        //2. 构建历史路径查询记录
        for (int i = 0; i < historicalQueries; i++) {
//            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size())));
//            cache.getShortestPath().add(new ShortestPath(path));

            //pois中随机取出两个点，使用kd-tree找出无向图中的映射点
            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));

            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get(nearestO.toString()), vertexs.get(nearestD.toString()));
            cache.getShortestPath().add(new ShortestPath(i + "", path));
        }

        //3. 构建缓存
        cache.init();

        int count = 0;
        long taskStartTime = System.currentTimeMillis();
        //4. 执行查询任务
        for (int i = 0; i < pathQueries; i++) {
            //pois中随机取出两个点，使用kd-tree找出无向图中的映射点
            Object nearestO = kdTree.nearest(pois.get(random.nextInt(pois.size())));
            Object nearestD = kdTree.nearest(pois.get(random.nextInt(pois.size())));

            if (cache.findShortestPath(vertexs.get(nearestO.toString()), vertexs.get(nearestD.toString()))) {
                count++;
            }

//            if (cache.findShortestPath(vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size()))) != null) {
//                count++;
//            }
        }
        long taskEndTime = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("#.####%");
        double cacheHitRatio = count * 1.0 / pathQueries;
        String testResult = "historical queries: " + historicalQueries + ", cache capacity: " + cacheCapacity + " , queries: " + pathQueries + ".  cache hit ratio: " + df.format(cacheHitRatio) + " , response time: " + (taskEndTime - taskStartTime) + " ms.";
        dataFileManager.writeData("type:" + type + " --- " + testResult);
//        System.out.println(testResult);
    }
}
