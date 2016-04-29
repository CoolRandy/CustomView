package com.yalantis.phoenix.sample.priority;

/**
 * Created by admin on 2016/4/22.
 *
 */
public abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

    private int priority;

    public PriorityRunnable(int priority) {
        if (priority < 0){
            throw new IllegalStateException();
        }
        this.priority = priority;
    }

    @Override
    public void run() {
        doSomething();
    }

    @Override
    public int compareTo(PriorityRunnable another) {
        int my = this.priority;
        int other = another.priority;
        return my < other? 1 : my > other? -1 : 0;
    }

    public abstract void doSomething();

    public int getPriority() {
        return priority;
    }
}
