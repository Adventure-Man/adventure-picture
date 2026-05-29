package com.adventure.picturebackend.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
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
     * 默认权限 login
     */
    public static final StpLogic defaultLogic = StpUtil.stpLogic;
    /**
     * 空间权限 管理space表所有的账号登录，权限
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);

}
