package com.handyedit.ant.xdebug;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntDebuggerEditorsProvider extends XDebuggerEditorsProvider {

    @NotNull
    @Override
    public FileType getFileType() {
        return XmlFileType.INSTANCE;
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull Project project, @NotNull String text, @Nullable XSourcePosition sourcePosition, @NotNull EvaluationMode mode) {
        PsiFile psiFile = new AntExpressionCodeFragmentImpl(project, "AntDebugger.expr", text);
        return PsiDocumentManager.getInstance(project).getDocument(psiFile);
    }
}
