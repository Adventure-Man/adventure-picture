package com.adventure.picturebackend.model.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class PictureTableDef extends TableDef {

    /**
     * 图片 实体类。

 @author Administrator
 @since 2025-07-17
     */
    public static final PictureTableDef PICTURE = new PictureTableDef();

    /**
     * id
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 图片 url
     */
    public final QueryColumn URL = new QueryColumn(this, "url");

    /**
     * 图片名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 标签（JSON 数组）
     */
    public final QueryColumn TAGS = new QueryColumn(this, "tags");

    /**
     * 创建用户 id
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "userId");

    /**
     * 图片体积
     */
    public final QueryColumn PIC_SIZE = new QueryColumn(this, "picSize");

    /**
     * 分类
     */
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");

    /**
     * 编辑时间
     */
    public final QueryColumn EDIT_TIME = new QueryColumn(this, "editTime");

    /**
     * 是否删除
     */
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    /**
     * 图片宽高比例
     */
    public final QueryColumn PIC_SCALE = new QueryColumn(this, "picScale");

    /**
     * 图片宽度
     */
    public final QueryColumn PIC_WIDTH = new QueryColumn(this, "picWidth");

    /**
     * 图片格式
     */
    public final QueryColumn PIC_FORMAT = new QueryColumn(this, "picFormat");

    /**
     * 图片高度
     */
    public final QueryColumn PIC_HEIGHT = new QueryColumn(this, "picHeight");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "createTime");

    /**
     * 审核时间
     */
    public final QueryColumn REVIEW_TIME = new QueryColumn(this, "reviewTime");

    /**
     * 审核人 ID
     */
    public final QueryColumn REVIEWER_ID = new QueryColumn(this, "reviewerId");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "updateTime");

    /**
     * 简介
     */
    public final QueryColumn INTRODUCTION = new QueryColumn(this, "introduction");

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    public final QueryColumn REVIEW_STATUS = new QueryColumn(this, "reviewStatus");

    /**
     * 缩略图 url
     */
    public final QueryColumn THUMBNAIL_URL = new QueryColumn(this, "thumbnail_url");

    /**
     * 审核信息
     */
    public final QueryColumn REVIEW_MESSAGE = new QueryColumn(this, "reviewMessage");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, URL, NAME, TAGS, USER_ID, PIC_SIZE, CATEGORY, EDIT_TIME, IS_DELETE, PIC_SCALE, PIC_WIDTH, PIC_FORMAT, PIC_HEIGHT, CREATE_TIME, REVIEW_TIME, REVIEWER_ID, UPDATE_TIME, INTRODUCTION, REVIEW_STATUS, THUMBNAIL_URL, REVIEW_MESSAGE};

    public PictureTableDef() {
        super("", "picture");
    }

    private PictureTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PictureTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PictureTableDef("", "picture", alias));
    }

}
