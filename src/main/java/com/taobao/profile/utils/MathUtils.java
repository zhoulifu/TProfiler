/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.utils;

import java.math.BigDecimal;

/**
 * 数学计算工具方法
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
public class MathUtils {

    private MathUtils() {
    }

    public static long divideRoundHalfUp(long dividend, long divisor) {
        return BigDecimal.valueOf(dividend).divide(BigDecimal.valueOf(divisor), 0,
                                                   BigDecimal.ROUND_HALF_UP).longValue();
    }
}
