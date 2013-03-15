package com.handyedit.ant.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 12, 2009
 */
public class IdeaConfigUtil {

    public static Sdk getJdk(Module module, @NotNull Project project) {
        Sdk jdk;
        if (module != null) {
            jdk = ModuleRootManager.getInstance(module).getSdk();
            if (jdk != null && jdk.getSdkType() instanceof JavaSdkType) {
                return jdk;
            }
        }

        jdk = ProjectRootManager.getInstance(project).getProjectJdk();
        if (jdk != null && jdk.getSdkType() instanceof JavaSdkType) {
            return jdk;
        }

        Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk sdk: sdks) {
            if (sdk != null && sdk.getSdkType() instanceof  JavaSdkType) {
                return sdk;
            }
        }

        return null;
    }

    /**
     * Returns folder or Jar file with plugin classes.
     *
     * @param pluginClass plugin class
     * @return path to folder or Jar file
     */
    public static String getPluginClassesFolder(Class pluginClass) {
        return PathUtil.getJarPathForClass(pluginClass);
    }
}
