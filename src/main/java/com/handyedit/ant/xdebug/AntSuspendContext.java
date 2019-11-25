package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntSuspendContext extends XSuspendContext {

    private AntExecutionStack myExecutionStack;
    private XExecutionStack[] myExecutionStacks = XExecutionStack.EMPTY_ARRAY;


    public AntSuspendContext(Project project, AntDebugProcess debugProcess) {
        AntDebuggerProxy debuggerProxy = debugProcess.myDebuggerProxy;
        if (!debuggerProxy.isReady()) {
            return;
        }

        myExecutionStack = new AntExecutionStack(project, debuggerProxy);
        myExecutionStacks = new XExecutionStack[1];
        myExecutionStacks[0] = myExecutionStack;
    }


    @Override
    public XExecutionStack getActiveExecutionStack() {
        return myExecutionStack;
    }

    @Override
    public XExecutionStack[] getExecutionStacks() {
        return myExecutionStacks;
    }

}
