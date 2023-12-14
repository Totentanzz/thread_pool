package com.po_lab.rgr.utils.concurrent.threads;

public class CustomThread {
    private Thread thread;
    private Runnable executableCode, exitCode;
    private CustomThreadPool threadPool;
    private Boolean terminateFlag, exitFlag;

    public CustomThread(Runnable executableCode) {
        this(executableCode,()->{},null);
    }

    public CustomThread(CustomThreadPool threadPool) {
        this(()->{},()->{},threadPool);
    }

    public CustomThread(CustomThreadPool threadPool, ThreadGroup threadGroup, String threadName) {
        this(()->{},()->{},threadPool,threadGroup,threadName);
    }

    public CustomThread(Runnable executableCode, Runnable exitCode, CustomThreadPool threadPool) {
        this(executableCode,
             exitCode,
             threadPool,
             new ThreadGroup("DefaultThreadGroup"),
             "Custom Thread");
    }

    public CustomThread(Runnable executableCode,
                        Runnable exitCode,
                        CustomThreadPool threadPool,
                        ThreadGroup threadGroup,
                        String threadName) {
        this.executableCode = executableCode;
        this.exitCode = exitCode;
        this.threadPool = threadPool;
        this.terminateFlag = exitFlag =  false;
        initThread(threadGroup,threadName);
    }

    public boolean isSleeping() {
        return getThreadState().equals(Thread.State.WAITING);
    }

    public Thread.State getThreadState() {
        return this.thread.getState();
    }

    public CustomThreadPool getThreadPool() {
        return this.threadPool;
    }

    public void setThreadPool(CustomThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void terminate() {
        this.terminateFlag = true;
        this.thread.interrupt();
    }

    @Override
    public String toString() {
        return String.format(
                "CustomThread: name=%s, group=%s, state=%s",
                thread.getName(),
                thread.getThreadGroup()!=null ? thread.getThreadGroup().getName() : "NULL",
                thread.getState());
    }

    private void notifyPool() {
        if (this.threadPool!=null)
            this.threadPool.setUpdated();
    }

    private void initThread(ThreadGroup threadGroup, String threadName) {
        this.thread = new Thread(
                threadGroup,
                () -> {
                    while (!exitFlag) {
                        if (this.terminateFlag) {
                            exitCode.run();
                            exitFlag = true;
                        } else {
                            if (threadPool!=null) {
                                executableCode = threadPool.getNewTask();
                            }
                            executableCode.run();
                            notifyPool();
                        }
                    }
                },
                threadName);
        this.thread.setDaemon(true);
        this.thread.start();
    }
}
