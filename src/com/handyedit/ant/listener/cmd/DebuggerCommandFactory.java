package com.handyedit.ant.listener.cmd;

import com.handyedit.ant.listener.BreakpointManager;
import com.handyedit.ant.listener.BreakpointPosition;
import com.handyedit.ant.listener.DebuggerCommand;
import com.handyedit.ant.listener.TempBreakpointType;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class DebuggerCommandFactory {

    // commands sent to build listener from IDE
    public static final String CMD_SET_BREAKPOINT = "set";
    public static final String CMD_REMOVE_BREAKPOINT = "remove";
    public static final String CMD_RESUME_EXECUTION = "resume";
    public static final String CMD_RUN_TO_CURSOR = "run-to";
    public static final String CMD_SET_TEMP_BREAKPOINT = "temp-breakpoint";

    private BreakpointManager myManager;

    public DebuggerCommandFactory(BreakpointManager manager) {
        myManager = manager;
    }

    public DebuggerCommand create(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        String type = args[0];

        if (CMD_SET_BREAKPOINT.equals(type) && args.length == 3) {
            return createAddBreakpointCommand(parseLocation(args[1]), args[2]);
        }
        if (CMD_REMOVE_BREAKPOINT.equals(type) && args.length == 3) {
            return createRemoveBreakpointCommand(parseLocation(args[1]), args[2]);
        }
        if (CMD_RESUME_EXECUTION.equals(type)) {
            return createResumeCommand();
        }
        if (CMD_RUN_TO_CURSOR.equals(type) && args.length == 3) {
            return createRunToCommand(parseLocation(args[1]), args[2]);
        }
        if (CMD_SET_TEMP_BREAKPOINT.equals(type) && args.length == 2) {
            return createTempBreakpoint(parseBreakpointType(args[1]));
        }
        return null;
    }

    private int parseLocation(String s) {
        return Integer.parseInt(s);
    }

    private TempBreakpointType parseBreakpointType(String val) {
        try {
            int value = Integer.parseInt(val);
            TempBreakpointType result = TempBreakpointType.get(value);
            if (result != null) {
                return result;
            }
        } catch (NumberFormatException e) {
        }

        return TempBreakpointType.OVER;
    }

    public DebuggerCommand createBreakpointCommand(String[] args) {
        if (isBreakpointCommand(args) && args.length == 3) {
            return createAddBreakpointCommand(parseLocation(args[1]), args[2]);
        }
        return null;
    }

    public boolean isBreakpointCommand(String[] args) {
        return CMD_SET_BREAKPOINT.equals(args[0]);
    }

    private DebuggerCommand createAddBreakpointCommand(final int loc, final String file) {
        return new DebuggerCommand() {
            public void execute(BufferedWriter out) {
                myManager.add(new BreakpointPosition(loc, file));
            }
        };
    }

    private DebuggerCommand createRemoveBreakpointCommand(final int loc, final String file) {
        return new DebuggerCommand() {
            public void execute(BufferedWriter out) {
                myManager.remove(new BreakpointPosition(loc, file));
            }
        };
    }

    private DebuggerCommand createResumeCommand() {
        return new DebuggerCommand() {
            public void execute(BufferedWriter out) {
                myManager.resume();
            }
        };
    }

    private DebuggerCommand createRunToCommand(final int loc, final String file) {
        return new DebuggerCommand() {
            public void execute(BufferedWriter out) {
                myManager.addRunTo(new BreakpointPosition(loc, file));
            }
        };
    }

    private DebuggerCommand createTempBreakpoint(final TempBreakpointType type) {
        return new DebuggerCommand() {
            public void execute(BufferedWriter out) throws IOException {
                myManager.addTemp(type);
            }
        };
    }
}
