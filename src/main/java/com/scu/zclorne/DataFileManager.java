package com.scu.zclorne;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class DataFileManager {
    String dataDir = "/src/main/resources/";

    /**
     * 将运行结果写入文件
     * @param content
     */
    void writeData(String content){
        String fileName = System.getProperty("user.dir") + dataDir + "TestResult.txt";
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read node information from file
     */
    HashMap<String, Vertex> readNode() throws IOException {
        HashMap<String, Vertex> vertexMap = new HashMap<>();
        String filePath = System.getProperty("user.dir") + dataDir + "California Road Network's Nodes (Node ID, Longitude, Latitude).txt";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings = str.split(" ");
                vertexMap.put(strings[0],new Vertex(strings[0],Double.parseDouble(strings[1]),Double.parseDouble(strings[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return vertexMap;
    }

    /**
     * read edge information from file
     */
    HashMap<String, Edge> readEdge(HashMap<String,Vertex> vMap) throws IOException {
        HashMap<String, Edge> edgeMap = new HashMap<>();
        String filePath = System.getProperty("user.dir") + dataDir + "California Road Network's Edges (Edge ID, Start Node ID, End Node ID, L2 Distance).txt";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings=str.split(" ");
                edgeMap.put(strings[0],new Edge(strings[0],vMap.get(strings[1]),vMap.get(strings[2]),Double.parseDouble(strings[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return edgeMap;
    }

    /**
     * read POI information from file
     */
    HashMap<String, Vertex> readPOI() throws IOException {
        String filePath = System.getProperty("user.dir") + dataDir + "California's Points of Interest (Longitude, Latitude, Category ID).txt";
        BufferedReader in = null;
        int count = 0;
        HashMap<String, Vertex> poiMap = new HashMap<>();
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            String[] strings;
            while ((str = in.readLine()) != null) {
                strings = str.split(" ");
                poiMap.put(""+count,new Vertex(""+count,Double.parseDouble(strings[0]),Double.parseDouble(strings[1])));
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return poiMap;
    }


}