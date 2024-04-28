package com.itfsw.mybatis.generator.plugins.ext;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Set;

public class MyCommentGenerator extends DefaultCommentGenerator {
    protected static final Logger logger = LoggerFactory.getLogger(MyTypeResolverSolver.class);

    private Properties properties = new Properties();
    private boolean suppressDate = false;
    private boolean suppressAllComments = false;
    private boolean addRemarkComments = false;
    private SimpleDateFormat dateFormat;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.properties.putAll(properties);
        this.suppressDate = StringUtility.isTrue(properties.getProperty("suppressDate"));
        this.suppressAllComments = StringUtility.isTrue(properties.getProperty("suppressAllComments"));
        this.addRemarkComments = StringUtility.isTrue(properties.getProperty("addRemarkComments"));
        String dateFormatString = properties.getProperty("dateFormat");
        if (StringUtility.stringHasValue(dateFormatString)) {
            this.dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        return;
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }

        innerClass.addJavaDocLine("/**"); //$NON-NLS-1$
        String remarks = introspectedTable.getRemarks();
        String[] remarkLines = remarks.split(System.getProperty("line.separator"));
        if (remarkLines.length<2){
            if (remarks.contains("\r\n")) {
                remarkLines = remarks.split("\r\n");
            }else if (remarks.contains("\n")) {
                remarkLines = remarks.split("\n");
            }else if (remarks.contains("\r")) {
                remarkLines = remarks.split("\r");
            }
        }
        for (String remarkLine : remarkLines) {
            innerClass.addJavaDocLine(" * " + remarkLine);
        }
        addJavadocTag(innerClass, false);
        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        return;
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (suppressAllComments  || !addRemarkComments) {
            return;
        }
        String remarks = introspectedTable.getRemarks();
        if (!StringUtility.stringHasValue(remarks)) {
            return;
        }
        logger.info("--->"+remarks);
        topLevelClass.addJavaDocLine("/**"); //$NON-NLS-1$
        String[] remarkLines = remarks.split(System.getProperty("line.separator"));
        if (remarkLines.length<2){
            if (remarks.contains("\r\n")) {
                remarkLines = remarks.split("\r\n");
            }else if (remarks.contains("\n")) {
                remarkLines = remarks.split("\n");
            }else if (remarks.contains("\r")) {
                remarkLines = remarks.split("\r");
            }
        }
        for (String remarkLine : remarkLines) {
            topLevelClass.addJavaDocLine(" * " + remarkLine);
        }
        topLevelClass.addJavaDocLine(" */"); //$NON-NLS-1$
    }

    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        return;
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!this.suppressAllComments) {
            String remarks = introspectedColumn.getRemarks();
            if (this.addRemarkComments && StringUtility.stringHasValue(remarks)) {
                field.addJavaDocLine("/**");
                String[] remarkLines = remarks.split(System.getProperty("line.separator"));
                if (remarkLines.length<2){
                    if (remarks.contains("\r\n")) {
                        remarkLines = remarks.split("\r\n");
                    }else if (remarks.contains("\n")) {
                        remarkLines = remarks.split("\n");
                    }else if (remarks.contains("\r")) {
                        remarkLines = remarks.split("\r");
                    }
                }
                for (String remarkLine : remarkLines) {
                    field.addJavaDocLine(" * " + remarkLine);
                }
                field.addJavaDocLine(" */");
            }
        }
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        return;
    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        return;
    }

    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        return;
    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        return;
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        return;
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        return;
    }


}
