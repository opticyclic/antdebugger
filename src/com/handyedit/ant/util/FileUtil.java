package com.handyedit.ant.util;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * @author Alexei Orischenko
 *         Date: Nov 10, 2009
 */
public class FileUtil {

    private static final String FILE_URL_PREFIX = "file:///";

    public static void addClasspath(String path, StringBuffer result) {
        result.append(path);
        result.append(File.pathSeparator);
    }

    public static String getPath(String parent, String child) {
        return parent + File.separator + child;
    }

    public static VirtualFile findFile(String path) {
        if (path == null || "".equals(path)) {
            return null;
        }

        if (!path.startsWith(FILE_URL_PREFIX)) {
            path = FILE_URL_PREFIX + path;
        }

        return VirtualFileManager.getInstance().findFileByUrl(path);
    }

    public static String getAbsolutePath(String path, String folder) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        if (isAbsolutePath(path)) {
            return path;
        } else {
            return getPath(folder, path);
        }
    }

    private static boolean isAbsolutePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }

        if (SystemInfo.isWindows) {
            if (path.length() >= 3) {
                String prefix = path.substring(0, 3);
                return Character.isLetter(prefix.charAt(0)) && (
                        prefix.endsWith(":/") || prefix.endsWith(":\\"));
            }
        } else {
            return path.startsWith("/");
        }

        return false;
    }
}
