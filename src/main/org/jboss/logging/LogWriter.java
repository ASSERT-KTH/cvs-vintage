/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/**
 * Allows you to log by writing to a PrintWriter.  Can't just wrap a LogStream
 * since writer doesn't actually call println on the underlying stream.  But we
 * are guaranteed that print and println calls here go through the write(String)
 * method.
 */
public class LogWriter extends java.io.PrintWriter {
    private Log log;

    /**
     * Creates a new writer for the specified log.
     * @param log The log to send messages to.  If null, the default log will
     *            be used.
     */
    public LogWriter(Log log) {
        super(System.out);
        this.log = log;
        if(log == null)
            log = Log.getLog();
    }

    /**
     * Override to do nothing.
     */
    public void flush() { }
    /**
     * Override to do nothing.
     */
    public void println() { }
    /**
     * Override to do nothing.
     */
    public void write(int p0) { }
    /**
     * All the print and println calls go through this method, so it's the one
     * that does the logging.
     */
    public void write(String p0) {log.log(p0); }
    /**
     * Override to do nothing.
     */
    public void write(String p0, int p1, int p2) { }
    /**
     * Override to do nothing.
     */
    public void write(char[] p0) { }
    /**
     * Override to do nothing.
     */
    public void write(char[] p0, int p1, int p2) { }
}
