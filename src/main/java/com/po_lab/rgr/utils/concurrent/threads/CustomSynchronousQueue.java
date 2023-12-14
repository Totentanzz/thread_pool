package com.po_lab.rgr.utils.concurrent.threads;

public class CustomSynchronousQueue<T> {
    private T item;
    private boolean hasItem = false;

    public synchronized void put(T item) throws InterruptedException {
        while (hasItem) {
            wait();
        }
        this.item = item;
        hasItem = true;
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (!hasItem) {
            wait();
        }
        T takenItem = this.item;
        hasItem = false;
        notifyAll();
        return takenItem;
    }
}
