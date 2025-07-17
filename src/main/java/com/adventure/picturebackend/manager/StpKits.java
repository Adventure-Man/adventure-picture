package com.adventure.picturebackend.manager;

import cn.dev33.satoken.stp.StpLogic;
import org.springframework.stereotype.Component;

/**
 * @author Adventure
 * @date 2025/7/16
 * @description TODO
 */
@Component
public class StpKits {
    /**
     * 空间类型
     */
    public static final String SPACE_TYPE = "space";
    /**
     * 空间权限
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);

}
