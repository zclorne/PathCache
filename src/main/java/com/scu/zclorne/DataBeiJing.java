package com.scu.zclorne;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DataBeiJing implements DataManager {

    String dataDir = "/src/main/resources/beijing/";
    String dataFile = "TestResult.txt";

    @Override
    public void writeData(String content) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = System.getProperty("user.dir") + dataDir + simpleDateFormat.format(new Date()) + "-" + dataFile;
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<String, Vertex> readNode() throws IOException {
        HashMap<String, Vertex> vertexMap = new HashMap<>();
        String filePath = System.getProperty("user.dir") + dataDir + "vertices.txt";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings = str.split("\t");
                vertexMap.put(strings[0], new Vertex(strings[0], Double.parseDouble(strings[2]), Double.parseDouble(strings[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return vertexMap;
    }

    @Override
    public HashMap<String, Edge> readEdge(HashMap<String, Vertex> vMap) throws IOException {
        HashMap<String, Edge> edgeMap = new HashMap<>();
        String filePath = System.getProperty("user.dir") + dataDir + "edges.txt";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings = str.split("\t");
                edgeMap.put(strings[0], new Edge(strings[0], vMap.get(strings[1]), vMap.get(strings[2]), Double.parseDouble(strings[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return edgeMap;
    }

    @Override
    public List<double[]> readPOI() throws IOException {
//        String filePath = System.getProperty("user.dir") + dataDir + "poi_mapping.txt";
        String filePath = System.getProperty("user.dir") + dataDir + "queries.csv";
        BufferedReader in = null;
        List<double[]> pois = new ArrayList<>(110000);
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings = str.split(" ");
                pois.add(new double[]{Double.parseDouble(strings[1]), Double.parseDouble(strings[2]),
                        Double.parseDouble(strings[3]), Double.parseDouble(strings[4])});
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return pois;
    }
}
