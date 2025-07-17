package generated.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class UserTableDef extends TableDef {

    /**
     * 用户 实体类。

 @author Administrator
 @since 2025-07-06
     */
    public static final UserTableDef USER = new UserTableDef();

    /**
     * id
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 编辑时间
     */
    public final QueryColumn EDIT_TIME = new QueryColumn(this, "editTime");

    /**
     * 是否删除
     */
    public final QueryColumn IS_DELETE = new QueryColumn(this, "isDelete");

    /**
     * 用户昵称
     */
    public final QueryColumn USER_NAME = new QueryColumn(this, "userName");

    /**
     * 用户角色：user/admin
     */
    public final QueryColumn USER_ROLE = new QueryColumn(this, "userRole");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "createTime");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "updateTime");

    /**
     * 用户头像
     */
    public final QueryColumn USER_AVATAR = new QueryColumn(this, "userAvatar");

    /**
     * 账号
     */
    public final QueryColumn USER_ACCOUNT = new QueryColumn(this, "userAccount");

    /**
     * 用户简介
     */
    public final QueryColumn USER_PROFILE = new QueryColumn(this, "userProfile");

    /**
     * 密码
     */
    public final QueryColumn USER_PASSWORD = new QueryColumn(this, "userPassword");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, EDIT_TIME, IS_DELETE, USER_NAME, USER_ROLE, CREATE_TIME, UPDATE_TIME, USER_AVATAR, USER_ACCOUNT, USER_PROFILE, USER_PASSWORD};

    public UserTableDef() {
        super("", "user");
    }

    private UserTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public UserTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new UserTableDef("", "user", alias));
    }

}
