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


    //参考类 java.sql.Type<property name="mapper_integer_type" value="3-4-0"/>
    public static final String MAPPER_INTEGER_TYPE = "mapper_integer_type";
    //参考类 java.sql.Type<property name="mapper_long_type" value="3-11-0"/>
    public static final String MAPPER_LONG_TYPE = "mapper_long_type";
    //<property name="mapper_integer_regex" value="(?i)[\w]+(page$|page_no$)"/>
    public static final String MAPPER_INTEGER_REGEX = "mapper_integer_regex";
    //<property name="mapper_long_regex" value="(?i)[\w]+(seq$|seq_no$)"/>
    public static final String MAPPER_LONG_REGEX = "mapper_long_regex";


    @Override
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        logger.info("overrideDefaultType --> {}", column.toString());
        return super.overrideDefaultType(column, defaultType);
    }

    protected FullyQualifiedJavaType calculateBigDecimalReplacement(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = super.overrideDefaultType(column, defaultType);
        String tls = column.getJdbcType() + "-" + column.getLength() + "-" + column.getScale();

        if (properties.containsKey(MAPPER_INTEGER_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_INTEGER_TYPE);
            if (typeStr.contains(tls)) {
                answer = new FullyQualifiedJavaType("java.lang.Integer");
            }
        }

        if (properties.containsKey(MAPPER_LONG_TYPE)) {
            String typeStr = properties.getProperty(MAPPER_LONG_TYPE);
            if (typeStr.contains(tls)) {
                answer = new FullyQualifiedJavaType("java.lang.Long");
            }
        }

        if (properties.containsKey(MAPPER_INTEGER_REGEX)) {
            String pattern = properties.getProperty(MAPPER_INTEGER_REGEX);
            if (Pattern.matches(pattern, column.getActualColumnName())) {
                answer = new FullyQualifiedJavaType("java.lang.Integer");
            }
        }

        if (properties.containsKey(MAPPER_LONG_REGEX)) {
            String pattern = properties.getProperty(MAPPER_LONG_REGEX);
            if (Pattern.matches(pattern, column.getActualColumnName())) {
                answer = new FullyQualifiedJavaType("java.lang.Long");
            }
        }
        return answer;
    }

}
