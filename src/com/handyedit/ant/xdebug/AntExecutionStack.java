package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.AntFrame;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntExecutionStack extends XExecutionStack {

    private List<AntStackFrame> myFrames = new ArrayList<AntStackFrame>();
    private AntStackFrame myTopFrame = null;

    public AntExecutionStack(Project project, AntDebuggerProxy debuggerProxy) {
        super("");
        
        if (!debuggerProxy.isReady()) {
            return;
        }
        try {
            AntFrame[] frames = debuggerProxy.getFrames();
            for (int i = 0; i < frames.length; i++) {
                final AntFrame antFrame = frames[i];
                AntStackFrame frame = new AntStackFrame(project, debuggerProxy, antFrame);
                myFrames.add(frame);
                if (i == 0) {
                    myTopFrame = frame;
                }
            }
        }
        catch (Exception e) {
            myFrames.clear();
            myTopFrame = null;
        }
    }

    public XStackFrame getTopFrame() {
        return myTopFrame;
    }

    public void computeStackFrames(final int frameCount, final XStackFrameContainer container) {
        if (frameCount <= myFrames.size()) {
            container.addStackFrames(myFrames.subList(frameCount, myFrames.size()), true);
        }
    }
}
