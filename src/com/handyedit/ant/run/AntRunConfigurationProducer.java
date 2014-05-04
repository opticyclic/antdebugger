package com.handyedit.ant.run;

import com.handyedit.ant.util.AntUtil;
import com.handyedit.ant.util.IdeaConfigUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.lang.ant.dom.AntDomElement;
import com.intellij.lang.ant.dom.AntDomProject;
import com.intellij.lang.ant.dom.AntDomReferenceBase;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

public class AntRunConfigurationProducer extends RunConfigurationProducer<AntRunConfiguration> {

    protected AntRunConfigurationProducer(AntRunConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(AntRunConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        Location location = context.getLocation();
        PsiElement psiLocation = context.getPsiLocation();

        String target = AntUtil.getTarget(psiLocation);

        configuration.setName(target);
        configuration.setTargetName(target);
        configuration.setBuildFile(location.getVirtualFile());
        Sdk targetJdk = IdeaConfigUtil.getJdk(location.getModule(), location.getProject());
        if (targetJdk != null) {
            configuration.setJdkName(targetJdk.getName());
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(AntRunConfiguration configuration, ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        String target = AntUtil.getTarget(psiLocation);
        String configurationTargetName = configuration.getTargetName();
        if (target == null || configurationTargetName == null || !target.equals(configurationTargetName)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
        return other.isProducedBy(AntRunConfigurationProducer.class);
    }
}
