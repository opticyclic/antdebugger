package com.handyedit.ant.breakpoint;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public interface AntDebugListener {

    void onBreakpoint(BreakpointPosition pos);

    void onFinish();
}
