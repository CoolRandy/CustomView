package com.yalantis.phoenix.sample.priority;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by admin on 2016/4/22.
 *

 */
public class PauseableThreadPoolExecutor extends ThreadPoolExecutor {

    private boolean isPaused;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public PauseableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * 任务执行前执行的方法
     * @param t
     * @param r
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        lock.lock();
        try {
            while (isPaused){
                condition.await();
            }
        }catch (InterruptedException e){
//            e.printStackTrace();
            t.interrupt();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 任务执行结束后执行的方法
     * @param r
     * @param t
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        Log.e("TAG", "-->afterExecute");
    }

    /**
     * 线程池关闭后执行的方法
     */
    @Override
    protected void terminated() {
        super.terminated();
        Log.e("TAG", "-->terminated");
    }

    public void pause(){

        lock.lock();
        try {
            isPaused = true;
        }finally {
            lock.unlock();
        }
    }

    public void resume(){
        lock.lock();
        try {
            isPaused = false;
            condition.signalAll();//唤醒所有等待线程，类似于notifyAll
        }finally {
            lock.unlock();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }
}
