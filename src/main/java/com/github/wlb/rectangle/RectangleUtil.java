package com.github.wlb.rectangle;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.util.List;
import java.util.Set;

/**
 * @description: 矩形组比较工具
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

public class RectangleUtil {

    private RectangleUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 比较两个Range 的关系
     * 在当前场景下，空Range为不同
     *
     * @param srcRange
     * @param tarRange
     * @return
     */
    public static RelationType compareRange(Range srcRange, Range tarRange) {
        if (srcRange.isEmpty() || tarRange.isEmpty()) {
            return RelationType.DIFFERENT;
        }
        if (srcRange.lowerEndpoint() == tarRange.lowerEndpoint()
                && srcRange.upperEndpoint() == tarRange.upperEndpoint()) {
            return RelationType.EQUAL;
        }
        if (srcRange.encloses(tarRange)) {
            return RelationType.SUPERSET;
        }
        if (tarRange.encloses(srcRange)) {
            return RelationType.SUBSET;
        }
        if (srcRange.isConnected(tarRange)) {
            if (srcRange.intersection(tarRange).isEmpty()) {
                return RelationType.DIFFERENT;
            } else {
                return RelationType.INTERSECTION;
            }
        }
        return RelationType.DIFFERENT;
    }

    /**
     * 合并两个相交的Range为一个Range
     *
     * @param srcRange
     * @param tarRange
     * @return 合并后的Range，如果两个Range不相邻，也不相交，那么返回第一个Range
     */
    public static Range<Integer> mergeRange(Range<Integer> srcRange, Range<Integer> tarRange) {
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        rangeSet.add(srcRange);
        rangeSet.add(tarRange);
        Set<Range<Integer>> set = rangeSet.asRanges();
        if (set.size() == 1) {
            return set.iterator().next();
        }
        return srcRange;
    }

    /**
     * 两个矩形的关系
     *
     * @param srcRect
     * @param tarRect
     * @return
     */
    public static RelationType compareRectangle(Rectangle srcRect, Rectangle tarRect) {
        if (srcRect.getXaxisRange().lowerEndpoint() == tarRect.getXaxisRange().lowerEndpoint()
                && srcRect.getXaxisRange().upperEndpoint() == tarRect.getXaxisRange().upperEndpoint()
                && srcRect.getYaxisRange().lowerEndpoint() == tarRect.getYaxisRange().lowerEndpoint()
                && srcRect.getYaxisRange().upperEndpoint() == tarRect.getYaxisRange().upperEndpoint()) {
            return RelationType.EQUAL;
        }
        if (srcRect.getXaxisRange().encloses(tarRect.getXaxisRange()) && srcRect.getYaxisRange().encloses(tarRect.getYaxisRange())) {
            return RelationType.SUPERSET;
        }
        if (tarRect.getXaxisRange().encloses(srcRect.getXaxisRange()) && tarRect.getYaxisRange().encloses(srcRect.getYaxisRange())) {
            return RelationType.SUBSET;
        }
        if (compareRange(srcRect.getXaxisRange(), tarRect.getXaxisRange()) != RelationType.DIFFERENT
                && compareRange(srcRect.getYaxisRange(), tarRect.getYaxisRange()) != RelationType.DIFFERENT) {
            return RelationType.INTERSECTION;
        }
        return RelationType.DIFFERENT;
    }

    /**
     * 比较两组矩形的关系
     *
     * @param srcRectList
     * @param tarRectList
     * @return
     */
    public static RelationType compareRectangleGroup(List<Rectangle> srcRectList, List<Rectangle> tarRectList) {
        RectangleGroup srcRectangleGroup = new RectangleGroup(srcRectList);
        RectangleGroup tarRectangleGroup = new RectangleGroup(tarRectList);
        return srcRectangleGroup.compare(tarRectangleGroup);
    }

    public static RelationType compareRectangleGroup(RectangleGroup srcRectangleGroup, RectangleGroup tarRectangleGroup) {
        return srcRectangleGroup.compare(tarRectangleGroup);
    }

}
