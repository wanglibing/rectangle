package com.github.wlb.rectangle;

import com.google.common.collect.Range;
import lombok.Data;

/**
 * @description: 矩形
 * @author: Wanglibing
 * @create: 2019-01-12
 **/

@Data
public class Rectangle {
    private int xpoint;         //x 坐标
    private int ypoint;         //y 坐标
    private int width;          //宽
    private int height;         //高
    private Range xaxisRange;   //X Range
    private Range yaxisRange;   //y Range

    public Rectangle(int x, int y, int width, int height) {
        this.xpoint = x;
        this.ypoint = y;
        this.width = width;
        this.height = height;
        xaxisRange = Range.closedOpen(x, x + width);
        yaxisRange = Range.closedOpen(y, y + height);
    }
}
