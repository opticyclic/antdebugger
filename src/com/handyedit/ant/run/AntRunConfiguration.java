package com.handyedit.ant.run;

import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.XmlUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> { // todo: store config between runs

    private static final String DEFAULT_BUILD_FILE = "build.xml";
    private static final int DEFAULT_DEBUG_PORT = 25000;
    private static final int DEFAULT_MAX_MEMORY = 256;

    private VirtualFile myBuildFile;
    private VirtualFile myTasksFolder;
    private int myDebugPort = DEFAULT_DEBUG_PORT;
    private String myJdkName;
    private String myTargetName;
    private String myVmParameters;

    private AntLoggingLevel myLoggingLevel = AntLoggingLevel.DEFAULT;

    private int myMaxMemory = DEFAULT_MAX_MEMORY;

    public AntRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(name, new RunConfigurationModule(project), factory);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new AntRunCommandLineState(executionEnvironment, this);
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        List<String> names = new ArrayList<String>();
        for (Sdk sdk: ProjectJdkTable.getInstance().getAllJdks()) {
            if (sdk.getSdkType() instanceof JavaSdkType) {
                names.add(sdk.getName());
            }
        }
        Collections.sort(names);
        return new AntRunSettingsEditor(names, getConfigurationModule().getProject());
    }

    @Override
    public Collection<Module> getValidModules() {
        return Collections.singleton(ModuleManager.getInstance(getProject()).getModules()[0]);
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new AntRunConfiguration(getProject(), getFactory(), "");
    }

    public String getBuildFolder() {
        if (myBuildFile == null) {
            return null;
        }

        VirtualFile parent = myBuildFile.getParent();
        return parent != null ? parent.getPath() : null;
    }

    public VirtualFile getBuildFile() {
        return myBuildFile;
    }

    public void setBuildFile(VirtualFile file) {
        myBuildFile = file;
    }

    public VirtualFile getTasksFolder() {
        return myTasksFolder;
    }

    public void setTasksFolder(VirtualFile folder) {
        myTasksFolder = folder;
    }

    public boolean isDefaultBuildFile() {
        return myBuildFile != null && DEFAULT_BUILD_FILE.equals(myBuildFile.getName());
    }

    public int getDebugPort() {
        return myDebugPort;
    }

    public void setDebugPort(int debugPort) {
        myDebugPort = debugPort;
    }

    public String getJavaExePath() {
        Sdk sdk = getSdk();
        JavaSdkType javaSdk = getJavaSdk(sdk);
        if (sdk != null && javaSdk != null) {
            return javaSdk.getVMExecutablePath(sdk);
        }
        return null;
    }


    public String getJdkTools() {
        Sdk sdk = getSdk();
        JavaSdkType javaSdk = getJavaSdk(sdk);
        if (sdk != null && javaSdk != null) {
            return javaSdk.getToolsPath(sdk);
        }
        return null;
    }

    public List<String> getAdditionalSdkClasses() {
        List<String> result = new ArrayList<String>();

        Sdk sdk = getSdk();
        if (sdk != null) {
            String jrePath = sdk.getHomePath() + File.separator + "jre";

            for (String url : sdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
                url = PathUtil.toPresentableUrl(url);
                if (!url.startsWith(jrePath)) {
                    result.add(url);
                }
            }
        }
        return result;
    }

    public List<String> getAdditionalAntClasses() {
        List<String> result = new ArrayList<String>();

        if (myTasksFolder != null) {
            for (VirtualFile child: myTasksFolder.getChildren()) {
                if (!child.isDirectory() && "jar".equals(child.getExtension())) {
                    result.add(child.getPath());
                }
            }
        }
        return result;
    }

    private static JavaSdkType getJavaSdk(Sdk sdk) {
        if (sdk != null) {
            SdkType sdkType = sdk.getSdkType();
            if (sdkType != null && sdkType instanceof JavaSdkType) {
                return (JavaSdkType) sdkType;
            }
        }
        return null;
    }

    private Sdk getSdk() {
        return ProjectJdkTable.getInstance().findJdk(myJdkName);
    }

    public String getAntHome() {
        File antFolder = new File(PathManager.getLibPath(), "ant");
        return antFolder.getPath();
    }

    private static final String ELEM_FILE = "file";
    private static final String ELEM_TASKS_FOLDER = "tasks-folder";
    private static final String ELEM_VM_PARAMS = "vm-params";
    private static final String ATTR_PORT = "debug-port";
    private static final String ATTR_JDK = "jdk-name";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_LOGGING_LEVEL = "logging-level";
    private static final String ATTR_MAX_MEMORY = "max-memory";

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        VirtualFile buildFile = getBuildFile();
        if (buildFile != null) {
            Element file = new Element(ELEM_FILE);
            file.setText(buildFile.getPath());
            element.addContent(file);
        }

        if (myTasksFolder != null) {
            Element file = new Element(ELEM_TASKS_FOLDER);
            file.setText(myTasksFolder.getPath());
            element.addContent(file);
        }

        if (myVmParameters != null) {
            Element vmParams = new Element(ELEM_VM_PARAMS);
            vmParams.setText(myVmParameters);
            element.addContent(vmParams);
        }

        element.setAttribute(ATTR_PORT, Integer.toString(myDebugPort));
        if (myJdkName != null) {
            element.setAttribute(ATTR_JDK, myJdkName);
        }
        if (myTargetName != null) {
            element.setAttribute(ATTR_TARGET, myTargetName);
        }
        element.setAttribute(ATTR_MAX_MEMORY, Integer.toString(myMaxMemory));
        element.setAttribute(ATTR_LOGGING_LEVEL, Integer.toString(getLoggingLevel().getValue()));
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        Element file = element.getChild(ELEM_FILE);
        if (file != null) {
            String path = file.getText();
            VirtualFile buildFile = FileUtil.findFile(path);
            setBuildFile(buildFile);
        }

        Element tasksFolderElem = element.getChild(ELEM_TASKS_FOLDER);
        if (tasksFolderElem != null) {
            String path = tasksFolderElem.getText();
            VirtualFile folder = FileUtil.findFile(path);
            setTasksFolder(folder);
        }
        Element vmParamsElem = element.getChild(ELEM_VM_PARAMS);
        if (vmParamsElem != null) {
            String vmParams = vmParamsElem.getText();
            if (!"".equals(vmParams)) {
                myVmParameters = vmParams;
            }
        }

        setDebugPort(XmlUtil.getIntAttribute(element, ATTR_PORT, DEFAULT_DEBUG_PORT));
        setMaxMemory(XmlUtil.getIntAttribute(element, ATTR_MAX_MEMORY, DEFAULT_MAX_MEMORY));

        myJdkName = element.getAttributeValue(ATTR_JDK);
        myTargetName = element.getAttributeValue(ATTR_TARGET);
        String levelStr = element.getAttributeValue(ATTR_LOGGING_LEVEL);
        int level = 0;
        if (levelStr != null) {
          try {
            level = Integer.parseInt(levelStr);
          } catch (NumberFormatException e) {
          }
        }
        myLoggingLevel = AntLoggingLevel.get(level);
    }

    public String getJdkName() {
        return myJdkName;
    }

    public void setJdkName(String jdkName) {
        myJdkName = jdkName;
    }

    public int getMaxMemory() {
        return myMaxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        myMaxMemory = maxMemory;
    }

    public String getTargetName() {
        return myTargetName;
    }

    public void setTargetName(String targetName) {
        myTargetName = targetName;
    }

    public String getVmParameters() {
        return myVmParameters;
    }

    public void setVmParameters(String vmParameters) {
        myVmParameters = vmParameters;
    }

    public AntLoggingLevel getLoggingLevel() {
        return myLoggingLevel != null ? myLoggingLevel : AntLoggingLevel.DEFAULT;
    }

    public void setLoggingLevel(AntLoggingLevel loggingLevel) {
        myLoggingLevel = loggingLevel;
    }
}
