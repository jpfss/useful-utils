package com.example.utils.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个方便异步获取线程池返回结果数据的util
 *
 * 使用方法:
 * CompletableTreadPoolUtil<Task> ctp = new CompletableTreadPoolUtil<>(corePoolSize, maximumPoolSize,
 *                         keepAliveTime, timeUnit,
 *                         new XxxBlockingDeque<>(),
 *                         CompletableTreadPoolUtil.namedThreadFactory("XXXX"));
 *
 * ctp.submit(new Task());
 * ......
 * ctp.shutdown();
 *
 * //do some other things...
 *
 * //等待结果返回
 * List<V> resultList = ctp.awaitForResult();
 * 或者
 * ctp.await();
 *
 * @author chen.qian
 * @date 2018/6/14
 */
public class CompletableTreadPoolUtil<V> {

    /**
     * thread pool
     */
    private final ThreadPoolExecutor executor;

    /**
     * completionService
     */
    private CompletionService<V> completionService;

    /**
     * 返回结果
     */
    private volatile List<V> resultList = new ArrayList<>();

    /**
     * 未完成标识
     */
    private volatile boolean notComplete = true;

    /**
     * 提交任务数
     */
    private final AtomicInteger taskNum = new AtomicInteger(0);

    /**
     * 发生异常标识
     */
    private volatile boolean exceptionOccurred = false;

    public CompletableTreadPoolUtil(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public CompletableTreadPoolUtil(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue,
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    public CompletableTreadPoolUtil(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize,
                keepAliveTime, unit,
                workQueue, Executors.defaultThreadFactory(), handler);

    }

    public CompletableTreadPoolUtil(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory,
                                    RejectedExecutionHandler handler) {
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit,
                workQueue, threadFactory, handler);
        completionService = new ExecutorCompletionService<>(executor);
    }


    /**
     * @param callable task
     * @return future
     */
    public Future<V> submit(Callable<V> callable) {
        taskNum.getAndIncrement();
        return completionService.submit(callable);
    }

    /**
     * shutdown
     */
    public void shutdown() {
        executor.shutdown();
        collectResult();
    }

    /**
     * @return true done
     */
    public boolean isDone() {
        return !notComplete;
    }

    /**
     * await to complete
     */
    public void await() {
        if (!executor.isShutdown()) {
            throw new IllegalStateException("Haven't shutdown yet!");
        }
        while (notComplete) {
            Thread.yield();
        }
    }

    /**
     * await to complete and return result
     * @return result
     */
    public List<V> awaitForResult() {
        this.await();
        return resultList;
    }

    /**
     * is exception occurred
     *
     * @return true exception occurred
     */
    public boolean isExceptionOccurred() {
        return exceptionOccurred;
    }

    /**
     * collect result
     */
    private void collectResult() {
        new Thread(() -> {
            int completedTaskNum = 0;
            List<V> list = new ArrayList<>();
            try {
                while (completedTaskNum != taskNum.get()) {
                    try {
                        Future<V> f;
                        if ((f = completionService.take()) != null) {
                            V v = null;
                            try {
                                v = f.get();
                            } catch (InterruptedException e) {
                                // 任务已经完成get立马返回，不会进到这里
                            } catch (ExecutionException e) {
                                exceptionOccurred = true;
                            } finally {
                                completedTaskNum++;
                            }
                            if (v != null) {
                                list.add(v);
                            }
                        }
                    } catch (InterruptedException e) {
                        //不响应中断，继续执行
                    }
                }
            } finally {
                resultList = list;
                notComplete = false;
            }
        }).start();
    }

    /**
     * 获取一个可命名的ThreadFactory
     *
     * @param namePrefix namePrefix
     * @return ThreadFactory
     */
    public static ThreadFactory namedThreadFactory(String namePrefix) {
        return new NamedThreadFactory(namePrefix);
    }

    public static class NamedThreadFactory implements ThreadFactory {

        private AtomicInteger threadNum = new AtomicInteger(0);

        private String namePrefix;

        public NamedThreadFactory(String namePrefix) {
            Objects.requireNonNull(namePrefix);
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix
                    + ("".equals(namePrefix) ? "" : "-thread-"
                    + threadNum.getAndIncrement()));
        }
    }
}
