package com.handyedit.ant.xdebug;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.PsiPlainTextFileImpl;
import com.intellij.testFramework.LightVirtualFile;

/**
 * @author Alexei Orischenko
 *         Date: Dec 16, 2009
 */
public class AntExpressionCodeFragmentImpl extends PsiPlainTextFileImpl {

    public AntExpressionCodeFragmentImpl(Project project, String name, String text) {
        super(((PsiManagerEx) PsiManager.getInstance(project)).getFileManager().createFileViewProvider(
            new LightVirtualFile(name, StdFileTypes.PLAIN_TEXT, text), true));

        ((SingleRootFileViewProvider) getViewProvider()).forceCachedPsi(this);
    }
}
