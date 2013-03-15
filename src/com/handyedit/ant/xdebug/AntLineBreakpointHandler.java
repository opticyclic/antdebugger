package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.BreakpointPosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

    private AntDebugProcess myDebugProcess;
    Map<BreakpointPosition, XLineBreakpoint> myBreakpointByPosition = new HashMap<BreakpointPosition, XLineBreakpoint>();

    public AntLineBreakpointHandler(@NotNull final AntDebugProcess debugProcess) {
        super(AntLineBreakpointType.class);
        myDebugProcess = debugProcess;
    }

    public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
        BreakpointPosition breakpoint = new BreakpointPosition(xBreakpoint);
        breakpoint = breakpoint.toLineEnd(myDebugProcess.getProject());
        myDebugProcess.addBreakPoint(breakpoint);
        myBreakpointByPosition.put(breakpoint, xBreakpoint);
    }

    public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, final boolean temporary) {
        BreakpointPosition breakpoint = new BreakpointPosition(xBreakpoint);
        breakpoint = breakpoint.toLineEnd(myDebugProcess.getProject());
        myDebugProcess.removeBreakPoint(breakpoint);
        myBreakpointByPosition.remove(breakpoint);
    }
}
