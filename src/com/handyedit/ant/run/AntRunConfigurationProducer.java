package com.handyedit.ant.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

public class AntRunConfigurationProducer extends RunConfigurationProducer<AntRunConfiguration> {

    protected AntRunConfigurationProducer(AntRunConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(AntRunConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(AntRunConfiguration configuration, ConfigurationContext context) {
        return true;
    }

    @Override
    public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
        return other.isProducedBy(AntRunConfigurationProducer.class);
    }
}
