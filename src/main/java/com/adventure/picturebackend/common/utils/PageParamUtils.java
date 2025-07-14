package com.adventure.picturebackend.common.utils;

import com.adventure.picturebackend.common.constant.CommonNumberConstant;

public class PageParamUtils implements CommonNumberConstant {
    private PageParamUtils() {
    }

    /**
     * 校验并修正分页参数
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 修正后的分页参数数组 [pageNum, pageSize]
     */
    public static int[] validatePageParams(Integer pageNum, Integer pageSize) {
        // 默认值处理
        if (pageNum == null || pageNum < 1) {
            pageNum = DEFAULT_PAGE_NUM;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        return new int[]{pageNum, pageSize};
    }
}
