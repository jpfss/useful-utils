package com.example.utils.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 及时返回的互斥锁
 *
 * 使用方法：
 * AccessMutex mutex = AccessMutex.getInstance();
 * try {
 *     mutex.tryAcquire();
 *     ....
 * } finally {
 *     mutex.release();
 * }
 *
 * @author cq created on 2018/2/9
 */
public class AccessMutex {

    private volatile static AccessMutex INSTANCE;

    private final AtomicInteger state;

    private transient Thread exclusiveOwner;

    private AccessMutex() {
        state = new AtomicInteger(0);
    }

    public static AccessMutex getInstance() {
        if (INSTANCE == null) {
            synchronized (AccessMutex.class) {
                INSTANCE = new AccessMutex();
            }
        }
        return INSTANCE;
    }

    /**
     * 尝试获取锁
     *
     * @return 成功与否
     */
    public boolean tryAcquire() {
        int s = state.get();
        if (s == 0 && state.compareAndSet(0, 1)) {
            exclusiveOwner = Thread.currentThread();
            return true;
        }
        return false;
    }

    /**
     * 释放锁， 不只能加锁的线程释放
     *
     * @return 释放成功与否
     */
    public boolean release() {
        int s = state.get();
        if (!isHeldExclusively()) {
            throw new IllegalStateException("非持有锁线程不能释放!");
        }
        if (s != 0) {
            state.set(0);
            exclusiveOwner = null;
            return true;
        }
        return false;
    }

    public boolean isHeldExclusively() {
        return exclusiveOwner == Thread.currentThread();
    }
}
