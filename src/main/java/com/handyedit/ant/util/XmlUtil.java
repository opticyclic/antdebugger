package com.handyedit.ant.util;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 7, 2009
 */
public class XmlUtil {

    public static XmlTag getTag(@NotNull XmlFile file, int line) {
        Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        XmlDocument xmlDoc = file.getDocument();
        if (doc == null || xmlDoc == null) {
            return null;
        }

        int startOffset = doc.getLineStartOffset(line);
        int endOffset = doc.getLineEndOffset(line);
        PsiElement elem = xmlDoc.findElementAt(startOffset);

        XmlTag tag = PsiTreeUtil.getParentOfType(elem, XmlTag.class, false);
        if (tag == null) {
            return null;
        }

        for (XmlTag child: tag.getSubTags()) {
            int childOffset = child.getTextOffset();
            if (startOffset <= childOffset && childOffset <= endOffset) {
                return child;
            }
        }
        return tag;
    }

    public static int getIntAttribute(Element elem, String name, int defaultValue) {
        String val = elem.getAttributeValue(name);
        if (val == null || "".equals(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static XmlToken getStartTagEnd(XmlTag tag) {
        if (tag == null) {
            return null;
        }

        for (PsiElement elem: tag.getChildren()) {
            if (elem instanceof XmlToken) {
                XmlToken token = (XmlToken) elem;
                if (XmlTokenType.XML_TAG_END.equals(token.getTokenType()) ||
                        XmlTokenType.XML_EMPTY_ELEMENT_END.equals(token.getTokenType())) {
                    return token;
                }
            }
        }

        return null;
    }
}
