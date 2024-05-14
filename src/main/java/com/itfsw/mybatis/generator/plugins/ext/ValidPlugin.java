/*
 * Copyright (c) 2018.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins.ext;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * MybatisPlus 插件
 * ---------------------------------------------------------------------------
 *
 * @author: tanshion
 * @time: 2020-05-25 17:19:24
 * ---------------------------------------------------------------------------
 */
public class ValidPlugin extends BasePlugin {

    private String dbType;
    private String validMessage;
    private String validLength;
    private String validDigits;
    private String validByteLength;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable 参数
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);

        validMessage = this.getProperties().getProperty("valid_message");
        validLength = this.getProperties().getProperty("valid_length");
        validDigits = this.getProperties().getProperty("valid_digits");
        validByteLength = this.getProperties().getProperty("valid_byte_length");
        if (this.getContext().getJdbcConnectionConfiguration().getDriverClass().contains("mysql")) {
            dbType = "mysql";
        } else if (this.getContext().getJdbcConnectionConfiguration().getDriverClass().contains("oracle")) {
            dbType = "oracle";
        } else if (this.getContext().getJdbcConnectionConfiguration().getDriverClass().contains("postgresql")) {
            dbType = "postgresql";
        }

    }


    /**
     * Model 生成
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param topLevelClass     参数
     * @param introspectedTable 参数
     * @return 返回
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.generatePlus(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    private void generatePlus(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        List<Field> fields = topLevelClass.getFields();
        List<IntrospectedColumn> allColumn = introspectedTable.getAllColumns();
        for (IntrospectedColumn column : allColumn) {
            int jdbcType = column.getJdbcType();
            String javaProperty = column.getJavaProperty();
            Field field = filterField(fields, javaProperty);
            if (field == null) {
                continue;
            }
            FullyQualifiedJavaType fullyQualifiedJavaType = column.getFullyQualifiedJavaType();
            String typeName = fullyQualifiedJavaType.getFullyQualifiedName();
            String msg = "";
            if (StringUtility.stringHasValue(validMessage)) {
                msg = String.format(", message = \"%s\"", validMessage);
            }
            if (column.isStringColumn()) {
                int length = column.getLength();
                if ("oracle".equals(dbType)) {
                    if (Types.NCLOB == jdbcType || Types.NVARCHAR == jdbcType || Types.NCHAR == jdbcType) {
                        topLevelClass.addImportedType(validByteLength);
                        field.addAnnotation(String.format("@ByteLength(max = %s, charset = \"UTF-8\"%s)", length, msg));
                    } else {
                        topLevelClass.addImportedType(validByteLength);
                        field.addAnnotation(String.format("@ByteLength(max = %s%s)", length, msg));
                    }
                } else {
                    topLevelClass.addImportedType(validLength);
                    field.addAnnotation(String.format("@Length(max = %s%s)", length, msg));
                }
            }

            if (typeName.equals(Integer.class.getName())
                || typeName.equals(Long.class.getName())
                || typeName.equals(Short.class.getName())) {
                int length = column.getLength();
                topLevelClass.addImportedType(validDigits);
                field.addAnnotation(String.format("@Digits(integer = %s, fraction = 0%s)", length, msg));
            } else if (typeName.equals(BigDecimal.class.getName())
                       || typeName.equals(Double.class.getName())
                       || typeName.equals(Float.class.getName())) {
                int length = column.getLength();
                int scale = column.getScale();
                topLevelClass.addImportedType(validDigits);
                field.addAnnotation(String.format("@Digits(integer = %s, fraction = %s%s)", length, scale, msg));
            }


        }

    }

    private Field filterField(List<Field> fields, String javaProperty) {
        for (Field field : fields) {
            if (field.getName().equals(javaProperty)) {
                return field;
            }
        }
        return null;
    }
}
