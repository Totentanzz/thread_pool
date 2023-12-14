package com.po_lab.rgr.utils.concurrent.threads;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomThreadPool {

    private final int maxTasks;
    private boolean pullerExitFlag;
    private Thread taskQueuePuller;
    private final AtomicInteger completedTasks;
    private final CustomThreadFactory threadFactory;
    private final ArrayList<CustomThread> threadList;
    private final Queue<Runnable> taskQueue;
    private final CustomSynchronousQueue<Runnable> synchQueue;

    public CustomThreadPool(int threadsNum) {
        this.maxTasks = threadsNum;
        this.completedTasks = new AtomicInteger(0);
        this.threadFactory = new CustomThreadFactory(new ThreadGroup("Pool"),"Custom Thread");
        this.taskQueue = new ArrayDeque<>();
        this.synchQueue = new CustomSynchronousQueue<>();
        this.threadList = generateThreads();
        this.pullerExitFlag = false;
        initQueuePullerThread();
    }

    public void submit(Runnable executionCode) {
        synchronized (this.taskQueue) {
            this.taskQueue.add(executionCode);
        }
    }

    public void invokeAll(Collection<Runnable> taskCollection) {
        taskCollection.forEach(this::submit);
    }

    public void awaitResults() {
        int workingThreads = 1;
        while (workingThreads>0) {
            workingThreads = (int) threadList.stream()
                    .filter(thread-> thread.getThreadState() == Thread.State.RUNNABLE)
                    .count();
        }
    }

    public void awaitResults(int supposedResultsNum) {
        boolean exitFlag = false;
        while (!exitFlag) {
            exitFlag = this.completedTasks.get() >= supposedResultsNum;
        }
    }

    public void shutdown() {
        this.threadList.forEach(CustomThread::terminate);
        this.pullerExitFlag = true;
    }

    public void setUpdated() {
        this.completedTasks.getAndIncrement();
    }

    public Runnable getNewTask() {
        Runnable newExecutableCode = null;
        try {
            newExecutableCode = this.synchQueue.take();
        } catch (InterruptedException exc) {
            newExecutableCode = () -> {};
        }
        if (newExecutableCode==null) newExecutableCode = ()->{};
        return newExecutableCode;
    }

    @Override
    public String toString() {
        return String.format(
                "CustomThreadPool: threadList=%s, taskQueue=%s, poolSize=%d, completedTasks=%d",
                threadList.toString(),
                taskQueue.toString(),
                maxTasks,
                completedTasks);
    }

    private ArrayList<CustomThread> generateThreads() {
        return IntStream
                .rangeClosed(1, this.maxTasks)
                .mapToObj(obj->this.threadFactory.createNewThread(this))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void initQueuePullerThread() {
        Runnable pullTask = () -> {
            while (!pullerExitFlag) {
                synchronized (taskQueue) {
                    if (!taskQueue.isEmpty()) {
                        Runnable polledTask = taskQueue.poll();
                        try {
                            synchQueue.put(polledTask);
                        } catch (InterruptedException exc) {
                            throw new RuntimeException(exc);
                        }
                    }
                }
            }
        };
        this.taskQueuePuller = new Thread(pullTask);
        this.taskQueuePuller.setDaemon(true);
        this.taskQueuePuller.start();
    }

}
