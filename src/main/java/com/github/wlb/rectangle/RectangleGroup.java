package com.github.wlb.rectangle;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 矩形组
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

@Data
public class RectangleGroup {

    //所有矩形成员组合后的最大范围，用于快速的完成初步判断，避免逐条比较
    RangeSet<Integer> xaxisRangeSet = TreeRangeSet.create();   //矩形组的 xRange
    RangeSet<Integer> yaxisRangeSet = TreeRangeSet.create();   //矩形组的 yRange

    //将一个 二维集合 转换为 一维Map，这样才能完成后续的比较，例如： (1,1,5,10) 转为Map (1-5) -> (1-10)，分别对应X轴范围和Y轴范围
    Map<RangeDecorater, RangeSet<Integer>> rangeMap = new HashMap<>();

    public RectangleGroup(List<Rectangle> rectList) {
        List<RangeDecorater> tempRangeList = new ArrayList<>();
        Map<RangeDecorater, RangeSet<Integer>> tempRangeMap = new HashMap<>();

        rectList.forEach(rectangle -> {
            xaxisRangeSet.add(rectangle.getXaxisRange());
            yaxisRangeSet.add(rectangle.getYaxisRange());

            //此处可根据实际情况反转 x, y 。 内容少的作为key，内容多的作 value， 可以提高性能
            RangeDecorater key = new RangeDecorater(rectangle.getYaxisRange());
            tempRangeList.add(key);
            RangeSet<Integer> value = TreeRangeSet.create();
            value.add(rectangle.getXaxisRange());
            tempRangeMap.put(key, value);
        });

        //合并相交的内容
        mergeRangeMap(tempRangeList, tempRangeMap, rangeMap);

        //合并相邻内容
        mergeConnected(rangeMap);
    }

    //合并相交的rangeMap,
    private void mergeRangeMap(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap,
                               Map<RangeDecorater, RangeSet<Integer>> mergedRangeMap) {
        if (rangeList.isEmpty()) {
            return;
        }

        //排序
        rangeList = rangeList.stream().sorted(Comparator.comparing(rangeDecorater -> rangeDecorater.getRange().lowerEndpoint())).collect(Collectors.toList());

        RangeDecorater srcRange = rangeList.get(0);
        if (rangeList.size() == 1) {
            mergedRangeMap.put(srcRange, rangeMap.get(srcRange));
            return;
        }

        RelationType relation = RelationType.DIFFERENT;
        RangeDecorater tarRange = null;
        for (int i = 1; i < rangeList.size(); i++) {
            relation = RectangleUtil.compareRange(srcRange.getRange(), rangeList.get(i).getRange());
            if (relation != RelationType.DIFFERENT) {
                tarRange = rangeList.get(i);
                break;
            }
        }

        switch (relation) {
            case EQUAL:
                rangeMap.get(srcRange).addAll(rangeMap.get(tarRange));
                removeRangeItem(rangeList, rangeMap, tarRange);
                break;
            case SUPERSET:
                mergeContainRange(rangeList, rangeMap, srcRange, tarRange);
                break;
            case SUBSET:
                mergeContainRange(rangeList, rangeMap, tarRange, srcRange);
                break;
            case INTERSECTION:
                mergeIntersecRange(rangeList, rangeMap, srcRange, tarRange);
                break;
            case DIFFERENT:
                mergedRangeMap.put(srcRange, rangeMap.get(srcRange));
                removeRangeItem(rangeList, rangeMap, srcRange);
                break;
            default:
                break;
        }
        mergeRangeMap(rangeList, rangeMap, mergedRangeMap);
    }

    //处理包含关系
    private void mergeContainRange(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap,
                                   RangeDecorater srcRange, RangeDecorater tarRange) {
        if (RangeUtil.isRangeSetContain(rangeMap.get(srcRange), rangeMap.get(tarRange))) { //value包含
            removeRangeItem(rangeList, rangeMap, tarRange);  //删除被包含的
        } else { //不包含，分拆
            rangeMap.get(tarRange).addAll(rangeMap.get(srcRange));
            mergeSplitRange(rangeList, rangeMap, srcRange, tarRange);
            removeRangeItem(rangeList, rangeMap, srcRange);
        }
    }

    //处理相交关系
    private void mergeIntersecRange(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap,
                                    RangeDecorater srcRange, RangeDecorater tarRange) {

        if (RangeUtil.isRangeSetEquals(rangeMap.get(srcRange), rangeMap.get(tarRange))) {  //value相等
            Range newRange = RectangleUtil.mergeRange(srcRange.getRange(), tarRange.getRange());  //合并
            RangeDecorater rangeDecorater = new RangeDecorater(newRange);
            rangeList.add(rangeDecorater);
            rangeMap.put(rangeDecorater, rangeMap.get(srcRange));
        } else {
            //找出相交的部分
            Range interRange = srcRange.getRange().intersection(tarRange.getRange());
            RangeDecorater interRangeDecorater = new RangeDecorater(interRange);

            //相交的部分，合并value
            RangeSet<Integer> interRangeSet = TreeRangeSet.create();
            interRangeSet.addAll(rangeMap.get(srcRange));
            interRangeSet.addAll(rangeMap.get(tarRange));
            rangeList.add(0, interRangeDecorater);
            rangeMap.put(interRangeDecorater, interRangeSet);

            mergeSplitRange(rangeList, rangeMap, srcRange, interRangeDecorater);
            mergeSplitRange(rangeList, rangeMap, tarRange, interRangeDecorater);
        }
        removeRangeItem(rangeList, rangeMap, srcRange);
        removeRangeItem(rangeList, rangeMap, tarRange);
    }

    //拆分，然后加入list
    private void mergeSplitRange(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap,
                                 RangeDecorater range, RangeDecorater interRange) {
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        rangeSet.add(range.getRange());
        rangeSet.remove(interRange.getRange());
        rangeSet.asRanges().stream().filter(rangeItem -> !rangeItem.isEmpty())
                .forEach(newRange -> {
                    RangeDecorater newRangeDecorater = new RangeDecorater(newRange);
                    rangeList.add(0, newRangeDecorater);

                    RangeSet<Integer> newRangeSet = TreeRangeSet.create();
                    newRangeSet.addAll(rangeMap.get(range));
                    rangeMap.put(newRangeDecorater, newRangeSet);
                });
    }

    //从templist 和 tempMap 删除
    private void removeRangeItem(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap, RangeDecorater tarRange) {
        rangeMap.remove(tarRange);
        rangeList.remove(tarRange);
    }

    //合并相邻的RangeMap
    private void mergeConnected(Map<RangeDecorater, RangeSet<Integer>> mergedRangeMap) {
        List<RangeDecorater> rangeList = mergedRangeMap.keySet().stream()
                .sorted(Comparator.comparing(rangeDecorater -> rangeDecorater.getRange().lowerEndpoint()))
                .collect(Collectors.toList());

        //如果key相邻，且value相等，那么合并
        for (int i = 0; i < rangeList.size() - 1; i++) {
            if (rangeList.get(i).getRange().upperEndpoint() == rangeList.get(i + 1).getRange().lowerEndpoint()
                    && RangeUtil.isRangeSetEquals(mergedRangeMap.get(rangeList.get(i)), mergedRangeMap.get(rangeList.get(i + 1)))) {
                mergedRangeMap.put(new RangeDecorater(RectangleUtil.mergeRange(
                        rangeList.get(i).getRange(), rangeList.get(i + 1).getRange())), mergedRangeMap.get(rangeList.get(i)));
                mergedRangeMap.remove(rangeList.get(i));
                mergedRangeMap.remove(rangeList.get(i + 1));
                mergeConnected(mergedRangeMap);
                break;
            }
        }
    }

    /**
     * 比较 当前矩形组 和 目标矩形组 的关系
     *
     * @param tarRectangleGroup
     * @return
     */
    public RelationType compare(RectangleGroup tarRectangleGroup) {
        //可以根据 X轴 和 Y轴 的区域范围先做初步判断，然后根据具体情况，再做进一步比较。
        //这里可以根据具体情况，把相关区域单独拿出来，再结合初步判断结果，可以优化比较方式，以提升性能。 暂时先做简单判断，以后有空再说。

        if (RangeUtil.isRangeSetInters(this.xaxisRangeSet, tarRectangleGroup.getXaxisRangeSet())
                && RangeUtil.isRangeSetInters(this.yaxisRangeSet, tarRectangleGroup.getYaxisRangeSet())) {

            return compareRectangleGroup(tarRectangleGroup);
        }

        return RelationType.DIFFERENT;
    }

    /**
     * 比较 当前矩形组 与 目标矩形组 的 rangeMap
     * <p>
     * 将两个 retMap 中的 key 拆分为一一对应的形式，这样才可以对比； 例如： src: [1-5],  tar: [1-2] [4-5], 拆分为： src: [1-2], [3-3], [4,5],  tar: [1-2] [4-5]
     * 需要拆分的场景：包含、被包含、相交。 直到只有 相等 和 不同
     *
     * @param tarRectangleGroup 目标矩形组
     * @return 关系
     */
    private RelationType compareRectangleGroup(RectangleGroup tarRectangleGroup) {
        splitRangeMap(rangeMap.keySet().stream().collect(Collectors.toList()), rangeMap,
                tarRectangleGroup.rangeMap.keySet().stream().collect(Collectors.toList()), tarRectangleGroup.rangeMap);

        //转换为Map<Range, RangeSet<Integer>> ， 方便containsKey判断
        Map<Range, RangeSet<Integer>> srcRangeMap = rangeMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getRange(), Map.Entry::getValue));

        Map<Range, RangeSet<Integer>> tarRangeMap = tarRectangleGroup.rangeMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getRange(), Map.Entry::getValue));

        //相等
        if (srcRangeMap.entrySet().stream().allMatch(srcRect ->
                tarRangeMap.containsKey(srcRect.getKey())
                        && RangeUtil.isRangeSetEquals(tarRangeMap.get(srcRect.getKey()), srcRect.getValue()))
                &&
                tarRangeMap.entrySet().stream().allMatch(tarRect ->
                        srcRangeMap.containsKey(tarRect.getKey())
                                && RangeUtil.isRangeSetEquals(srcRangeMap.get(tarRect.getKey()), tarRect.getValue()))) {
            return RelationType.EQUAL;
        }

        //包含
        if (tarRangeMap.entrySet().stream().allMatch(tarRect ->
                srcRangeMap.containsKey(tarRect.getKey())
                        && RangeUtil.isRangeSetContain(srcRangeMap.get(tarRect.getKey()), tarRect.getValue()))) {
            return RelationType.SUPERSET;
        }

        //被包含
        if (srcRangeMap.entrySet().stream().allMatch(srcRect ->
                tarRangeMap.containsKey(srcRect.getKey())
                        && RangeUtil.isRangeSetContain(tarRangeMap.get(srcRect.getKey()), srcRect.getValue()))) {
            return RelationType.SUBSET;
        }

        //相交
        if (srcRangeMap.entrySet().stream().anyMatch(srcRect ->
                tarRangeMap.containsKey(srcRect.getKey())
                        && RangeUtil.isRangeSetInters(tarRangeMap.get(srcRect.getKey()), srcRect.getValue()))) {
            return RelationType.INTERSECTION;
        }

        //不等
        if (srcRangeMap.entrySet().stream().noneMatch(srcRect ->
                tarRangeMap.containsKey(srcRect.getKey())
                        && RangeUtil.isRangeSetEquals(tarRangeMap.get(srcRect.getKey()), srcRect.getValue()))) {
            return RelationType.DIFFERENT;
        }

        return RelationType.DIFFERENT;
    }

    /**
     * 将 srcRangeMap，tarRangeMap 中的 key 拆分为一一对应的形式，这样才可以对比。例如：
     * 拆分前： src: [1-5]                     tar: [1-2], [4-5]
     * 拆分后： src: [1-2], [3-3], [4,5]       tar: [1-2], [4-5]
     * <p>
     * 需要拆分的场景：包含、被包含、相交。 直到只有 相等 和 不同
     *
     * @param srcRangeList
     * @param srcRangeMap
     * @param tarRangeList
     * @param tarRangeMap
     */
    private void splitRangeMap(List<RangeDecorater> srcRangeList, Map<RangeDecorater, RangeSet<Integer>> srcRangeMap,
                               List<RangeDecorater> tarRangeList, Map<RangeDecorater, RangeSet<Integer>> tarRangeMap) {
        RelationType relation = RelationType. DIFFERENT;
        int i = 0;
        int j = 0;
        for (; i < srcRangeList.size(); i++) {
            for (j = 0; j < tarRangeList.size(); j++) {
                relation = RectangleUtil.compareRange(srcRangeList.get(i).getRange(), tarRangeList.get(j).getRange());
                if (relation == RelationType.SUPERSET || relation == RelationType.SUBSET || relation == RelationType.INTERSECTION) {  //相交
                    break;
                }
            }
            if (relation == RelationType.SUPERSET || relation == RelationType.SUBSET || relation == RelationType.INTERSECTION) {
                break;
            }
        }

        //相等 或 不同， 拆分完毕
        if (relation == RelationType.EQUAL || relation == RelationType.DIFFERENT) {
            return;
        }

        RangeDecorater srcRange = srcRangeList.get(i);
        RangeDecorater tarRange = tarRangeList.get(j);

        if (i > 0) {  //去掉前面的，下次递归就不重复比了
            srcRangeList = srcRangeList.subList(i, srcRangeList.size());
        }

        switch (relation) {
            case SUPERSET:
                splitRangeMapKey(srcRangeList, srcRangeMap, srcRange, tarRange);
                splitRangeMap(srcRangeList, srcRangeMap, tarRangeList, tarRangeMap);
                break;
            case SUBSET:
                splitRangeMapKey(tarRangeList, tarRangeMap, tarRange, srcRange);
                splitRangeMap(srcRangeList, srcRangeMap, tarRangeList, tarRangeMap);
                break;
            case INTERSECTION:
                Range interRange = srcRange.getRange().intersection(tarRange.getRange());
                splitRangeMapKey(srcRangeList, srcRangeMap, srcRange, new RangeDecorater(interRange));
                splitRangeMapKey(tarRangeList, tarRangeMap, tarRange, new RangeDecorater(interRange));
                splitRangeMap(srcRangeList, srcRangeMap, tarRangeList, tarRangeMap);
                break;
            default:
                break;
        }

    }

    /**
     * 把 range 按照 interRange 拆分成多个，
     *
     * @param rangeList
     * @param rangeMap
     * @param range      要拆分的range
     * @param interRange 相交的range，也就是拆分区域
     */
    private void splitRangeMapKey(List<RangeDecorater> rangeList, Map<RangeDecorater, RangeSet<Integer>> rangeMap,
                                  RangeDecorater range, RangeDecorater interRange) {
        //增加相交的部分
        rangeList.add(interRange);
        rangeMap.put(interRange, rangeMap.get(range));

        //去掉相交的部分，将拆分后结果加入 list 和 map
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        rangeSet.add(range.getRange());
        rangeSet.remove(interRange.getRange());
        rangeSet.asRanges().forEach(newRange -> {
            RangeDecorater newRangeDecorater = new RangeDecorater(newRange);
            rangeList.add(newRangeDecorater);
            rangeMap.put(newRangeDecorater, rangeMap.get(range));
        });

        //从 list 和 map 中去掉之前的 range
        rangeList.remove(range);
        rangeMap.remove(range);
    }

}
