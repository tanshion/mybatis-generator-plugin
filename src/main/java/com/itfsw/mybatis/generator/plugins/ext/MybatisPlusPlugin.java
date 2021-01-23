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
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

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
    private JavaFormatter javaFormatter;

    private String baseMapper;
    private String tableName;
    private String tableIdType;
    private String keySequence;
    private String targetPackage;
    private String targetProject;
    private String mapperTargetPackage;
    private String generatedController = "false";
    private String constructorTargetPackage;
    private String generatedService = "false";
    private String serviceTargetPackage;

    /**
     * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
     *
     * @param introspectedTable 参数
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        shellCallback = new DefaultShellCallback(false);
        javaFormatter = context.getJavaFormatter();
        baseMapper = this.getProperties().getProperty("baseMapper");
        tableName = this.getProperties().getProperty("tableName");
        tableIdType = this.getProperties().getProperty("tableIdType");
        constructorTargetPackage = this.getProperties().getProperty("constructorTargetPackage");
        serviceTargetPackage = this.getProperties().getProperty("serviceTargetPackage");
        keySequence = introspectedTable.getTableConfigurationProperty("keySequence");
        generatedController = introspectedTable.getTableConfigurationProperty("generatedController");
        generatedService = introspectedTable.getTableConfigurationProperty("generatedService");

        JavaModelGeneratorConfiguration javaModelGenCfg = context.getJavaModelGeneratorConfiguration();
        targetPackage = javaModelGenCfg.getTargetPackage();
        targetProject = javaModelGenCfg.getTargetProject();
        JavaClientGeneratorConfiguration javaCliGenCfg = context.getJavaClientGeneratorConfiguration();
        mapperTargetPackage = javaCliGenCfg.getTargetPackage();

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
            if (StringUtility.stringHasValue(tableIdType)) {
                topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.IdType");
            }
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableId");

            List<Field> fields = topLevelClass.getFields();
            for (Field field : fields) {
                if (field.getName().equals(introspectedColumn.getJavaProperty())) {
                    if (StringUtility.stringHasValue(tableIdType)) {
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
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<>();
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        List<GeneratedJavaFile> javaFiles = introspectedTable.getGeneratedJavaFiles();
        try {
            for (GeneratedJavaFile javaFile : javaFiles) {
                CompilationUnit unit = javaFile.getCompilationUnit();
                FullyQualifiedJavaType baseModelJavaType = unit.getType();
                String shortName = baseModelJavaType.getFullyQualifiedNameWithoutTypeParameters()
                        .replace(targetPackage, "");

                GeneratedJavaFile mapper = generatedMapper(tableConfiguration.getMapperName(), baseModelJavaType, shortName);
                if (!fileExists(mapper)) {
                    generatedJavaFiles.add(mapper);
                }
                if (StringUtility.isTrue(generatedController)) {
                    GeneratedJavaFile controller = generatedController(baseModelJavaType, shortName);
                    if (!fileExists(controller)) {
                        generatedJavaFiles.add(controller);
                    }
                }
                if (StringUtility.isTrue(generatedService)) {
                    GeneratedJavaFile service = generatedService(baseModelJavaType, shortName);
                    if (!fileExists(service)) {
                        generatedJavaFiles.add(service);
                    }
                    GeneratedJavaFile serviceImpl = generatedServiceImpl(service);
                    if (!fileExists(serviceImpl)) {
                        generatedJavaFiles.add(serviceImpl);
                    }
                }


            }
        } catch (ShellException e) {
            e.printStackTrace();
        }
        return generatedJavaFiles;
    }


    private boolean fileExists(GeneratedJavaFile javaFile) throws ShellException {
        File fileDir = shellCallback.getDirectory(javaFile.getTargetProject(), javaFile.getTargetPackage());
        File file = new File(fileDir, javaFile.getFileName());
        // 文件不存在
        return file.exists();
    }

    private GeneratedJavaFile generatedService(FullyQualifiedJavaType baseModelJavaType, String shortName) {
        Interface anInterface = new Interface(serviceTargetPackage + shortName + "Service");
        anInterface.setVisibility(JavaVisibility.PUBLIC);

        return new GeneratedJavaFile(anInterface, targetProject, javaFormatter);
    }

    private GeneratedJavaFile generatedServiceImpl(GeneratedJavaFile JavaFile) {
        FullyQualifiedJavaType serviceType = JavaFile.getCompilationUnit().getType();
        TopLevelClass topLevelClass = new TopLevelClass(serviceType.getPackageName()+".impl."+serviceType.getShortName()+"Impl");
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 添加泛型支持
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
        topLevelClass.addAnnotation("@Slf4j");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.RequiredArgsConstructor"));
        topLevelClass.addAnnotation("@RequiredArgsConstructor");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
        topLevelClass.addAnnotation("@Service");

        topLevelClass.addImportedType(serviceType);
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType(serviceType.getShortName()));

        return new GeneratedJavaFile(topLevelClass, targetProject, javaFormatter);
    }

    private GeneratedJavaFile generatedController(FullyQualifiedJavaType baseModelJavaType, String shortName) throws ShellException {
        TopLevelClass topLevelClass = new TopLevelClass(constructorTargetPackage + shortName + "Controller");
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 添加泛型支持
        topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.Api"));
        topLevelClass.addAnnotation(String.format("@Api(tags = \"%s\")", topLevelClass.getType().getShortName()));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
        topLevelClass.addAnnotation("@Slf4j");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.RequiredArgsConstructor"));
        topLevelClass.addAnnotation("@RequiredArgsConstructor");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
        topLevelClass.addAnnotation("@RestController");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
        topLevelClass.addAnnotation(String.format("@RequestMapping(\"%s\")", "/"));

        return new GeneratedJavaFile(topLevelClass, targetProject, javaFormatter);

    }

    private GeneratedJavaFile generatedMapper(String mapperName, FullyQualifiedJavaType baseModelJavaType, String shortName) throws ShellException {
        Interface anInterface;
        if (StringUtility.stringHasValue(mapperName)) {
            anInterface = new Interface(mapperTargetPackage + "." + mapperName);
        } else {
            anInterface = new Interface(mapperTargetPackage + shortName + "Mapper");
        }
        anInterface.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType daoSuperType = new FullyQualifiedJavaType(baseMapper);
        // 添加泛型支持
        anInterface.addImportedType(baseModelJavaType);
        anInterface.addImportedType(daoSuperType);
        daoSuperType.addTypeArgument(baseModelJavaType);
        anInterface.addSuperInterface(new FullyQualifiedJavaType(daoSuperType.getShortName()));

        return new GeneratedJavaFile(anInterface, targetProject, javaFormatter);
    }

}
