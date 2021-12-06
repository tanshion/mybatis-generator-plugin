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
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * ---------------------------------------------------------------------------
 * Swagger 插件
 * ---------------------------------------------------------------------------
 *
 * @author: tanshion
 * @time: 2020-05-25 17:19:24
 * ---------------------------------------------------------------------------
 */
public class SwaggerPlugin extends BasePlugin {


    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable 参数
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
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
        this.generateSwagger(topLevelClass, introspectedTable);
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    private void generateSwagger(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");

        List<Field> fields = topLevelClass.getFields();
        for (Field field : fields) {
            IntrospectedColumn column = getColumn(field, introspectedTable);
            String remarks = column.getRemarks();
            if (null != remarks && !"".equals(remarks)) {
                remarks = remarks.replace(System.getProperty("line.separator"), ";");
                if (remarks.contains("\r\n")) {
                    remarks = remarks.replace("\r\n",";");
                }else if (remarks.contains("\n")) {
                    remarks = remarks.replace("\n",";");
                }else if (remarks.contains("\r")) {
                    remarks = remarks.replace("\r",";");
                }
                field.addAnnotation(String.format("@ApiModelProperty(value = \"%s\")", remarks));
            }
        }

    }

    private IntrospectedColumn getColumn(Field field, IntrospectedTable introspectedTable) {
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        for (IntrospectedColumn column : allColumns) {
            if (field.getName().equals(column.getJavaProperty())) {
                return column;
            }
        }
        return null;
    }

}
