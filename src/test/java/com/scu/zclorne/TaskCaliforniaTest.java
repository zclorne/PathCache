package com.scu.zclorne;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class TaskCaliforniaTest {

    @Test
    void buildGraph() throws IOException, InterruptedException {
        TaskCalifornia taskCalifornia = new TaskCalifornia();
        taskCalifornia.buildGraph();
//        taskCalifornia.singleTest();
        taskCalifornia.cacheTestParallel();
        taskCalifornia.cacheTestParallel();
        taskCalifornia.cacheTestParallel();
    }
}