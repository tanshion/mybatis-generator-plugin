//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itfsw.mybatis.generator.plugins.ext;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameJavaMapperPlugin extends PluginAdapter {
    private String searchString;
    private String replaceString;
    private Pattern pattern;

    public RenameJavaMapperPlugin() {
    }

    public boolean validate(List<String> warnings) {
        this.searchString = this.properties.getProperty("searchString");
        this.replaceString = this.properties.getProperty("replaceString");
        boolean valid = StringUtility.stringHasValue(this.searchString) && StringUtility.stringHasValue(this.replaceString);
        if (valid) {
            this.pattern = Pattern.compile(this.searchString);
        } else {
            if (!StringUtility.stringHasValue(this.searchString)) {
                warnings.add(Messages.getString("ValidationError.18", "RenameExampleClassPlugin", "searchString"));
            }

            if (!StringUtility.stringHasValue(this.replaceString)) {
                warnings.add(Messages.getString("ValidationError.18", "RenameExampleClassPlugin", "replaceString"));
            }
        }

        return valid;
    }

    public void initialized(IntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getMyBatis3JavaMapperType();
        Matcher matcher = this.pattern.matcher(oldType);
        oldType = matcher.replaceAll(this.replaceString);
        introspectedTable.setMyBatis3JavaMapperType(oldType);
    }
}
