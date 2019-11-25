package com.handyedit.ant.breakpoint;

import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.XmlUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

/**
 * @author Alexei Orischenko
 *         Date: Dec 9, 2009
 */
public class BreakpointPosition extends Pair<String, Integer> {

    public BreakpointPosition(String file, Integer line) {
        super(file, line);
    }

    public BreakpointPosition(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        this(breakpoint.getPresentableFilePath(), breakpoint.getLine());
    }

    public int getLine() {
        return getSecond();
    }

    public String getFile() {
        return getFirst();
    }

    public BreakpointPosition toLineEnd(Project project) {
        VirtualFile file = FileUtil.findFile(getFile());
        if (file != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile instanceof XmlFile) {
                XmlTag tag = XmlUtil.getTag((XmlFile) psiFile, getLine());
                XmlToken token = XmlUtil.getStartTagEnd(tag);
                Document doc = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                if (doc != null && token != null) {
                    int endLine = doc.getLineNumber(token.getTextRange().getEndOffset());
                    return getLine() != endLine ? new BreakpointPosition(getFile(), endLine) : this;
                }
            }
        }
        return this;
    }
}
