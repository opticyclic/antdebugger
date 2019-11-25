package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebugListener;
import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.BreakpointPosition;
import com.handyedit.ant.util.ConsoleUtil;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntDebugProcess extends XDebugProcess {

    boolean isSuspended = false;

    XBreakpointHandler[] myBreakPointHandlers;
    AntDebuggerProxy myDebuggerProxy;
    AntDebugListener myDebugListener;
    RunProfileState myState;
    AntLineBreakpointHandler myLineBreakpointHandler;


    private final ProcessHandler myOSProcessHandler;


    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new AntDebuggerEditorsProvider();
    }

    public AntDebugProcess(@NotNull final XDebugSession session,
                           @NotNull final RunProfileState state,
                           @Nullable final ProcessHandler processHandler,
                           @NotNull final AntDebuggerProxy debuggerProxy) {
        super(session);
        myState = state;
        myDebuggerProxy = debuggerProxy;
        myOSProcessHandler = processHandler;
        myDebugListener = new MyAntDebugListener(session.getProject());

        myLineBreakpointHandler = new AntLineBreakpointHandler(this);
        myBreakPointHandlers = new XBreakpointHandler[]{ myLineBreakpointHandler };

        myDebuggerProxy.addAntDebugListener(myDebugListener);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Ant debugger", true) {
            public void run(@NotNull final ProgressIndicator indicator) {
                indicator.setText("Connecting...");
                indicator.setIndeterminate(true);

                try {
                    myDebuggerProxy.connect(indicator, myOSProcessHandler, 60);
                    
                    if (myDebuggerProxy.isReady()) {
                        myDebuggerProxy.attach(myLineBreakpointHandler.myBreakpointByPosition.keySet());
                    } else {
                        terminateDebug(null);
                    }

                } catch (final Exception e) {
                    terminateDebug(e.getMessage());
                }
            }
        });
    }

    private void terminateDebug(final String msg) {
        getProcessHandler().destroyProcess();
        invokeLater(new Runnable() {
            public void run() {
                String text = "Debugger can't connect to Ant on port " + myDebuggerProxy.getPort();
                Messages.showErrorDialog(msg != null ? text + ":\r\n" + msg : text, "Ant debugger");
            }
        });
    }

    @Nullable
    protected ProcessHandler doGetProcessHandler() {
        return myOSProcessHandler;
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        return ConsoleUtil.createAttachedConsole(getSession().getProject(), getProcessHandler());
    }

    public void startStepInto() {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepInto();
        }
    }

    public void startStepOver() {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepOver();
        }
    }

    public void startStepOut() {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepOut();
        }
    }

    public void runToPosition(@NotNull final XSourcePosition position) {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.runTo(position);
        }
    }

    public void stop() {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.removeAntDebugListener(myDebugListener);
            if (isSuspended) {
                getSession().resume();
            }
            myDebuggerProxy.finish();
        }
    }

    public void resume() {
        isSuspended = false;
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.resume();
            } catch (IOException e) {
                // todo:
            }
        }
    }

    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return myBreakPointHandlers;
    }

    public void removeBreakPoint(BreakpointPosition breakpoint) {
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.removeBreakpoint(breakpoint);
            } catch (IOException e) {
                // todo
            }
        }
    }

    public void addBreakPoint(BreakpointPosition breakpoint) {
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.addBreakpoint(breakpoint);
            } catch (IOException e) {
                // todo:
            }
        }
    }

    Project getProject() {
        return myDebuggerProxy.getProject();
    }

    private class MyAntDebugListener implements AntDebugListener {

        private Project myProject;

        private MyAntDebugListener(Project project) {
            myProject = project;
        }

        public void onBreakpoint(BreakpointPosition pos) {
            final XDebugSession debugSession = getSession();


            isSuspended = true;
            final XBreakpoint xBreakpoint = myLineBreakpointHandler.myBreakpointByPosition.get(pos);

            AntSuspendContext suspendContext = new AntSuspendContext(myProject, AntDebugProcess.this);

            if (xBreakpoint != null) {
                if (debugSession.breakpointReached(xBreakpoint, suspendContext)) {
                } else {
                    resume();
                }
            } else {
                debugSession.positionReached(suspendContext);
            }
        }

        public void onFinish() {
            getSession().stop();
        }
    }
}
