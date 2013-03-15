package com.handyedit.ant.xdebug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntLineBreakpointType extends XLineBreakpointType<XBreakpointProperties> {

    private XDebuggerEditorsProvider myEditorsProvider = new AntDebuggerEditorsProvider();

    public AntLineBreakpointType() {
        super("ant-line", "Ant breakpoints");
    }

    @Override
    public XBreakpointProperties createBreakpointProperties(@NotNull VirtualFile virtualFile, int i) {
        return null;
    }

    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        return "xml".equals(file.getExtension());
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return myEditorsProvider;
    }
}
