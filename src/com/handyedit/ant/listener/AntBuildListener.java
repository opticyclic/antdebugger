package com.handyedit.ant.listener;

import com.handyedit.ant.util.StringUtil;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.Path;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Ant build listener that notifies IDE about reaching a task or target start (by build process)
 * and suspends build on task or target breakpoint (waits resume command from IDE).
 *
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntBuildListener implements BuildListener {

    // commands sent to IDE
    public static final String CMD_VAR = "var";
    public static final String CMD_TARGET_START = "target-start";
    public static final String CMD_TARGET_END = "target-end";
    public static final String CMD_BREAKPOINT_STOP = "stop";
    public static final String CMD_BUILD_FINISHED = "finish";

    private static final Set<String> IGNORED_TASKS = createIgnored();

    public static final String DEBUG_PORT_PROPERTY = "jetbrains.ant.debug.port";

    private BreakpointManager myManager;

    private DebuggerCommandListener myListener;

    public void buildStarted(BuildEvent buildEvent) {
        myManager = new BreakpointManager();

        try {
            String portStr = System.getProperty(DEBUG_PORT_PROPERTY);
            if (portStr != null) {
                myListener = DebuggerCommandListener.start(myManager, Integer.parseInt(portStr));
            }
        } catch (IOException e) {
            onError(e);
        }
    }

    public void buildFinished(BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                myListener.sendCommand(CMD_BUILD_FINISHED);
            }
        } catch (IOException e) {
            onError(e);
        }
    }

    public void targetStarted(BuildEvent buildEvent) {
        try {
            Target target = buildEvent.getTarget();
            Location location = target.getLocation();
            if (myListener != null) {
                BreakpointPosition pos = new BreakpointPosition(location);
                myManager.setCurrentPosition(pos);
                myManager.onTargetStart(pos);
                String line = Integer.toString(location.getLineNumber());
                myListener.sendCommand(CMD_TARGET_START, line, location.getFileName());
                onBreakpoint(location, buildEvent);
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    private void onBreakpoint(Location location, BuildEvent event) throws InterruptedException {
        if (Location.UNKNOWN_LOCATION.equals(location)) {
            return;
        }

        int line = location.getLineNumber() - 1;
        boolean tempBreakpoint = isTempBreakpoint(event);
        BreakpointPosition pos = new BreakpointPosition(location);
        boolean runToBreakpoint = myManager.isRunToBreakpoint(pos);

        if ((tempBreakpoint || runToBreakpoint || myManager.isBreakpoint()) && myListener != null) {
            try {
                if (tempBreakpoint) {
                    myManager.removeTemp();
                }
                if (runToBreakpoint) {
                    myManager.removeRunTo(pos);
                }

                sendVars(event);
                sendRefs(event);
                myListener.sendCommand(CMD_BREAKPOINT_STOP, Integer.toString(line), location.getFileName());
                myManager.waitResume();
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    private boolean isTempBreakpoint(BuildEvent e) {
        if (myManager.isTempBreakpoint()) {
            return !isIgnored(e.getTask(), e.getTarget());
        }
        return false;
    }

    public void targetFinished(BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                myManager.onTargetEnd();

                myListener.sendCommand(CMD_TARGET_END);
            }
        } catch (IOException e) {
            onError(e);
        }
    }

    public void taskStarted(BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                Location taskLocation = buildEvent.getTask().getLocation();
                Location targetLocation = buildEvent.getTarget().getLocation();
                myManager.setCurrentPosition(new BreakpointPosition(taskLocation), new BreakpointPosition(targetLocation));
                onBreakpoint(taskLocation, buildEvent);
            }
        } catch (InterruptedException e) {
            onError(e);
        }
    }

    public void taskFinished(BuildEvent buildEvent) {
    }

    public void messageLogged(BuildEvent buildEvent) {
    }

    private void onError(Exception e) {
        if (myListener != null) {
            myListener.close();
            myListener = null;
        }
    }

    private void sendVars(BuildEvent e) throws IOException {
        Hashtable props = e.getProject().getProperties();
        if (props != null && myListener != null) {
            for (Map.Entry entry: (Set<Map.Entry>) props.entrySet()) {
                String name = (String) entry.getKey();
                Object val = entry.getValue();
                if (val != null) {
                    myListener.sendCommand(CMD_VAR, name, val.toString());
                } else {
                    myListener.sendCommand(CMD_VAR, name);
                }
            }
        }
    }

    private void sendRefs(BuildEvent e) throws IOException {
        Hashtable refs = e.getProject().getReferences();
        if (refs != null && myListener != null) {
            for (Map.Entry entry: (Set<Map.Entry>) refs.entrySet()) {
                String name = (String) entry.getKey();
                Object val = entry.getValue();
                if (val != null && val instanceof Path) {
                    myListener.sendCommand(CMD_VAR, name, StringUtil.removeLineFeeds(val.toString()));
                }
            }
        }
    }

    static void log(String msg) {
        File file = new File("/projects/debug.log");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
            out.write(msg);
            out.newLine();
            out.close();
        } catch (IOException e) {

        }
    }

    private static Set<String> createIgnored() {
        Set<String> result = new HashSet<String>();
        result.add("import");
        result.add("property");
        result.add("xmlproperty");
        result.add("loadproperties");
        result.add("taskdef");
        result.add("typedef");
        result.add("patternset");
        result.add("path");
        result.add("tstamp");
        return result;
    }

    private boolean isIgnored(Task task, Target parentTarget) {
        if (task == null || parentTarget == null || task.getTaskName() == null) {
            return true;
        }

        return IGNORED_TASKS.contains(task.getTaskName()) && !myManager.isCurrentTarget(parentTarget.getLocation());
    }
}
