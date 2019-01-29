package com.github.wlb.rectangle;

import com.google.common.collect.Range;
import lombok.Data;

import java.util.UUID;

/**
 * Range只要内容相同，hashcode equals 都是相等的，所以这里包装一下，可以让内容相同的range作为不同的key放入map中
 *
 * @description: range
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

@Data
public class RangeDecorater {
    String id;
    Range<Integer> range;

    public RangeDecorater(Range<Integer> range) {
        this.range = range;
        this.id = UUID.randomUUID().toString();
    }

}
