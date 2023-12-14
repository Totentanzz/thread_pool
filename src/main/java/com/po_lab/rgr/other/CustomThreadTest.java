package com.po_lab.rgr.other;

import com.po_lab.rgr.utils.concurrent.threads.CustomThreadPool;

import java.util.ArrayDeque;
import java.util.LinkedList;

public class CustomThreadTest {

    public enum ManualTestType {
        SINGLE_THREAD_ONLY,
        POOL_SIMULATION

    }

    public enum EfficiencyTestType {
        CREATING_THREADS,
        HEAVY_WORK
    }

    public static void runEfficiencyTest(int iterations,
                                         int threadsNum,
                                         int tasksNum,
                                         EfficiencyTestType testType) {
        double manualTimeTaken=0, poolTimeTaken = 0;
        double manualMeanTime, poolMeanTime;
        Runnable task;
        if (testType.equals(EfficiencyTestType.CREATING_THREADS)) {
            task = () -> {};
        } else {
            task = () -> {
                long sum = 0;
                for (int i=0;i<1000000;i++) {
                    sum+=i;
                }
            };
        }
        for (int i=0;i<iterations;i++) {
            manualTimeTaken += manualLoopTest(threadsNum,tasksNum,task,ManualTestType.POOL_SIMULATION);
            poolTimeTaken += poolLoopTest(threadsNum,tasksNum,task);
        }
        manualMeanTime = manualTimeTaken/iterations;
        poolMeanTime = poolTimeTaken/iterations;
        System.out.printf("MEAN TIME WITH MANUAL THREAD TESTING: %.4f (ms)\n", manualMeanTime);
        System.out.printf("MEAN TIME WITH THREAD POOL TESTING: %.4f (ms)\n", poolMeanTime);
    }

    public static double poolLoopTest(int poolSize, int tasksNum, Runnable executingCode) {
        CustomThreadPool threadPool = new CustomThreadPool(poolSize);
        LinkedList<Runnable> taskList = new LinkedList<>();
        long startTime, endTime;
        for (int i=0;i<tasksNum;i++) {
            taskList.add(()->{
                executingCode.run();
            });
        }
        startTime = System.nanoTime();
        threadPool.invokeAll(taskList);
        threadPool.awaitResults(tasksNum);
        endTime = System.nanoTime();
        threadPool.shutdown();
        return (endTime - startTime)/1000000F;
    }

    public static double manualLoopTest(int threadsNum, int tasksNum, Runnable executingCode, ManualTestType testType) {
        ArrayDeque<Thread> threadQueue = new ArrayDeque<>();
        LinkedList<Runnable> taskList = new LinkedList<>();
        long startTime, endTime;
        for (int i = 0; i < tasksNum; i++) {
            taskList.add(()->{
                executingCode.run();
            });
        }
        startTime = System.nanoTime();
        if (testType.equals(ManualTestType.POOL_SIMULATION)) {
            taskList.forEach(task->{
                if (threadQueue.size() >= threadsNum) {
                    Thread oldestThread = threadQueue.poll();
                    try {
                        oldestThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Thread thread = new Thread(task);
                thread.start();
                threadQueue.add(thread);
            });
            threadQueue.forEach(thread-> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            taskList.forEach(task->{
                Thread thread = new Thread(task);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        endTime = System.nanoTime();
        return (endTime - startTime)/1000000F;
    }

}
