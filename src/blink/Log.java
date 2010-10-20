package blink;

import java.io.PrintWriter;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Log {
    public Log() {
    }

    public static final int MAX_LEVEL = 4;

    private static PrintWriter _log[];
    static {
        try {
            _log = new PrintWriter[] {
                   new PrintWriter("c:/workspace/blink/log/log0.txt"),
                   new PrintWriter("c:/workspace/blink/log/log1.txt"),
                   new PrintWriter("c:/workspace/blink/log/log2.txt"),
                   new PrintWriter("c:/workspace/blink/log/log3.txt"),
                   new PrintWriter("c:/workspace/blink/log/log4.txt")
            };
        }
        catch (Exception e) {
            System.out.println("Could not create log");
        }
    }
    public static void log(int level, String s) {
        _log[level].println(s);
        _log[level].flush();
    }
}
