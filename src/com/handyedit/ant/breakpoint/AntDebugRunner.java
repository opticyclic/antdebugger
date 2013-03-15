package com.handyedit.ant.breakpoint;

import com.handyedit.ant.run.AntProcessFactory;
import com.handyedit.ant.run.AntRunConfiguration;
import com.handyedit.ant.xdebug.AntDebugProcess;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntDebugRunner extends GenericProgramRunner {

    @NotNull
    public String getRunnerId() {
        return "AntDebugRunner";
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof AntRunConfiguration;
    }

    @Override
    public void checkConfiguration(RunnerSettings settings, ConfigurationPerRunnerSettings configurationPerRunnerSettings)
            throws RuntimeConfigurationException {

    }

    protected RunContentDescriptor doExecute(
            Project project, Executor executor,
            final RunProfileState state, RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {

        FileDocumentManager.getInstance().saveAllDocuments();

        AntRunConfiguration runConfig = (AntRunConfiguration) state.getRunnerSettings().getRunProfile();
        int debugPort = runConfig.getDebugPort();
        final OSProcessHandler serverProcessHandler = AntProcessFactory.getInstance(debugPort).createProcess(runConfig);
        if (serverProcessHandler == null) {
            return null;
        }
        if (runConfig.getBuildFile() == null) {
            Messages.showErrorDialog("Configuration doesn't have build file", "Ant debugger");
            return null;
        }
        final AntDebuggerProxy debuggerProxy = new AntDebuggerProxy(project, debugPort);

        final XDebugSession session = XDebuggerManager.getInstance(project).
                startSession(this, env, contentToReuse, new XDebugProcessStarter() {
                    @NotNull
                    public XDebugProcess start(@NotNull final XDebugSession session) {
                        return new AntDebugProcess(session, state, serverProcessHandler, debuggerProxy);
                    }
                });
        return session.getRunContentDescriptor();
    }
}
