//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itfsw.mybatis.generator.plugins.ext;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class MyTypeResolverSolver extends JavaTypeResolverDefaultImpl {
    protected static final Logger logger = LoggerFactory.getLogger(MyTypeResolverSolver.class);
    public static final String KEY_TINYINT = "TINYINT";
    public static final String KEY_SMALLINT = "SMALLINT";

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = super.overrideDefaultType(column, defaultType);

        if (column.getJdbcType() == Types.TINYINT && properties.containsKey(KEY_TINYINT)) {
            //logger.warn(column.getActualColumnName() + "的类型" + column.getJdbcTypeName() +
            //        "转换为" + properties.get(KEY_TINYINT));
            answer = new FullyQualifiedJavaType(properties.getProperty(KEY_TINYINT));
        }

        if (column.getJdbcType() == Types.SMALLINT) {
            //logger.warn(column.getActualColumnName() + "的类型" + column.getJdbcTypeName() +
            //        "转换为" + properties.get(KEY_SMALLINT));
            answer = new FullyQualifiedJavaType(properties.getProperty(KEY_SMALLINT));
        }

        return answer;
    }

    @Override
    protected FullyQualifiedJavaType calculateBigDecimalReplacement(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer;

        if (column.getScale() > 0 || column.getLength() > 18 || forceBigDecimals) {
            answer = defaultType;
        } else if (column.getLength() > 0 && column.getLength()<=9) {
            answer = new FullyQualifiedJavaType(Integer.class.getName());
        } else {
            answer = new FullyQualifiedJavaType(Long.class.getName());
        }

        return answer;
    }
}
