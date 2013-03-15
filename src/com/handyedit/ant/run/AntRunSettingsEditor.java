package com.handyedit.ant.run;

import com.handyedit.ant.util.AntUtil;
import com.handyedit.ant.util.FileUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunSettingsEditor extends SettingsEditor<AntRunConfiguration> {

    private TextFieldWithBrowseButton myPath = new TextFieldWithBrowseButton();
    private TextFieldWithBrowseButton myTasksPath = new TextFieldWithBrowseButton();
    private JComboBox myJdkName;
    private JComboBox myTargetName;
    private JTextField myMaxMemory = new JTextField();
    private JTextField myPort = new JTextField();
    private JTextField myVmParameters = new JTextField();

    private JComboBox myLoggingLevel;

    private JPanel myPanel = new JPanel();

    private Project myProject;

    public AntRunSettingsEditor(java.util.List<String> jdkNames, Project project) {
        myProject = project;
        java.util.List<String> sdks = new ArrayList<String>();
        sdks.addAll(jdkNames);
        sdks.add(0, "[None]");
        myJdkName = new JComboBox(com.handyedit.ant.util.StringUtil.toArray(sdks));
        myTargetName = new JComboBox();

        myLoggingLevel = new JComboBox(new String[] { "Default", "Quiet", "Verbose", "Debug" });

        myPanel.setLayout(new GridLayout(0, 2));
        myPanel.add(new JLabel("Build file:"));
        myPanel.add(myPath);
        myPanel.add(new JLabel("Target:"));
        myPanel.add(myTargetName);
        myPanel.add(new JLabel("Java SDK:"));
        myPanel.add(myJdkName);

        myPanel.add(new JLabel("Ant tasks folder:"));
        myPanel.add(myTasksPath);

        myPanel.add(new JLabel("Max Java heap size (Mb):"));
        myPanel.add(myMaxMemory);
        myPanel.add(new JLabel("VM parameters:"));
        myPanel.add(myVmParameters);
        myPanel.add(new JLabel("Debug port:"));
        myPanel.add(myPort);
        myPanel.add(new JLabel("Logging level:"));
        myPanel.add(myLoggingLevel);

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        myPath.addBrowseFolderListener("Select build file", "Select build file", null, descriptor);
        myPath.getTextField().getDocument().addDocumentListener(new FileChangedListener());

        descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        myTasksPath.addBrowseFolderListener("Select custom Ant tasks folder", "Select folder with JARs that contain custom Ant tasks", null, descriptor);
    }

    @Override
    protected void resetEditorFrom(AntRunConfiguration config) {
        VirtualFile file = config.getBuildFile();
        myPath.setText(file != null ? file.getPath() : "");
        VirtualFile tasksFolder = config.getTasksFolder();
        myTasksPath.setText(tasksFolder != null ? tasksFolder.getPath() : "");
        myPort.setText(Integer.toString(config.getDebugPort()));
        myMaxMemory.setText(Integer.toString(config.getMaxMemory()));

        String jdkName = config.getJdkName();

        if (jdkName != null) {
            myJdkName.setSelectedItem(jdkName);
        } else if (myJdkName.getItemCount() > 0) {
            myJdkName.setSelectedIndex(0);
        }

        String targetName = config.getTargetName();
        if (targetName != null) {
            myTargetName.setSelectedItem(targetName);
        } else if (myTargetName.getItemCount() > 0) {
            myTargetName.setSelectedIndex(0);
        }

        String vmParams = config.getVmParameters();
        myVmParameters.setText(vmParams != null ? vmParams : "");

        myLoggingLevel.setSelectedIndex(config.getLoggingLevel().getValue());
    }

    @Override
    protected void applyEditorTo(AntRunConfiguration config) throws ConfigurationException {
        String path = myPath.getText();
        VirtualFile file = FileUtil.findFile(path);

        if (file == null) {
            throw new ConfigurationException("Please select file");
        }
        VirtualFile folder = file.getParent();
        if (folder == null) {
            throw new ConfigurationException("File without parent folder");
        }
        config.setBuildFile(file);

        path = myTasksPath.getText();
        VirtualFile libFolder = FileUtil.findFile(path);
        config.setTasksFolder(libFolder);

        String port = myPort.getText();
        if (!StringUtil.isEmptyOrSpaces(port)) {
            try {
                config.setDebugPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Port should be integer");
            }
        }
        String memory = myMaxMemory.getText();
        if (StringUtil.isEmptyOrSpaces(memory)) {
            throw new ConfigurationException("Max memory should be integer");
        }
        try {
            config.setMaxMemory(Integer.parseInt(memory));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Max memory should be integer");
        }


        Sdk jdk = null;
        String jdkName = null;
        if (myJdkName.getSelectedIndex() > 0) {
            jdkName = (String) myJdkName.getSelectedItem();
            jdk = ProjectJdkTable.getInstance().findJdk(jdkName);
        }
        if (jdk == null) {
            throw new ConfigurationException("JDK required");
        }

        config.setJdkName(jdkName);

        String targetName = null;
        if (myTargetName.getSelectedIndex() > 0) {
            targetName = (String) myTargetName.getSelectedItem();
        }
        config.setTargetName(targetName);

        String vmParams = myVmParameters.getText();
        config.setVmParameters(!StringUtil.isEmpty(vmParams) ? vmParams : null);

        config.setLoggingLevel(AntLoggingLevel.get(myLoggingLevel.getSelectedIndex()));
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    protected void disposeEditor() {
    }

    private void loadTargets() {
        java.util.List<String> targets = new ArrayList<String>();

        String path = myPath.getText();
        if (!StringUtil.isEmpty(path)) {
            VirtualFile file = FileUtil.findFile(path);

            targets.addAll(AntUtil.getTargets(file, myProject));
            Collections.sort(targets);
            targets.add(0, "[Default]");
        }
        myTargetName.setModel(new DefaultComboBoxModel(com.handyedit.ant.util.StringUtil.toArray(targets)));
    }

    private class FileChangedListener implements DocumentListener {
        public void insertUpdate(DocumentEvent documentEvent) {
            loadTargets();
        }

        public void removeUpdate(DocumentEvent documentEvent) {
            loadTargets();
        }

        public void changedUpdate(DocumentEvent documentEvent) {
            loadTargets();
        }
    }
}
