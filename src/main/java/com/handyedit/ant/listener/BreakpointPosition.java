package com.handyedit.ant.listener;

import org.apache.tools.ant.Location;

/**
 * @author Alexei Orischenko
 *         Date: Dec 9, 2009
 */
public class BreakpointPosition {
    private int myLine;
    private String myFile;

    public BreakpointPosition() {
    }

    public BreakpointPosition(int line, String file) {
        myLine = line;
        myFile = file;
    }

    public BreakpointPosition(Location loc) {
        myLine = loc.getLineNumber() - 1;
        myFile = loc.getFileName();
    }

    public int getLine() {
        return myLine;
    }

    public void setLine(int line) {
        myLine = line;
    }

    public int hashCode() {
        return myLine;
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof BreakpointPosition) {
            BreakpointPosition pos = (BreakpointPosition) o;
            return pos.myLine == myLine && myFile != null && myFile.equals(pos.myFile);
        }
        return false;
    }

    @Override
    public String toString() {
        return myFile + ":" + myLine; 
    }
}

