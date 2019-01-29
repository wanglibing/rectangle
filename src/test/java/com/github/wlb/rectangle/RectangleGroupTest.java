package com.github.wlb.rectangle;

import com.github.wlb.rectangle.Rectangle;
import com.github.wlb.rectangle.RectangleGroup;
import com.github.wlb.rectangle.RectangleUtil;
import com.github.wlb.rectangle.RelationType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @description: 测试矩形组比较
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

public class RectangleGroupTest {

    @Test
    void testRectangleGroupCompare() {

        //1,1,5,5
        List<Rectangle> srcRectList = Arrays.asList(
                new Rectangle(1, 1, 5, 2),
                new Rectangle(1, 2, 4, 3),
                new Rectangle(2, 3, 3, 1),
                new Rectangle(1, 5, 4, 1),
                new Rectangle(2, 5, 2, 1),
                new Rectangle(5, 3, 1, 3));

        List<Rectangle> tarRectList = Arrays.asList(
                new Rectangle(1, 1, 5, 3),
                new Rectangle(4, 4, 2, 2),
                new Rectangle(1, 3, 4, 3));

        assertEquals(RelationType.EQUAL, RectangleUtil.compareRectangleGroup(srcRectList, tarRectList));


        //包含
        tarRectList = Arrays.asList(
                new Rectangle(1, 1, 5, 3),
                new Rectangle(4, 4, 2, 1),
                new Rectangle(1, 3, 4, 3));

        RectangleGroup srcRectangleGroup = new RectangleGroup(srcRectList);
        RectangleGroup tarRectangleGroup = new RectangleGroup(tarRectList);
        System.out.println(tarRectangleGroup);
        assertEquals(RelationType.SUPERSET, srcRectangleGroup.compare(tarRectangleGroup));


        //子集
        assertEquals(RelationType.SUBSET, tarRectangleGroup.compare(srcRectangleGroup));


        //相交
        tarRectList = Arrays.asList(
                new Rectangle(1, 1, 5, 3),
                new Rectangle(4, 4, 3, 2),
                new Rectangle(1, 5, 4, 3));

        srcRectangleGroup = new RectangleGroup(srcRectList);
        tarRectangleGroup = new RectangleGroup(tarRectList);
        System.out.println(tarRectangleGroup);
        assertEquals(RelationType.INTERSECTION, srcRectangleGroup.compare(tarRectangleGroup));


        //不同
        tarRectList = Arrays.asList(
                new Rectangle(1, 6, 5, 3),
                new Rectangle(4, 10, 2, 1),
                new Rectangle(1, 8, 4, 3));

        srcRectangleGroup = new RectangleGroup(srcRectList);
        tarRectangleGroup = new RectangleGroup(tarRectList);
        assertEquals(RelationType.DIFFERENT, srcRectangleGroup.compare(tarRectangleGroup));
    }
}
