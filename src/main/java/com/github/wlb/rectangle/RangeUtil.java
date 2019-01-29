package com.github.wlb.rectangle;

import com.google.common.collect.RangeSet;

/**
 * @description: 数字区间util
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

public class RangeUtil {

    private RangeUtil() {
        throw new IllegalStateException("Utility class");
    }


    //RangeSet是否相等
    public static boolean isRangeSetEquals(RangeSet<Integer> srcRangeSet, RangeSet<Integer> tarRangeSet) {
        return srcRangeSet.enclosesAll(tarRangeSet) && tarRangeSet.enclosesAll(srcRangeSet);
    }

    //RangeSet是否包含
    public static boolean isRangeSetContain(RangeSet<Integer> srcRangeSet, RangeSet<Integer> tarRangeSet) {
        return srcRangeSet.enclosesAll(tarRangeSet);
    }

    //RangeSet是否相交
    public static boolean isRangeSetInters(RangeSet<Integer> srcRangeSet, RangeSet<Integer> tarRangeSet) {
        return srcRangeSet.asRanges().stream().anyMatch(tarRangeSet::intersects);
    }


}
