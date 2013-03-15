package com.handyedit.ant.listener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public interface DebuggerCommand {
    void execute(BufferedWriter out) throws IOException;
}
