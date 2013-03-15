package com.handyedit.ant.xdebug;

import com.handyedit.ant.util.StringUtil;
import com.handyedit.ant.xdebug.vars.AntVar;
import com.intellij.lang.ant.psi.AntNameIdentifier;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Dec 15, 2009
 */
public class AntDebuggerEvaluator extends XDebuggerEvaluator {

    private AntStackFrame myFrame;

    public AntDebuggerEvaluator(AntStackFrame frame) {
        myFrame = frame;
    }

    @Override
    public void evaluate(@NotNull String s, XEvaluationCallback xEvaluationCallback) {
        String value = myFrame.getDebuggerProxy().getVariableValue(s);
        xEvaluationCallback.evaluated(new AntVar(s, value));
    }

    @Override
    public TextRange getExpressionRangeAtOffset(Project project, Document document, int i) {
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (file != null) {
            PsiElement elem = file.findElementAt(i);
            if (elem instanceof AntNameIdentifier) {
                AntNameIdentifier antElement = (AntNameIdentifier) elem;
                TextRange range = antElement.getTextRange();
                return new TextRange(range.getStartOffset() + 2, range.getEndOffset() - 1);
            } else {
                String text = document.getText();
                int start = StringUtil.findPropertyNameEnd(text, i, -1) + 1;
                int end = StringUtil.findPropertyNameEnd(text, i, 1);
                if (start >= 2 && "${".equals(text.substring(start - 2, start)) && end < text.length() - 2 &&
                        "}".equals(text.substring(end, end + 1))) {
                    return new TextRange(start, end);
                }
            }
        }
        return null;
    }
}
