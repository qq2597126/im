package com.lcy.common.concurrent;


import com.google.common.util.concurrent.*;

import java.util.concurrent.*;

public class CallbackTaskScheduler extends Thread {

    /**
     * 任务队列
     */
    private ConcurrentLinkedQueue<CallbackTask> workQueue = new ConcurrentLinkedQueue<CallbackTask>();

    /**
     * 核心文件数
     */
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 线程池
     */
    private ExecutorService pool  =  new ThreadPoolExecutor(corePoolSize,corePoolSize,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>());



    ListeningExecutorService gPool = MoreExecutors.listeningDecorator(pool);

    private static CallbackTaskScheduler taskScheduler = new CallbackTaskScheduler();

    /**
     *
     */
    private int sleepTime = 200;

    private CallbackTaskScheduler(){

    }

    public static void add(CallbackTask callbackTask){
        taskScheduler.workQueue.add(callbackTask);
    }

    @Override
    public void run() {
        while(true){
            //任务处理
            handleTask();
            //处理时间
            threadSleep(sleepTime);
        }
    }

    private void threadSleep(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void handleTask() {
        try {
            CallbackTask executeTask = null;
            while ((executeTask = workQueue.poll()) != null) {
                handleTask(executeTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private <T> void handleTask(CallbackTask<T> callbackTask){
        ListenableFuture<T> future = gPool.submit(new ExecuteCallable<T>(callbackTask));
        Futures.addCallback(future,new FutureCallback<T>(){

            @Override
            public void onSuccess(T result) {
                callbackTask.onBack(result);
            }

            @Override
            public void onFailure(Throwable t) {
                callbackTask.onException(t);
            }
        },pool);
    }

    private class ExecuteCallable<T> implements Callable<T>{
        private CallbackTask<T> callbackTask;
        public ExecuteCallable(CallbackTask callbackTask) {
            this.callbackTask = callbackTask;
        }
        @Override
        public T call() throws Exception {
            T execute =  execute = callbackTask.execute();;
            return execute;
        }
    }
}
