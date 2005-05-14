/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Command
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Command {

    private String cmd;
    private Process process;
    private Output out;
    private Output err;
    private PrintWriter in;

    public Command(String cmd) {
        this.cmd = cmd;
    }

    public String getCommand() {
        return cmd;
    }

    public void start() throws IOException {
        process = Runtime.getRuntime().exec(cmd);
        out = new Output(process.getInputStream());
        err = new Output(process.getErrorStream());
        in = new PrintWriter
            (new OutputStreamWriter(process.getOutputStream()));
    }

    public PrintWriter getInput() {
        return in;
    }

    public void write(String str) {
        in.print(str);
    }

    public void writeln(String str) {
        in.println(str);
    }

    public Output getOutput() {
        return out;
    }

    public boolean expect(String expected) throws InterruptedException {
        return out.expect(expected);
    }

    public Output getError() {
        return err;
    }

    public boolean expectError(String expected) throws InterruptedException {
        return err.expect(expected);
    }

    public String getResult() {
        String out = this.out.toString();
        String err = this.err.toString();
        StringBuffer result = new StringBuffer();
        if (!out.equals("")) {
            result.append(" --- Standard Out ---\n");
            result.append(out);
        }
        if (!err.equals("")) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(" --- Standard Error ---\n");
            result.append(err);
        }
        return result.toString();
    }

    public void waitFor() throws InterruptedException {
        process.waitFor();
        out.waitFor();
        err.waitFor();
    }

    public class Output extends Thread {

        private Reader in;
        private StringWriter out;
        private int off = 0;
        private boolean done = false;

        Output(InputStream in) {
            this.in = new InputStreamReader(in);
            this.out = new StringWriter();
            start();
        }

        public synchronized String toString() {
            return out.toString();
        }

        public synchronized boolean expect(String expected)
            throws InterruptedException {
            while (true) {
                String str = out.toString();
                int idx = str.indexOf(expected, off);
                if (idx >= 0) {
                    off = idx;
                    return true;
                } else if (done) {
                    return false;
                } else {
                    wait();
                }
            }
        }

        public synchronized void waitFor() throws InterruptedException {
            while (!done) { wait(); }
        }

        public void run() {
            try {
                char[] buf = new char[4*1024];
                while (true) {
                    int n = in.read(buf);
                    synchronized (this) {
                        if (n == -1) {
                            done = true;
                            notifyAll();
                            return;
                        } else {
                            out.write(buf, 0, n);
                            out.flush();
                            notifyAll();
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                synchronized (this) {
                    done = true;
                    notifyAll();
                }
            }
        }
    }

}
