package com.handyedit.ant.listener;

import org.apache.tools.ant.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Breakpoints storage on Ant side.
 *
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class BreakpointManager {

    // current debugger position
    private BreakpointPosition myCurrentTaskLine;
    private BreakpointPosition myCurrentTargetLine;

    private List<BreakpointPosition> myTaskStack = new ArrayList<BreakpointPosition>();

    // sync objects
    private Object myResumeObject = new Object();
    private Object myTempSyncObject = new Object();

    // task and target breakpoints
    private Set<BreakpointPosition> myBreakpoints = new HashSet<BreakpointPosition>(); // breakpoints
    private Set<BreakpointPosition> myRunToBreakpoints = new HashSet<BreakpointPosition>(); // run to cursor breakpoints

     // next task breakpoint variables
    private boolean myTaskBreakpoint = false; // true if set
    private TempBreakpointType myTempBreakpointType;
    private int myTempTargetStackDepth;

    public boolean isBreakpoint() {
        return myBreakpoints.contains(myCurrentTaskLine) ||
                (myBreakpoints.contains(myCurrentTargetLine) && !isTask());
    }

    public boolean isRunToBreakpoint(BreakpointPosition pos) {
        return myRunToBreakpoints.contains(pos);
    }

    public boolean isTempBreakpoint() {
        synchronized (myTempSyncObject) {
            if (isTask() && myTaskBreakpoint) {
                if (myTempBreakpointType == TempBreakpointType.INTO) {
                    return true;
                }
                int stackSize = getStackSize();
                if (myTempBreakpointType == TempBreakpointType.OVER) {
                    return stackSize <= myTempTargetStackDepth;
                }
                if (myTempBreakpointType == TempBreakpointType.OUT) {
                    return stackSize < myTempTargetStackDepth;
                }
            }
            return false;
        }
    }

    public void add(BreakpointPosition loc) {
        myBreakpoints.add(loc);
    }

    public void remove(BreakpointPosition loc) {
        myBreakpoints.remove(loc);
    }

    public void addRunTo(BreakpointPosition loc) {
        myRunToBreakpoints.add(loc);
    }

    public void removeRunTo(BreakpointPosition pos) {
        myRunToBreakpoints.remove(pos);
    }

    public void addTemp(TempBreakpointType type) {
        synchronized (myTempSyncObject) {
            myTaskBreakpoint = true;
            myTempBreakpointType = type;
            myTempTargetStackDepth = getStackSize();
        }
    }

    public void removeTemp() {
        synchronized (myTempSyncObject) {
            myTaskBreakpoint = false;
        }
    }

    public void waitResume() throws InterruptedException {
        synchronized (myResumeObject) {
            myResumeObject.wait();
        }
    }

    public void resume() {
        synchronized (myResumeObject) {
            myResumeObject.notifyAll();
        }
    }

    public void setCurrentPosition(BreakpointPosition taskLine, BreakpointPosition targetLine) {
        myCurrentTaskLine = taskLine;
        myCurrentTargetLine = targetLine;
    }

    private boolean isTask() {
        return myCurrentTaskLine != null;
    }

    public void setCurrentPosition(BreakpointPosition targetLine) {
        setCurrentPosition(null, targetLine);
    }

    public Set<BreakpointPosition> getBreakpoints() {
        return myBreakpoints;
    }

    public void onTargetStart(BreakpointPosition pos) {
        myTaskStack.add(pos);
    }

    public void onTargetEnd() {
        if (!myTaskStack.isEmpty()) {
            myTaskStack.remove(myTaskStack.size() - 1);
        }
    }

    private int getStackSize() {
        return myTaskStack.size();
    }

    private BreakpointPosition getCurrentTarget() {
        return !myTaskStack.isEmpty() ? myTaskStack.get(myTaskStack.size() - 1) : null;
    }

    public boolean isCurrentTarget(Location loc) {
        BreakpointPosition currentPos = getCurrentTarget();
        if (currentPos != null && loc != null) {
            BreakpointPosition pos = new BreakpointPosition(loc);
            return pos.equals(currentPos);
        }
        return false;
    }
}
