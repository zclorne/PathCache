package com.scu.zclorne;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface DataManager {

    /**
     * 将运行结果写入文件
     * @param content
     */
    void writeData(String content);

    /**
     * read node information from file
     */
    HashMap<String, Vertex> readNode() throws IOException;

    /**
     * read edge information from file
     */
    HashMap<String, Edge> readEdge(HashMap<String,Vertex> vMap) throws IOException;

    /**
     * read POI information from file
     */
    List<double[]> readPOI() throws IOException;

}