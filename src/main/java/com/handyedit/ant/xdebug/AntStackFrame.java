package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.AntFrame;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntStackFrame extends XStackFrame {

    protected Project myProject;

    protected AntDebuggerProxy myDebuggerProxy;
    protected XSourcePosition mySourcePosition;

    private String myName;

    @Override
    public Object getEqualityObject() {
        return 0;
    }

    @Override
    public void computeChildren(@NotNull final XCompositeNode node) {
        try {

      node.addChildren(myDebuggerProxy.getVars(), true);
        } catch (Exception e) {
            super.computeChildren(node);
        }
    }

    public AntStackFrame(final Project project,
                         final AntDebuggerProxy debuggerProxy, AntFrame frame) {
        myProject = project;
        myDebuggerProxy = debuggerProxy;
        mySourcePosition = frame.getSourcePosition();

        myName = frame.getName();
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    public void customizePresentation(final SimpleColoredComponent component) {
        final XSourcePosition position = getSourcePosition();
        if (position != null) {
            appendText(component, myName);
            appendText(component, " ");
            appendText(component, position.getFile().getName());
            appendText(component, ":");
            appendText(component, Integer.toString(position.getLine() + 1));
            component.setIcon(AllIcons.Debugger.StackFrame);
        } else {
            component.append("Stack frame not available", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    private static void appendText(SimpleColoredComponent component, String text) {
        component.append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new AntDebuggerEvaluator(this);
    }

    public AntDebuggerProxy getDebuggerProxy() {
        return myDebuggerProxy;
    }
}
