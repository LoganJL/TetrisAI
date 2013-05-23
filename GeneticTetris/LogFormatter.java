// We control the format of the log using this functions

import java.util.logging.*;
import java.io.*;
import java.util.Arrays;

public class LogFormatter extends Formatter {
  //FORMAT: [LEVEL] Information
  //
  //INFO Grid: 0 1 2 3 4 5 ... <10x20 = 200 numbers>

  public static final int NO_OF_ROWS = 21;
  public static final int NO_OF_COLUMNS = 11;

  public String format(LogRecord record) {

    StringBuffer log_line = new StringBuffer();
    String level = record.getLevel().getName();
    if (level == "FINE") {
      level = "DEBUG";
    }
    String message = record.getMessage();
    // StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    // String function_name = "";
    // for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
    //   function_name += ste.getMethodName() + " | ";
    // }

     // = stackTrace[stackTrace.length-2].getMethodName(); //get the name 
    
    // Object params[] = record.getParameters();
    // if (params != null) {
    //   // System.out.println(record.getParameters().length);
    //   // int[] data = (int[])record.getParameters()[0];
    //   LogData data = (LogData)record.getParameters()[0];
    //   // System.out.println(data.test());
    //   log_line.append(String.format( "@%s | %s\n %s \n", level, formatMessage(record), data.gameInfo() ));
    // } else {
    //   // System.out.println(level);
    //   // System.out.println(formatMessage(record));
    //   // System.out.println(log_data);
    //   // System.out.println("-------------------------------------------------------\n");
    log_line.append(String.format(  "@%-10s %s\n", level, message ));
    // }

    return log_line.toString();
  }
}