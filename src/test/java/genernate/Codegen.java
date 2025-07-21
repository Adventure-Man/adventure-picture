package genernate;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Codegen {

    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1/mp?characterEncoding=utf-8");
        dataSource.setUsername("root");
        dataSource.setPassword("24994627");
        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle();
        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);
        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();
        //设置根包
        globalConfig.setBasePackage("generated");
        //设置表前缀和只生成哪些表
        // globalConfig.setTablePrefix("tz_");
        globalConfig.setGenerateTable("picture");
        //设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        //设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
        globalConfig.setEntityJdkVersion(17);

        //设置生成 mapper
//        globalConfig.setMapperGenerateEnable(true);
//        globalConfig.setServiceGenerateEnable(true);
//        globalConfig.setServiceImplGenerateEnable(true);
//        globalConfig.setControllerGenerateEnable(true);
        //可以单独配置某个列
//        ColumnConfig columnConfig = new ColumnConfig();
//        columnConfig.setColumnName("tenant_id");
//        columnConfig.setLarge(true);
//        columnConfig.setVersion(true);
//        globalConfig.setColumnConfig("tb_account", columnConfig);
        return globalConfig;
    }
}