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
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
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

    private String modelTargetPackage;
    private String modelTargetProject;
    private String clientTargetPackage;
    private String clientTargetProject;


    private String baseMapper;
    private String tableName;
    private String tableIdType;
    private String keySequence;
    private String swagger = "false";
    private String generatedController = "false";
    private String constructorTargetPackage;
    private String serviceStyle = "simple";//simple pro
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
        keySequence = introspectedTable.getTableConfigurationProperty("keySequence");
        constructorTargetPackage = this.getProperties().getProperty("constructorTargetPackage");
        serviceTargetPackage = this.getProperties().getProperty("serviceTargetPackage");
        serviceStyle = this.getProperties().getProperty("serviceStyle");
        generatedController = introspectedTable.getTableConfigurationProperty("generatedController");
        generatedService = introspectedTable.getTableConfigurationProperty("generatedService");

        JavaModelGeneratorConfiguration javaModelGenCfg = context.getJavaModelGeneratorConfiguration();
        modelTargetPackage = javaModelGenCfg.getTargetPackage();
        modelTargetProject = javaModelGenCfg.getTargetProject();
        JavaClientGeneratorConfiguration javaCliGenCfg = context.getJavaClientGeneratorConfiguration();
        clientTargetProject = javaCliGenCfg.getTargetProject();
        clientTargetPackage = javaCliGenCfg.getTargetPackage();

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

        //String tableRemarks = introspectedTable.getRemarks();
        //if (null != tableRemarks && !"".equals(tableRemarks)) {
        //    topLevelClass.addAnnotation(String.format("//%s", tableRemarks));
        //}
        if ("true".equals(tableName)) {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableName");
            topLevelClass.addAnnotation(String.format("@TableName(value = \"%s\")", introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()));
        }

        if (null != keySequence && !"".equals(keySequence)) {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.KeySequence");
            topLevelClass.addAnnotation(String.format("@KeySequence(value = \"%s\")", keySequence));
        }
    }


    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        GeneratedJavaFile javaFile = new GeneratedJavaFile(interfaze, clientTargetProject, javaFormatter);
        FullyQualifiedTable fullyQualifiedTable = introspectedTable.getFullyQualifiedTable();
        // 添加泛型支持
        FullyQualifiedJavaType daoSuperType = new FullyQualifiedJavaType(baseMapper);
        FullyQualifiedJavaType modelJavaType = getModelJavaType(fullyQualifiedTable);
        interfaze.addImportedType(daoSuperType);
        interfaze.addImportedType(modelJavaType);
        daoSuperType.addTypeArgument(modelJavaType);
        interfaze.addSuperInterface(new FullyQualifiedJavaType(daoSuperType.getShortName()));
        if (fileExists(javaFile)) {
            return false;
        }
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    private FullyQualifiedJavaType getModelJavaType(FullyQualifiedTable fullyQualifiedTable) {
        StringBuilder sb = new StringBuilder();
        sb.append(modelTargetPackage);
        sb.append('.');
        if (StringUtility.stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
            sb.append(fullyQualifiedTable.getDomainObjectSubPackage());
            sb.append('.');
        }
        sb.append(fullyQualifiedTable.getDomainObjectName());
        return new FullyQualifiedJavaType(sb.toString());
    }

    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<>();
        FullyQualifiedJavaType modelJavaType = getModelJavaType(introspectedTable.getFullyQualifiedTable());
        List<GeneratedJavaFile> javaFiles = introspectedTable.getGeneratedJavaFiles();
        for (GeneratedJavaFile javaFile : javaFiles) {
            CompilationUnit unit = javaFile.getCompilationUnit();
            FullyQualifiedJavaType baseModelJavaType = unit.getType();
            if (modelJavaType.getFullyQualifiedName().equals(baseModelJavaType.getFullyQualifiedName())) {
                String shortName = baseModelJavaType.getFullyQualifiedName()
                        .replace(modelTargetPackage, "");
                //GeneratedJavaFile mapper = generatedMapper(tableConfiguration.getMapperName(), baseModelJavaType, shortName);
                //if (!fileExists(mapper)) {
                //    generatedJavaFiles.add(mapper);
                //}

                if (StringUtility.stringHasValue(generatedController) && !"false".equals(generatedController)) {
                    GeneratedJavaFile controller = generatedController(baseModelJavaType, shortName);
                    if (!fileExists(controller)) {
                        generatedJavaFiles.add(controller);
                    }
                }

                if (StringUtility.stringHasValue(generatedService) && !"false".equals(generatedService)) {
                    if ("simple".equals(serviceStyle)) {
                        GeneratedJavaFile simpleService = generatedSimpleService(baseModelJavaType, shortName);
                        if (!fileExists(simpleService)) {
                            generatedJavaFiles.add(simpleService);
                        }
                    } else {
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
            }
        }
        return generatedJavaFiles;
    }


    private boolean fileExists(GeneratedJavaFile javaFile) {
        try {
            File fileDir = shellCallback.getDirectory(javaFile.getTargetProject(), javaFile.getTargetPackage());
            File file = new File(fileDir, javaFile.getFileName());
            // 文件不存在
            return file.exists();
        } catch (ShellException e) {
            e.printStackTrace();
        }
        return true;
    }

    private GeneratedJavaFile generatedService(FullyQualifiedJavaType baseModelJavaType, String shortName) {
        String name;
        if (StringUtility.isTrue(generatedService)) {
            name = shortName + "Service";
        } else {
            name = "." + generatedService;
        }

        Interface anInterface = new Interface(serviceTargetPackage + name);
        anInterface.setVisibility(JavaVisibility.PUBLIC);

        return new GeneratedJavaFile(anInterface, modelTargetProject, javaFormatter);
    }

    private GeneratedJavaFile generatedServiceImpl(GeneratedJavaFile JavaFile) {
        FullyQualifiedJavaType serviceType = JavaFile.getCompilationUnit().getType();
        TopLevelClass topLevelClass = new TopLevelClass(serviceType.getPackageName() + ".impl." + serviceType.getShortName() + "Impl");
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

        return new GeneratedJavaFile(topLevelClass, modelTargetProject, javaFormatter);
    }

    private GeneratedJavaFile generatedSimpleService(FullyQualifiedJavaType baseModelJavaType, String shortName) {
        String name;
        if (StringUtility.isTrue(generatedService)) {
            name = shortName + "Service";
        } else {
            name = "." + generatedService;
        }

        TopLevelClass topLevelClass = new TopLevelClass(serviceTargetPackage + name);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 添加泛型支持
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
        topLevelClass.addAnnotation("@Slf4j");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.RequiredArgsConstructor"));
        topLevelClass.addAnnotation("@RequiredArgsConstructor");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
        topLevelClass.addAnnotation("@Service");

        return new GeneratedJavaFile(topLevelClass, modelTargetProject, javaFormatter);
    }

    private GeneratedJavaFile generatedController(FullyQualifiedJavaType baseModelJavaType, String shortName) {
        String name;
        if (StringUtility.isTrue(generatedController)) {
            name = shortName + "Controller";
        } else {
            name = "." + generatedController;
        }

        TopLevelClass topLevelClass = new TopLevelClass(constructorTargetPackage + name);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // 添加泛型支持
        if (StringUtility.stringHasValue("true")){
            topLevelClass.addImportedType(new FullyQualifiedJavaType("io.swagger.annotations.Api"));
            topLevelClass.addAnnotation(String.format("@Api(tags = \"%s\")", topLevelClass.getType().getShortName()));
        }
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
        topLevelClass.addAnnotation("@Slf4j");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("lombok.RequiredArgsConstructor"));
        topLevelClass.addAnnotation("@RequiredArgsConstructor");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
        topLevelClass.addAnnotation("@RestController");
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
        topLevelClass.addAnnotation(String.format("@RequestMapping(\"%s\")", "/"));

        return new GeneratedJavaFile(topLevelClass, modelTargetProject, javaFormatter);

    }

    private GeneratedJavaFile generatedMapper(String mapperName, FullyQualifiedJavaType baseModelJavaType, String shortName) {
        Interface anInterface;
        if (StringUtility.stringHasValue(mapperName)) {
            anInterface = new Interface(clientTargetPackage + "." + mapperName);
        } else {
            anInterface = new Interface(clientTargetPackage + shortName + "Mapper");
        }
        anInterface.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType daoSuperType = new FullyQualifiedJavaType(baseMapper);
        // 添加泛型支持
        anInterface.addImportedType(baseModelJavaType);
        anInterface.addImportedType(daoSuperType);
        daoSuperType.addTypeArgument(baseModelJavaType);
        anInterface.addSuperInterface(new FullyQualifiedJavaType(daoSuperType.getShortName()));

        return new GeneratedJavaFile(anInterface, modelTargetProject, javaFormatter);
    }

    @Override
    public boolean clientBasicCountMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientBasicDeleteMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientBasicInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientBasicUpdateMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return super.clientSelectAllMethodGenerated(method, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }
}
