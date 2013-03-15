package com.handyedit.ant.breakpoint;

import com.handyedit.ant.listener.AntBuildListener;
import com.handyedit.ant.listener.TempBreakpointType;
import com.handyedit.ant.listener.cmd.DebuggerCommandFactory;
import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.NetUtil;
import com.handyedit.ant.util.XmlUtil;
import com.handyedit.ant.xdebug.vars.AntVar;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Proxy that communicates with Ant build process listener running in the Ant process.
 * Sends and reads commands. Read commands passed to Ant debug listeners.
 *
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntDebuggerProxy {

    private XDebuggerUtilImpl myDebuggerUtil = new XDebuggerUtilImpl();

    private Project myProject;

    private BufferedReader myReader;
    private BufferedWriter myWriter;

    private XSourcePosition myCurrentPosition;
    private List<XSourcePosition> myStack = new ArrayList<XSourcePosition>();

    private Set<AntDebugListener> myListeners = new HashSet<AntDebugListener>();

    private Map<String, String> myVars = new HashMap<String, String>();

    private int myPort;

    public AntDebuggerProxy(Project project, int port) {
        myProject = project;
        myPort = port;
    }

    synchronized public boolean isReady() {
        return myWriter != null && myReader != null;
    }

    public boolean connect(ProgressIndicator indicator, ProcessHandler handler, int seconds) throws IOException {
        Socket s = connect(seconds, handler, indicator);
        if (s == null) {
            return false;
        }
        synchronized (this) {
            myReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            myWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    readAntResponse();
                } catch (IOException e) {
                    // todo:
                }
            }
        }).start();

        return true;
    }

    private Socket connect(int times, ProcessHandler handler, ProgressIndicator indicator) {
        for (int i = 0; i < times && !indicator.isCanceled(); i++) {
            try {
                return new Socket(NetUtil.getLocalHost(), myPort);
            } catch (IOException e) {
                try {
                    if (handler.isProcessTerminated()) {
                        throw new RuntimeException("Ant process terminated");
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
        }

        return null;
    }

    private void readAntResponse() throws IOException {
        String line;
        while ((line = myReader.readLine()) != null) {
            String[] args = line.split(",");
            String cmd = args[0];
            if (AntBuildListener.CMD_VAR.equals(cmd)) {
                processVar(args);
            } else if (AntBuildListener.CMD_TARGET_START.equals(cmd)) {
                processTargetStart(args);
            } else if (AntBuildListener.CMD_TARGET_END.equals(cmd)) {
                processTargetEnd(args);
            } else {
                notifyListener(args);
            }
        }
    }

    private void processVar(String[] args) {
        if (args.length != 2 && args.length != 3) {
            return;
        }

        String val = args.length == 3 ? args[2] : null;
        myVars.put(args[1], val);
    }

    private void processTargetStart(String[] args) {
        int line = Integer.parseInt(args[1]);
        XSourcePosition pos = createPosition(args[2], line - 1);
        if (pos != null) {
            myStack.add(pos);
        }
    }

    private void processTargetEnd(String[] args) {
        myStack.remove(myStack.size() - 1);
    }

    private void notifyListener(String[] args) {
        String cmd = args[0];
        for (AntDebugListener l: myListeners) {

            if (AntBuildListener.CMD_BREAKPOINT_STOP.equals(cmd)) {
                Integer line = Integer.parseInt(args[1]);
                String file = args[2];
                XSourcePosition pos = createPosition(file, line);
                if (pos != null) {
                    myCurrentPosition = pos;
                    l.onBreakpoint(new BreakpointPosition(file, line));
                }
            }
            if (AntBuildListener.CMD_BUILD_FINISHED.equals(cmd)) {
                l.onFinish();
            }
        }
    }

    private XSourcePosition createPosition(String file, int line) {
        VirtualFile virtualFile = FileUtil.findFile(file);
        return virtualFile != null ? myDebuggerUtil.createPosition(virtualFile, line) : null;
    }
    
    public void attach(Collection<BreakpointPosition> breakpoints) throws IOException {
        for (BreakpointPosition position: breakpoints) {
            addBreakpoint(position);
        }
        resume();
    }

    public void addBreakpoint(BreakpointPosition pos) throws IOException {
        command(DebuggerCommandFactory.CMD_SET_BREAKPOINT, Integer.toString(pos.getLine()), pos.getFile());
    }

    public void removeBreakpoint(BreakpointPosition pos) throws IOException {
        command(DebuggerCommandFactory.CMD_REMOVE_BREAKPOINT, Integer.toString(pos.getLine()), pos.getFile());
    }

    public void resume() throws IOException {
        command(DebuggerCommandFactory.CMD_RESUME_EXECUTION);
    }

    public void runTo(XSourcePosition pos) {
        try {
            command(DebuggerCommandFactory.CMD_RUN_TO_CURSOR, Integer.toString(pos.getLine()), pos.getFile().getPath());
            resume();
        } catch (IOException e) {
            // todo:
        }
    }

    public void stepInto() {
        try {
            setTempBreakpoint(TempBreakpointType.INTO);
        } catch (IOException e) {
            // todo:
        }
    }

    public void stepOver() {
        try {
            setTempBreakpoint(TempBreakpointType.OVER);
        } catch (IOException e) {
            // todo:
        }
    }

    public void stepOut() {
        try {
            setTempBreakpoint(TempBreakpointType.OUT);
        } catch (IOException e) {
            // todo:
        }
    }

    private void setTempBreakpoint(TempBreakpointType type) throws IOException {
        command(DebuggerCommandFactory.CMD_SET_TEMP_BREAKPOINT, Integer.toString(type.getValue()));
        resume();
    }

    public void finish() {

    }

    public void addAntDebugListener(AntDebugListener listener) {
        myListeners.add(listener);
    }

    public void removeAntDebugListener(AntDebugListener listener) {
        myListeners.remove(listener);
    }

    private void command(String... args) throws IOException {
        myWriter.write(StringUtils.join(args, ","));
        myWriter.newLine();
        myWriter.flush();
    }

    public AntFrame[] getFrames() {
        if (myCurrentPosition == null) {
            return new AntFrame[] {};
        }

        List<AntFrame> result = new ArrayList<AntFrame>();
        for (XSourcePosition pos: myStack) {
            result.add(createFrame(pos));
        }
        result.add(createFrame(myCurrentPosition));
        Collections.reverse(result);
        AntFrame[] arr = new AntFrame[result.size()];
        result.toArray(arr);
        return arr;
    }

    private AntFrame createFrame(XSourcePosition pos) {
        XmlTag tag = getTag(pos);
        String name = tag != null ? tag.getName() : "";
        boolean target = "target".equals(name);
        if (tag != null && target) {
            String targetName = tag.getAttributeValue("name");
            if (targetName != null) {
                name += " '" + targetName + "'";
            }
        }
        return new AntFrame(pos, target, name);
    }

    private XmlTag getTag(final XSourcePosition pos) {
        final XmlTag[] result = new XmlTag[1];

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                XmlFile xmlFile = (XmlFile) PsiManager.getInstance(myProject).findFile(pos.getFile());
                if (xmlFile != null) {
                    result[0] = XmlUtil.getTag(xmlFile, pos.getLine());
                }
            }
        });

        return result[0];
    }

    public String getVariableValue(String name) {
        String value = myVars.get(name);
        return value;
    }

    public List<XValue> getVars() {
        List<String> names = new ArrayList<String>(myVars.keySet());
        Collections.sort(names);

        List<XValue> result = new ArrayList<XValue>();

        for (String key: names) {
            result.add(new AntVar(key, myVars.get(key)));
        }

        return result;
    }

    public int getPort() {
        return myPort;
    }

    public Project getProject() {
        return myProject;
    }
}
