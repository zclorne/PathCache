package com.scu.zclorne;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TaskBeijingTest {

    @Test
    void countSubPath() throws IOException, InterruptedException {
        TaskBeijing taskBeijing = new TaskBeijing();
        taskBeijing.buildGraph();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        for (int i = 5000; i < 60000; i+=5000) {
            int finalI = i;
            executor.execute(()->{
                try {
                    taskBeijing.countSubPath(finalI);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        // 线程池执行完毕
        while (true) {
            TimeUnit.MINUTES.sleep(30);
            if (executor.isTerminated()) {
                break;
            }
        }
    }
}