package com.handyedit.ant.run;

/**
 * @author Alexei Orischenko
 *         Date: Feb 15, 2010
 */
public enum AntLoggingLevel {
  DEFAULT(0), QUIET(1), VERBOSE(2), DEBUG(3);

  private int myValue;

  AntLoggingLevel(int value) {
    myValue = value;
  }

  public int getValue() {
    return myValue;
  }

  public static AntLoggingLevel get(int value) {
    for (AntLoggingLevel level: AntLoggingLevel.values()) {
        if (level.getValue() == value) {
            return level;
        }
    }
    return DEFAULT;
  }
}
