package com.adventure.picturebackend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.adventure.picturebackend.model.entity.Space;
import org.apache.ibatis.annotations.Mapper;

/**
 *  映射层。
 *
 * @author Administrator
 * @since 2025-07-25
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

}
