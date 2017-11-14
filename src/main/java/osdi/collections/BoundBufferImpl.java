package osdi.collections;

import osdi.locks.Monitor;
import osdi.locks.SpinLock;

import java.util.ArrayDeque;

/*
 * Modify this as you see fit. you may not use anything in java.util.concurrent.* you may only use locks from osdi.locks.*
 */
class BoundBufferImpl<T> implements SimpleQueue<T> {
    private final int bufferSize;
    private final java.util.Queue<T> queue;
    Monitor monitor;
    SpinLock lock;

    public BoundBufferImpl(int bufferSize) {
        this.bufferSize = bufferSize;
        queue = new ArrayDeque<>(bufferSize);
        monitor = new Monitor();
        lock = new SpinLock();
    }

    @Override
    public void enqueue(T item) {
        while (this.queue.size() == bufferSize) {
                monitor.sync((Monitor.MonitorOperations::Wait));
        }
        if (this.queue.size() >= 0) {
            monitor.sync((Monitor.MonitorOperations::pulse));
        }
        lock.lock();
        if (item != null) {
            queue.add(item);
            //System.out.println(Thread.currentThread().getName()+" ins"+item);
        }
        lock.unlock();

    }

    @Override
    public T dequeue() {
        T item = null;
        while(queue.isEmpty()){
            monitor.sync((Monitor.MonitorOperations::Wait));
        }
        if(this.queue.size() <= bufferSize){
            monitor.sync((Monitor.MonitorOperations::pulse));
        }
        lock.lock();
        if (!queue.isEmpty()) {
            item = queue.remove();
            //System.out.println(Thread.currentThread().getName()+" del"+item);
        }
        lock.unlock();

        return item;
    }

    @Override
    public long size() {
        return queue.size();
    }
}
