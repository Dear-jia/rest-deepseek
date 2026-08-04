package com.wenfeng.config;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 兼容已存在的数据库：把 staff.image_data 列从 oid（PostgreSQL 大对象）改为 bytea。
 * Hibernate 的 ddl-auto 不会修改已存在列的类型，这里在启动时做一次幂等迁移。
 */
@Component
public class PhotoColumnMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PhotoColumnMigrator.class);

    private final JdbcTemplate jdbcTemplate;

    public PhotoColumnMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select data_type from information_schema.columns "
                            + "where table_name = 'staff' and column_name = 'image_data'");
            if (!rows.isEmpty()) {
                String type = String.valueOf(rows.get(0).get("data_type")).toUpperCase();
                if ("OID".equals(type)) {
                    // PostgreSQL：大对象 oid -> bytea
                    jdbcTemplate.execute(
                            "ALTER TABLE staff ALTER COLUMN image_data TYPE bytea USING NULL::bytea");
                    log.info("已迁移 staff.image_data 列类型：oid -> bytea");
                } else if (type.startsWith("BINARY VARYING") || type.equals("VARBINARY")) {
                    // H2：长度受限的 VARBINARY(32600) -> 无长度限制的 BLOB
                    jdbcTemplate.execute(
                            "ALTER TABLE staff ALTER COLUMN image_data TYPE BLOB");
                    log.info("已迁移 staff.image_data 列类型：{} -> BLOB", type);
                }
            }
        } catch (Exception e) {
            log.warn("staff.image_data 列迁移跳过：{}", e.getMessage());
        }
    }
}
