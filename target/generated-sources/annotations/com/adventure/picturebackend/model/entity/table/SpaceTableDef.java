package com.adventure.picturebackend.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SpaceTableDef extends TableDef {

    /**
     * 实体类。

 @author Administrator
 @since 2025-07-25
     */
    public static final SpaceTableDef SPACE = new SpaceTableDef();

    /**
     * 空间id
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 创建用户id
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "userId");

    /**
     * 空间图片的最大总大小
     */
    public final QueryColumn MAX_SIZE = new QueryColumn(this, "maxSize");

    /**
     * 编辑时间
     */
    public final QueryColumn EDIT_TIME = new QueryColumn(this, "editTime");

    /**
     * 是否删除
     */
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    /**
     * 当前空间下的图片数量
     */
    public final QueryColumn MAX_COUNT = new QueryColumn(this, "maxCount");

    /**
     * 空间名称
     */
    public final QueryColumn SPACE_NAME = new QueryColumn(this, "spaceName");

    /**
     * 当前空间的总大小
     */
    public final QueryColumn TOTAL_SIZE = new QueryColumn(this, "totalSize");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "createTime");

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    public final QueryColumn SPACE_LEVEL = new QueryColumn(this, "spaceLevel");

    /**
     * 当前空间图片的总数量
     */
    public final QueryColumn TOTAL_COUNT = new QueryColumn(this, "totalCount");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "updateTime");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, USER_ID, MAX_SIZE, EDIT_TIME, IS_DELETE, MAX_COUNT, SPACE_NAME, TOTAL_SIZE, CREATE_TIME, SPACE_LEVEL, TOTAL_COUNT, UPDATE_TIME};

    public SpaceTableDef() {
        super("", "space");
    }

    private SpaceTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SpaceTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SpaceTableDef("", "space", alias));
    }

}
