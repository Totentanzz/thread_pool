package com.po_lab.rgr.utils.concurrent.threads;

import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory {
    private static final AtomicInteger factoryNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup threadGroup;
    private final String threadsName;

    public CustomThreadFactory() {
        this(Thread.currentThread().getThreadGroup(),
             String.format("Factory #%d, Thread #", factoryNumber.getAndIncrement()));
    }

    public CustomThreadFactory(ThreadGroup threadGroup, String threadsName) {
        this.threadGroup = threadGroup;
        this.threadsName = String.format(
                "Factory #%d-%s #",
                factoryNumber.getAndIncrement(),
                threadsName);
    }


    public CustomThread createNewThread(CustomThreadPool pool) {
        return new CustomThread(
                pool,
                this.threadGroup,
                this.threadsName + threadNumber.getAndIncrement());
    }
}
