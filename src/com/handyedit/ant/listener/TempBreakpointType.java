package com.handyedit.ant.listener;

public enum TempBreakpointType {
    INTO(0), OVER(1), OUT(2);

    private int myValue;

    private TempBreakpointType(int value) {
        myValue = value;
    }

    public int getValue() {
        return myValue;
    }

    public static TempBreakpointType get(int value) {
        for (TempBreakpointType type: TempBreakpointType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return OVER;
    }
}
