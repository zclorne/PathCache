package com.scu.zclorne;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

import java.util.*;
import java.util.stream.Collectors;

public class CacheEPC {
    private int capacity = 500000;
    private int size = 0;
    private HashMap<String, Edge> EII = new HashMap<>();
    private HashMap<String, Set<String>> ENI = new HashMap<>();
    private HashMap<String, List<ShortestPath>> EPI = new HashMap<>();
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
        buildThreeIndex();
        buildRTree();
    }

    /**
     * 统计SA并计算每个最短路径的sharing ability per edge
     */
    void calculateSharingAbility() {
        ShortestPath cur = null;
        ShortestPath next = null;
        for (int i = 0; i < this.shortestPath.size(); i++) {
            for (int j = i + 1; j < this.shortestPath.size(); j++) {
                cur = shortestPath.get(i);
                next = shortestPath.get(j);
                cur.analysisWithOtherPath(next);
                next.analysisWithOtherPath(cur);
            }
            this.shortestPath.get(i).calculateSAPE();
        }
    }

    /**
     * 构建缓存
     */
    void buildCache() {
        calculateSharingAbility();

        //将路径按边数（也就是节点数，节点数=边数+1）排序
        List<ShortestPath> sortedPath = shortestPath.stream().sorted(Comparator.comparingInt(o -> o.getDp().getEdgeList().size())).collect(Collectors.toList());

        while (true) {
            //1. 找到sape最大的最短路径
            ShortestPath selected = sortedPath.stream().max(Comparator.comparingDouble(ShortestPath::getSape)).get();
            int pathSize = selected.getDp().getVertexList().size();
            //达到容量限制
            if (pathSize + size > capacity) {
                break;
            }
            //2. 将所选路径插入缓存
            cache.add(selected);
            size += pathSize;
            //3. 从sortedPath中删除所选路径及其能应答的路径
            sortedPath.remove(selected);
            selected.getCanAnswer().forEach(sortedPath::remove);
            //sortedPath已空
            if (sortedPath.isEmpty()) break;
        }

    }

    /**
     * 构建三大索引
     * 1. Edge Information Index
     * 2. Edge neighbor Index
     * 3. Edge Path Index
     */
    void buildThreeIndex() {
        Edge cur;
        Edge pre = null;
        for (ShortestPath p : cache) {
            for (Object e : p.getDp().getEdgeList()) {
                cur = (Edge) e;
                String curEid = cur.getEid();

                // build EII
                if (!EII.containsKey(curEid)) {
                    EII.put(curEid, (Edge) e);
                }
                // build ENI
                if (pre != null) {
                    String preEid = pre.getEid();
                    // put curEid into preEid set
                    if (ENI.containsKey(preEid)) {
                        ENI.get(preEid).add(curEid);
                    } else {
                        HashSet<String> strings = new HashSet<>();
                        strings.add(curEid);
                        ENI.put(preEid, strings);
                    }
                    // put preEid into curEid set
                    if (ENI.containsKey(curEid)) {
                        ENI.get(curEid).add(preEid);
                    } else {
                        HashSet<String> strings = new HashSet<>();
                        strings.add(preEid);
                        ENI.put(curEid, strings);
                    }
                }
                pre = cur;

                // build EPI
                if (EPI.containsKey(curEid)) {
                    EPI.get(curEid).add(p);
                } else {
                    ArrayList<ShortestPath> shortestPaths = new ArrayList<>();
                    shortestPaths.add(p);
                    EPI.put(curEid, shortestPaths);
                }
            }
        }
    }

    /**
     * 使用缓存进行尝试应答
     *
     * @return
     */
    ShortestPath findShortestPath(Vertex o, Vertex d) {
        // 在RTree中定位 o,d 所映射的边
        Iterable<Entry<String, Geometry>> oEntries = edgeLocating(o.getLongitude(), o.getLatitude());
        Iterable<Entry<String, Geometry>> dEntries = edgeLocating(d.getLongitude(), d.getLatitude());
        List<String> oEdge = new ArrayList<>();
        List<String> dEdge = new ArrayList<>();
        // o,d均可在RTree中定位到相应边
        if (oEntries.iterator().hasNext() && dEntries.iterator().hasNext()) {
            for (Entry<String, Geometry> oEntry : oEntries) {
                oEdge.add(oEntry.value());
            }
            for (Entry<String, Geometry> dEntry : dEntries) {
                dEdge.add(dEntry.value());
            }
            // 起点映射的候选边
            List<ShortestPath> oEdgePath = new ArrayList<>();
            // 终点映射的候选边
            List<ShortestPath> dEdgePath = new ArrayList<>();
            for (String s : oEdge) {
                oEdgePath.addAll(EPI.get(s));
            }
            for (String s : dEdge) {
                dEdgePath.addAll(EPI.get(s));
            }
            // 交集检测是否有同一路径
            oEdgePath.retainAll(dEdgePath);
            if (!oEdgePath.isEmpty()) {
                // 从路径中截取对应起点终点的最短路径
                for (ShortestPath path : oEdgePath) {
//                    System.out.println(path.getDp());
                    return path;
                }
            }

        }
        return null;
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
            tree = tree.add(e.getEid(), Geometries.rectangleGeographic(x1, y1, x2, y2));
        }
    }

    /**
     * 在RTree中定位边
     *
     * @param longitude 定位点经度
     * @param latitude  定位点维度
     * @return 命中边的迭代集合
     */
    Iterable<Entry<String, Geometry>> edgeLocating(double longitude, double latitude) {
        Iterable<Entry<String, Geometry>> entries = tree.search(Geometries.point(longitude, latitude))
                .toBlocking().toIterable();
        return entries;
    }

}
