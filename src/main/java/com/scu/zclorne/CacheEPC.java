package com.scu.zclorne;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

import java.util.*;

public class CacheEPC {
    private int capacity = 500000;
    private int size = 0;
    private HashMap<String, Edge> EII = new HashMap<>();
    private HashMap<String, Set<String>> ENI = new HashMap<>();
    private HashMap<String, Set<String>> EPI = new HashMap<>();
    private List<ShortestPath> shortestPath = new ArrayList<>();//历史路径查询及结果
    private List<ShortestPath> cache = new ArrayList<>();
    private RTree<String, Geometry> tree;

    public CacheEPC(int cacheCapacity) {
        capacity = cacheCapacity;
    }

    public List<ShortestPath> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(List<ShortestPath> shortestPath) {
        this.shortestPath = shortestPath;
    }

    void init() {
        buildCache();
        buildRTree();
    }

    /**
     * 统计SA并计算每个最短路径的sharing ability per edge
     */
    void calculateSharingAbility() {
        ShortestPath cur = null;
        ShortestPath next = null;
        int size = this.shortestPath.size();
        for (int i = 0; i < size; i++) {
            cur = shortestPath.get(i);
            for (int j = i + 1; j < size; j++) {
                next = shortestPath.get(j);
                cur.analysisWithOtherPath(next);
                next.analysisWithOtherPath(cur);
            }
            cur.calculateSAPE();
        }
    }

    /**
     * 构建缓存
     */
    void buildCache() {
        calculateSharingAbility();

        HashSet<String> shortests = new HashSet<>();
        for (ShortestPath shortestPath : shortestPath) {
            shortests.addAll(shortestPath.getCanAnswer());
        }
        System.out.println("子路径条数：" + shortests.size());
        System.out.println("最短路径条数：" + shortestPath.size());
        System.out.println(shortests.size() * 1.0 / 10000);

        //将路径按边数（也就是节点数，节点数=边数+1）排序
//        List<ShortestPath> sortedPath = shortestPath.stream().sorted(Comparator.comparingInt(o -> o.getDp().getEdgeList().size())).collect(Collectors.toList());

        Collections.sort(shortestPath, Comparator.comparingInt(o -> o.getDp().getEdgeList().size()));
        while (true) {
            //1. 找到sape最大的最短路径
            ShortestPath selected = shortestPath.stream().max(Comparator.comparingDouble(ShortestPath::getSape)).get();
            //2. 将所选路径插入缓存
            addThreeIndex(selected);
            if (size >= capacity) break;
            //3. 从sortedPath中删除所选路径及其能应答的路径
            shortestPath.remove(selected);
            for (String s : selected.getCanAnswer()) {
                shortestPath.remove(new ShortestPath(s));
            }
            //sortedPath已空
            if (shortestPath.isEmpty()) break;
        }

    }

    /**
     * 构建三大索引
     * 1. Edge Information Index
     * 2. Edge neighbor Index
     * 3. Edge Path Index
     */
    void addThreeIndex(ShortestPath path) {
        Edge cur;
        Edge pre = null;
        for (Object e : path.getDp().getEdgeList()) {
            cur = (Edge) e;
            String curEid = cur.getEid();

            // build EII
            if (!EII.containsKey(curEid)) {
                EII.put(curEid, (Edge) e);
                size += 2;
            }
            // build ENI
            if (pre != null) {
                String preEid = pre.getEid();
                // put curEid into preEid set
                addNeighborEdge(curEid, preEid);
                // put preEid into curEid set
                addNeighborEdge(preEid, curEid);
            }
            pre = cur;

            // build EPI
            if (EPI.containsKey(curEid)) {
                if (!EPI.get(curEid).contains(path.pathID)) {
                    EPI.get(curEid).add(path.pathID);
                    size++;
                }
            } else {
                HashSet<String> shortestPaths = new HashSet<>();
                shortestPaths.add(path.getPathID());
                EPI.put(curEid, shortestPaths);
                size++;
            }
        }
    }

    /**
     * 将cur添加到pre的相邻边索引中
     *
     * @param curEid
     * @param preEid
     */
    private void addNeighborEdge(String curEid, String preEid) {
        if (ENI.containsKey(preEid)) {
            if (!ENI.get(preEid).contains(curEid)) {
                ENI.get(preEid).add(curEid);
                size++;
            }
        } else {
            HashSet<String> strings = new HashSet<>();
            strings.add(curEid);
            ENI.put(preEid, strings);
            size++;
        }
    }

    int canLocate = 0;
    int notCanLocate = 0;

    /**
     * 使用缓存进行尝试应答
     *
     * @return
     */
    boolean findShortestPath(Vertex o, Vertex d) {
        // 在RTree中定位 o,d 所映射的边
        Iterable<Entry<String, Geometry>> oEntries = edgeLocating(o.getLongitude(), o.getLatitude());
        Iterable<Entry<String, Geometry>> dEntries = edgeLocating(d.getLongitude(), d.getLatitude());

        // o,d均可在RTree中定位到相应边
        if (oEntries.iterator().hasNext() && dEntries.iterator().hasNext()) {
            canLocate++;
            // 起点映射的候选边所在最短路径
            List<String> oEdgePath = new ArrayList<>();
            // 终点映射的候选边所在最短路径
            List<String> dEdgePath = new ArrayList<>();
            for (Entry<String, Geometry> oEntry : oEntries) {
                Edge edge = EII.get(oEntry.value());
                Vertex oV = edge.getO();
                Vertex dV = edge.getD();
                // /oVo/+/odV/-/oVdV/<0.000001
                if (claculateDistance(oV, o) + claculateDistance(o, dV) -
                        claculateDistance(oV, dV) < 0.000001) {
                    oEdgePath.addAll(EPI.get(oEntry.value()));
                }
            }
            for (Entry<String, Geometry> dEntry : dEntries) {
                Edge edge = EII.get(dEntry.value());
                Vertex oV = edge.getO();
                Vertex dV = edge.getD();
                // /oVo/+/odV/-/oVdV/<0.000001
                if (claculateDistance(oV, d) + claculateDistance(d, dV) -
                        claculateDistance(oV, dV) < 0.000001) {
                    dEdgePath.addAll(EPI.get(dEntry.value()));
                }
            }

            // 交集检测是否有同一路径
            oEdgePath.retainAll(dEdgePath);
            if (!oEdgePath.isEmpty()) {
                // 从路径中截取对应起点终点的最短路径
                return true;
            }

        } else {
            notCanLocate++;
        }
        return false;
    }

    /**
     * calculate the distance between two vertices
     *
     * @param v1 vertex1
     * @param v2 vertex2
     * @return distance
     */
    double claculateDistance(Vertex v1, Vertex v2) {
        return Math.sqrt(Math.pow(v1.getLongitude() - v2.getLongitude(), 2) + Math.pow(v1.getLatitude() - v2.getLatitude(), 2));
    }

    /**
     * 构建R-Tree
     */
    void buildRTree() {
        tree = RTree.create();
        Vertex o = null;
        Vertex d = null;
        double x1, y1, x2, y2;
        for (Edge e : EII.values()) {
            o = e.getO();
            d = e.getD();
            x1 = Math.min(o.getLongitude(), d.getLongitude());
            y1 = Math.min(o.getLatitude(), d.getLatitude());
            x2 = Math.max(o.getLongitude(), d.getLongitude());
            y2 = Math.max(o.getLatitude(), d.getLatitude());
            tree = tree.add(e.getEid(), Geometries.rectangle(x1, y1, x2, y2));
        }

        tree.visualize(800, 800).save(System.getProperty("user.dir") + "/src/main/resources/mytree.png");
    }

    /**
     * 在RTree中定位边
     *
     * @param longitude 定位点经度
     * @param latitude  定位点维度
     * @return 命中边的迭代集合
     */
    Iterable<Entry<String, Geometry>> edgeLocating(double longitude, double latitude) {
        Iterable<Entry<String, Geometry>> entries = tree.search(Geometries.pointGeographic(longitude, latitude))
                .toBlocking().toIterable();
        return entries;
    }
}
