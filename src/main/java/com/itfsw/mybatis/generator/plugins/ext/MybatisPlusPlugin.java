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
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
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
public class MybatisPlusPlugin extends BasePlugin {

    private ShellCallback shellCallback;

    private final String BASE_MAPPER_TYPE = "baseMapper";
    private final String TABLE_NAME = "tableName";
    private final String KEY_SEQUENCE = "keySequence";
    private final String TABLE_ID_TYPE = "tableIdType";
    private String baseMapper;
    private String tableName;
    private String tableIdType;
    private String keySequence;


    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable 参数
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        shellCallback = new DefaultShellCallback(false);
        baseMapper = this.getProperties().getProperty(BASE_MAPPER_TYPE);
        tableName = this.getProperties().getProperty(TABLE_NAME);
        tableIdType = this.getProperties().getProperty(TABLE_ID_TYPE);
        keySequence = introspectedTable.getTableConfigurationProperty(KEY_SEQUENCE);
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
        List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
        if (primaryKeyColumns.size() == 1) {
            IntrospectedColumn introspectedColumn = primaryKeyColumns.get(0);
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.IdType");
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableId");

            List<Field> fields = topLevelClass.getFields();
            for (Field field : fields) {
                if (field.getName().equals(introspectedColumn.getJavaProperty())) {
                    if (null != tableIdType && !"".equals(tableIdType)) {
                        field.addAnnotation(String.format("@TableId(type = IdType.%s)", tableIdType));
                    } else {
                        field.addAnnotation("@TableId");
                    }
                }
            }
        }

        String tableRemarks = introspectedTable.getRemarks();
        if (null != tableRemarks && !"".equals(tableRemarks)) {
            topLevelClass.addAnnotation(String.format("//%s", tableRemarks));
        }
        if ("true".equals(tableName)) {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableName");
            topLevelClass.addAnnotation(String.format("@TableName(value = \"%s\")", introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()));
        }

        if (null != keySequence && !"".equals(keySequence)) {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.KeySequence");
            topLevelClass.addAnnotation(String.format("@KeySequence(value = \"%s\")", keySequence));
        }
    }


    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        JavaFormatter javaFormatter = context.getJavaFormatter();
        String daoTargetDir = "src/main/java";
        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<>();
        try {
            for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {
                CompilationUnit unit = javaFile.getCompilationUnit();
                FullyQualifiedJavaType baseModelJavaType = unit.getType();
                String shortName = baseModelJavaType.getShortName();
                String daoTargetPackage = javaFile.getTargetPackage().replace(".entity", ".mapper");
                Interface mapperInterface = new Interface(
                        daoTargetPackage + "." + shortName + "Mapper");
                mapperInterface.setVisibility(JavaVisibility.PUBLIC);
                FullyQualifiedJavaType daoSuperType = new FullyQualifiedJavaType(baseMapper);
                // 添加泛型支持
                mapperInterface.addImportedType(baseModelJavaType);
                mapperInterface.addImportedType(daoSuperType);
                daoSuperType.addTypeArgument(baseModelJavaType);
                mapperInterface.addSuperInterface(new FullyQualifiedJavaType(daoSuperType.getShortName()));

                GeneratedJavaFile mapperJavaFile = new GeneratedJavaFile(mapperInterface, daoTargetDir, javaFormatter);
                File mapperDir = shellCallback.getDirectory(daoTargetDir, daoTargetPackage);
                File mapperFile = new File(mapperDir, mapperJavaFile.getFileName());
                // 文件不存在
                if (!mapperFile.exists()) {
                    mapperJavaFiles.add(mapperJavaFile);
                }

            }
        } catch (ShellException e) {
            e.printStackTrace();
        }
        return mapperJavaFiles;
    }

}
