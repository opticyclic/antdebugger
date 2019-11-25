package com.handyedit.ant.breakpoint;

import com.intellij.xdebugger.XSourcePosition;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntFrame {

    private XSourcePosition mySourcePosition;
    private boolean myTarget;
    private String myName;

    public AntFrame(XSourcePosition pos, boolean target, String name) {
        mySourcePosition = pos;
        myTarget = target;
        myName = name;
    }

    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    public boolean isTarget() {
        return myTarget;
    }

    public String getName() {
        return myName;
    }
}
