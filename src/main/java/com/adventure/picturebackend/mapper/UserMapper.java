package com.adventure.picturebackend.mapper;

import com.adventure.picturebackend.model.entity.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author adventure
 * @since 2025-07-06
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
