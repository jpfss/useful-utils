package com.example.utils.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 切割器
 *
 * @author chen.qian
 * @date 2018/4/23
 */
public class Slicer {

    /**
     * 将list按照指定大小切割成多个list
     *
     * @param list list
     * @param gap 分片大小
     * @param <T> 数据类型
     * @return List<List<T>>
     */
    public static <T> List<List<T>> slice(List<T> list, int gap) {
        Objects.requireNonNull(list);
        if (gap <= 0)
            throw new IllegalArgumentException("gap can not be less than or equals to zero!");

        int size = list.size();
        if (gap >= size) {
            List<List<T>> result = new ArrayList<>();
            result.add(list);
            return result;
        }
        int num = size / gap;
        int remainder = size % gap;
        num += remainder == 0 ? 0 : 1;
        List<List<T>> result = new ArrayList<>(num + 1);
        for (int i = 0; i < num; i++) {
            int from = i * gap;
            int to = i == num - 1 && remainder != 0 ?  from + remainder : from + gap;
            List<T> temp = new ArrayList<>(list.subList(from, to));
            result.add(temp);
        }
        return result;
    }
}
