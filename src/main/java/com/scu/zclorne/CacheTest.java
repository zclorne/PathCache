package com.scu.zclorne;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.util.SupplierUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CacheTest {

    public static void main(String[] args) throws IOException {
        cacheTestParallel();
    }

    private static void cacheTestParallel() throws IOException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5,8,5, TimeUnit.SECONDS,new LinkedBlockingQueue<>());

        DataFileManager dataFileManager = new DataFileManager();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        dataFileManager.writeData("------------------------" + sdf.format(new Date()) + "-----------------------------");

        for (int i = 1000; i <= 5000; i += 500) {
            int finalI = i;
            tpe.execute(()->{
                try {
                    test(10000,500000, finalI,"path queries");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 150000; i <= 500000; i += 50000) {
            int finalI = i;
            tpe.execute(()->{
                try {
                    test(10000,finalI, 3000,"cache capacity");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 5000; i <= 15000; i += 1000) {
            int finalI = i;
            tpe.execute(()->{
                try {
                    test(finalI,500000, 3000,"historical queries");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        tpe.shutdown();
        // 线程池执行完毕
        while (true){
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
            test(10000, 500000, i,"path queries");
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
        //1. 构建无向图
        DataFileManager dataFileManager = new DataFileManager();
        HashMap<String, Vertex> vertexs = dataFileManager.readNode();
        HashMap<String, Edge> edges = dataFileManager.readEdge(vertexs);
        Graph<Vertex, Edge> graph = new DefaultUndirectedWeightedGraph<>(SupplierUtil.createSupplier(Vertex.class), SupplierUtil.createSupplier(Edge.class));
        vertexs.values().forEach((s) -> {
            graph.addVertex(s);
        });

        edges.values().forEach((s) -> {
            graph.addEdge(s.getO(), s.getD(), s);
            graph.setEdgeWeight(s, s.getDistance());
        });

        Random random = new Random();
        CacheEPC cache = new CacheEPC(cacheCapacity);

        //2. 构建历史路径查询记录
        for (int i = 0; i < historicalQueries; i++) {
            //vertexs中随机取出两个点，在无向图中查找
            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph, vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size())));
            cache.getShortestPath().add(new ShortestPath(path));
        }

        //3. 构建缓存
        cache.init();

        int count = 0;
        long taskStartTime = System.currentTimeMillis();
        //4. 执行查询任务
        for (int i = 0; i < pathQueries; i++) {
            if (cache.findShortestPath(vertexs.get("" + random.nextInt(vertexs.size())), vertexs.get("" + random.nextInt(vertexs.size()))) != null) {
                count++;
            }
        }
        long taskEndTime = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("#.####%");
        double cacheHitRatio = count * 1.0 / pathQueries;
        String testResult = "historical queries: " + historicalQueries + ", cache capacity: " + cacheCapacity + " , queries: " + pathQueries + ".  cache hit ratio: " + df.format(cacheHitRatio) + " , response time: " + (taskEndTime - taskStartTime) + " ms.";
        dataFileManager.writeData("type:"+type+" --- "+testResult);
//        System.out.println(testResult);
    }

//    private static void init2() throws IOException {
//        //1. 构建无向图
//        DataFileReader dataFileReader = new DataFileReader();
//        HashMap<String, Vertex> vertexs = dataFileReader.readNode();
//        HashMap<String, Edge> edges = dataFileReader.readEdge(vertexs);
////        HashMap<String, Vertex> pois = dataFileReader.readPOI();
//        Graph<Vertex, Edge> graph = new DefaultUndirectedWeightedGraph<>(SupplierUtil.createSupplier(Vertex.class), SupplierUtil.createSupplier(Edge.class));
//        vertexs.values().forEach((s) -> {
//            graph.addVertex(s);
//        });
//
//        edges.values().forEach((s) -> {
//            graph.addEdge(s.getO(), s.getD(), s);
//            graph.setEdgeWeight(s, s.getDistance());
//        });
//
//        Random random = new Random();
//
//        //2. 构建历史路径查询记录
//        for (int i = 0; i < 200; i++) {
//            //bertexs中随机取出两个点，在无向图中查找
//            GraphPath<Vertex, Edge> path = DijkstraShortestPath.findPathBetween(graph,vertexs.get(""+random.nextInt(vertexs.size())),vertexs.get(""+random.nextInt(vertexs.size())));
//            cache.getShortestPath().add(new ShortestPath(path));
//        }
//
//        //3. 构建缓存
//        cache.init();
//
//        int count = 0;
//        //4. 执行查询任务
//        for (int i = 0; i < 200; i++) {
//            if (cache.findShortestPath(vertexs.get(""+random.nextInt(vertexs.size())),vertexs.get(""+random.nextInt(vertexs.size())))!=null) {
//                count++;
//            }
//        }
//        System.out.println(count);
//    }

//    private static void init1() {
//        //测试数据
//        Vertex v1 = new Vertex("a");
//        Vertex v2 = new Vertex("b");
//        Vertex v3 = new Vertex("c");
//        Vertex v4 = new Vertex("d");
//        Vertex v5 = new Vertex("e");
//        Vertex v6 = new Vertex("f");
//
//        //1. 获取边
//        Graph<Vertex, Edge> graph = new DefaultUndirectedWeightedGraph<Vertex, Edge>(Edge.class);
//        graph.addVertex(v1);
//        graph.addVertex(v2);
//        graph.addVertex(v3);
//        graph.addVertex(v4);
//        graph.addVertex(v5);
//        graph.addVertex(v6);
//        graph.addEdge(v1, v2, new Edge("a-b", v1, v2));
//        graph.setEdgeWeight(v1, v2, 6);
//        graph.addEdge(v1, v3, new Edge("a-c", v1, v3));
//        graph.setEdgeWeight(v1, v3, 3);
//        graph.addEdge(v2, v3, new Edge("b-c", v2, v3));
//        graph.setEdgeWeight(v2, v3, 2);
//        graph.addEdge(v2, v4, new Edge("b-d", v2, v4));
//        graph.setEdgeWeight(v2, v4, 5);
//        graph.addEdge(v3, v4, new Edge("c-d", v3, v4));
//        graph.setEdgeWeight(v3, v4, 3);
//        graph.addEdge(v3, v5, new Edge("c-e", v3, v5));
//        graph.setEdgeWeight(v3, v5, 4);
//        graph.addEdge(v4, v5, new Edge("d-e", v4, v5));
//        graph.setEdgeWeight(v4, v5, 2);
//        graph.addEdge(v4, v6, new Edge("d-f", v4, v6));
//        graph.setEdgeWeight(v4, v6, 3);
//        graph.addEdge(v5, v6, new Edge("e-f", v5, v6));
//        graph.setEdgeWeight(v5, v6, 5);
//        DijkstraShortestPath<Vertex, Edge> dijkstraAlg =
//                new DijkstraShortestPath<Vertex, Edge>(graph);
//        GraphPath<Vertex, Edge> path = dijkstraAlg.getPath(v1, v2);
//        System.out.println(path);
////        System.out.println(iPaths.getPath(v6) + "\n");
//        //2. 生成图
//    }
}
