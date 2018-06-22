package com.example.utils.common;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 计时器(可重入)
 *
 * 由于有内存泄漏的风险
 * 推荐:
 *
 *      TimeCounter.start();
 *      ...
 *      TimeCounter.start();
 *      try {
 *        //...
 *     } finally {
 *         long cost1 = TimeCounter.count(unit);
 *         long cost2 = TimeCounter.count(unit);
 *     }
 * 或者使用者可以保证count方法或者remove方法一定会执行，又或者不在乎这点儿内存hhh(不推荐)
 *
 * @author chen.qian
 * @date 2018/4/8
 */
public class TimeCounter {

    private static final ThreadLocal<ElasticLinkedList<Long>> PIVOT = new ThreadLocal<>();

    public static void start() {
        ElasticLinkedList<Long> l = PIVOT.get();
        l = l == null ? new ElasticLinkedList<>() : l;
        l.add(System.nanoTime());
        PIVOT.set(l);
    }

    public static long count(TimeUnit unit) {
        ElasticLinkedList<Long> l;
        if ((l = PIVOT.get()) == null) {
            return 0L;
        }
        long cost = System.nanoTime() - l.poolLastOrElse(0L);
        //防止内存泄漏
        if (l.isEmpty()) {
            remove();
        }
        return unit.convert(cost, TimeUnit.NANOSECONDS);
    }

    public static void remove() {
        PIVOT.remove();
    }

    public static long countMillis() {
        return count(TimeUnit.MILLISECONDS);
    }

    public static long countSeconds() {
        return count(TimeUnit.SECONDS);
    }

    /**
     * 供TimeCounter使用的可伸缩的LinkedList
     * 由于使用TreadLocal所以线程不安全
     *
     * 当只有一个元素的时候退化成单个field，超过一个元素的时候为base + linkedList的形式
     *
     * @param <E>
     */
    private static class ElasticLinkedList<E> {

        private E base;

        private LinkedList<E> l;

        void add(E e) {
            if (base == null) {
                base = e;
            } else {
                l = l == null ? new LinkedList<>() : l;
                l.add(e);
            }
        }

        E poolLast() {
            E r = null;
            if (l != null && !l.isEmpty()) {
                r = l.pollLast();
                if (l.isEmpty()) {
                    l = null;
                }
            } else if (base != null) {
                l = null;
                r = base;
                base = null;
            }
            return r;
        }

        E poolLastOrElse(E e) {
            E r = poolLast();
            return r == null ? e : r;
        }

        boolean isEmpty() {
            return base == null && (l == null || l.isEmpty());
        }
    }
}
