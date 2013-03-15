package com.handyedit.ant.util;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

/**
 * @author Alexei Orischenko
 *         Date: Nov 10, 2009
 */
public class ConsoleUtil {

    public static ConsoleView createAttachedConsole(Project project, ProcessHandler processHandler) {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);

        ConsoleView console = consoleBuilder.getConsole();
        console.attachToProcess(processHandler);
        return console;
    }
}
