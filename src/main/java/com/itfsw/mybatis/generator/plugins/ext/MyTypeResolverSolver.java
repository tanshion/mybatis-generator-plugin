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

import java.util.regex.Pattern;

public class MyTypeResolverSolver extends JavaTypeResolverDefaultImpl {
    protected static final Logger logger = LoggerFactory.getLogger(MyTypeResolverSolver.class);


    // <property name="mapper_integer_type" value="TINYINT,SMALLINT,NUMBER,NUMBER38"/>
    public static final String MAPPER_INTEGER_TYPE = "mapper_integer_type";
    // <property name="mapper_long_type" value="number19,NUMBER20"/>
    public static final String MAPPER_LONG_TYPE = "mapper_long_type";
    // <property name="mapper_integer_regex" value="(?i)[\w]+(page$|page_no$)"/>
    public static final String MAPPER_INTEGER_REGEX = "mapper_integer_regex";
    // <property name="mapper_long_regex" value="(?i)[\w]+(seq$|seq_no$)"/>
    public static final String MAPPER_LONG_REGEX = "mapper_long_regex";

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = super.overrideDefaultType(column, defaultType);
        logger.info("overrideDefaultType --> ActualColumnName=={},JdbcTypeName=={},JdbcType=={},Scale={},Length=={}",
                column.getActualColumnName(), column.getJdbcTypeName(), column.getJdbcType(), column.getScale(), column.getLength());
        if (properties.containsKey(MAPPER_INTEGER_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_INTEGER_TYPE).toUpperCase();
            if (typeStr.contains(column.getJdbcTypeName())) {
                answer = new FullyQualifiedJavaType("java.lang.Integer");
            }
        }
        if (properties.containsKey(MAPPER_LONG_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_LONG_TYPE).toUpperCase();
            if (typeStr.contains(column.getJdbcTypeName())) {
                answer = new FullyQualifiedJavaType("java.lang.Long");
            }
        }
        return answer;
    }

    @Override
    protected FullyQualifiedJavaType calculateBigDecimalReplacement(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        if (column.getScale() > 0) {
            return defaultType;
        }

        if (properties.containsKey(MAPPER_INTEGER_REGEX)) {
            String pattern = properties.getProperty(MAPPER_INTEGER_REGEX);
            if (Pattern.matches(pattern, column.getActualColumnName())) {
                return new FullyQualifiedJavaType("java.lang.Integer");
            }
        }

        if (properties.containsKey(MAPPER_LONG_REGEX)) {
            String pattern = properties.getProperty(MAPPER_LONG_REGEX);
            if (Pattern.matches(pattern, column.getActualColumnName())) {
                return new FullyQualifiedJavaType("java.lang.Long");
            }
        }

        if (properties.containsKey(MAPPER_INTEGER_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_INTEGER_TYPE).toUpperCase() + ",";
            int length = column.getLength();
            String type = length == 0 ? "NUMBER," : "NUMBER" + length + ",";
            if (typeStr.contains(type)) {
                return new FullyQualifiedJavaType("java.lang.Integer");
            }
        }

        if (properties.containsKey(MAPPER_LONG_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_INTEGER_TYPE).toUpperCase() + ",";
            int length = column.getLength();
            String type = length == 0 ? "NUMBER," : "NUMBER" + length + ",";
            if (typeStr.contains(type)) {
                return new FullyQualifiedJavaType("java.lang.Long");
            }
        }

        if (column.getLength() > 0 && column.getLength() <= 9) {
            return new FullyQualifiedJavaType(Integer.class.getName());
        }
        return defaultType;
    }

}
