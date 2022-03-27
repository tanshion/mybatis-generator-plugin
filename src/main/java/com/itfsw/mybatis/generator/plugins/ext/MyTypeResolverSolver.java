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
    public static final String KEY_NUMBER_DEFAULT = "NUMBER_DEFAULT";
    public static final String NUMBER_LENGTH = "NUMBER_LENGTH";
    public static final String SEQ_TYPE = "SEQ_TYPE";
    public static final String SEQ_END_STR = "SEQ_END_STR";

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = super.overrideDefaultType(column, defaultType);
        logger.info("overrideDefaultType --> ActualColumnName=={},JdbcTypeName=={},JdbcType=={},Scale={},Length=={}",
                column.getActualColumnName(), column.getJdbcTypeName(), column.getJdbcType(), column.getScale(), column.getLength());
        if (column.getJdbcType() == Types.TINYINT && properties.containsKey(KEY_TINYINT)) {
            answer = new FullyQualifiedJavaType(properties.getProperty(KEY_TINYINT));
        }
        if (column.getJdbcType() == Types.SMALLINT && properties.containsKey(KEY_SMALLINT)) {
            answer = new FullyQualifiedJavaType(properties.getProperty(KEY_SMALLINT));
        }
        return answer;
    }

    @Override
    protected FullyQualifiedJavaType calculateBigDecimalReplacement(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        if (column.getScale() > 0) {
            return defaultType;
        }

        if (properties.containsKey(SEQ_TYPE) && properties.containsKey(SEQ_END_STR)) {
            String[] endStrArr = properties.getProperty(SEQ_END_STR).split(",");
            for (String endStr : endStrArr) {
                if (column.getActualColumnName().endsWith(endStr)) {
                    return new FullyQualifiedJavaType(properties.getProperty(SEQ_TYPE));
                }
            }
        }

        if (properties.containsKey(NUMBER_LENGTH) && properties.getProperty(NUMBER_LENGTH).contains(column.getLength() + ",")) {
            String[] nlProperty = properties.getProperty(NUMBER_LENGTH).split(",");
            if (nlProperty.length == 2 && nlProperty[0].equals("" + column.getLength())) {
                return new FullyQualifiedJavaType(nlProperty[1]);
            }
        }

        if (column.getLength() > 18 || forceBigDecimals) {
            return defaultType;
        } else if (column.getLength() > 0 && column.getLength() <= 9) {
            return new FullyQualifiedJavaType(Integer.class.getName());
        } else {
            if (properties.containsKey(KEY_NUMBER_DEFAULT)) {
                return new FullyQualifiedJavaType(properties.getProperty(KEY_NUMBER_DEFAULT));
            } else {
                return new FullyQualifiedJavaType(Long.class.getName());
            }
        }
    }

}
