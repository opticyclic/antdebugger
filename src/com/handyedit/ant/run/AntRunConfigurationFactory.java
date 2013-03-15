package com.handyedit.ant.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunConfigurationFactory extends ConfigurationFactory {

    public AntRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new AntRunConfiguration(project, this, "");
    }

    @Override
    public String getName() {
        return "Ant build";
    }

    @Override
    public Icon getIcon() {
        return Icons.ANT_TARGET_ICON;
    }

    
}
