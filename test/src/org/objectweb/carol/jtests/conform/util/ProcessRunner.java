/**
 * Copyright (C) 2005 - Red Hat, Inc. All rights reserved.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * --------------------------------------------------------------------------
 * $Id: ProcessRunner.java,v 1.4 2005/02/11 10:12:59 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Calls the <code>main()</code> method of the specified Java class and waits
 * for a shutdown command from {@link ProcessStopper}.
 * <p>
 * This allows you to programmatically shut down Java processes that don't
 * normally provide a way of doing that.
 * </p>
 * <p>
 * Upon starting the process, <code>ProcessRunner</code> binds to a specified
 * TCP port waiting for a shutdown command from {@linkProcessStopper}.
 * </p>
 * <p>
 * Upon receiving the shutdown command, <code>ProcessRunner</code> exits, thus
 * stopping the previously started process and touches a file whose name is
 * specified by the <code>process.runner.exit.stamp</code> property. This file
 * signals successful shutdown and its presence can be used as a condition by
 * Ant's <a href="http://ant.apache.org/manual/CoreTasks/waitfor.html">WaitFor
 * </a> task.
 * </p>
 * @author Vadim Nasardinov (vadimn@redhat.com)
 * @since 2005-01-04
 * @see ProcessStopper
 */
public class ProcessRunner {

    /**
     * Encoding value
     */
    public static final String STREAM_ENCODING = "US-ASCII";

    /**
     * Command used to shutdown
     */
    public static final String SHUTDOWN_COMMAND = "shutdown";

    /**
     * No default constructor, utility class
     */
    private ProcessRunner() {

    }

    /**
     * Main method
     * @param args arguments of the program
     * @throws IOException
     */
    public static void main(String[] args) {
        deleteExitStamp();

        final int nServerArgs = args.length - 1;
        if (nServerArgs < 0) {
            throw new IllegalArgumentException("server class name expected");
        }
        final String serverClass = args[0];
        final String[] serverArgs = new String[nServerArgs];
        System.arraycopy(args, 1, serverArgs, 0, nServerArgs);

        final int remaining = getNShutdowns();
        final Runner runner = new Runner(serverClass, serverArgs, remaining);
        runner.start();

        int listenerPort = getListenerPort();

        ServerSocket listenerSocket;
        try {
            listenerSocket = new ServerSocket(listenerPort);
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't bind to " + listenerPort, ex);
        }

        try {
            for (int ii = 0; ii < remaining; ii++) {
                new Listener(listenerPort, listenerSocket.accept(), runner).start();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error accepting connection", ex);
        }
    }

    private static String getExitStamp() {
        String exitStamp = System.getProperty("process.runner.exit.stamp");
        if ("".equals(exitStamp)) {
            exitStamp = null;
        }
        return exitStamp;
    }

    private static void deleteExitStamp() {
        final String exitStamp = getExitStamp();

        if (exitStamp != null) {
            File exitStampFile = new File(exitStamp);
            if (exitStampFile.exists()) {
                exitStampFile.delete();
            }
        }
    }

    private static int getListenerPort() {
        String TCP_LISTENER_PORT = "process.runner.tcp.port";

        String port = System.getProperty(TCP_LISTENER_PORT);
        if (port == null) {
            throw new IllegalArgumentException("The " + TCP_LISTENER_PORT + " property not set");
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            throw (IllegalArgumentException) new IllegalArgumentException("The value of " + TCP_LISTENER_PORT
                    + " is not a integer: " + port).initCause(ex);
        }
    }

    private static int getNShutdowns() {
        String N_SHUTDOWNS = "process.runner.n.shutdowns";

        String nShutdowns = System.getProperty(N_SHUTDOWNS);
        if (nShutdowns == null) {
            throw new IllegalArgumentException("The " + N_SHUTDOWNS + " property not set");
        }
        try {
            return Integer.parseInt(nShutdowns);
        } catch (NumberFormatException ex) {
            throw (IllegalArgumentException) new IllegalArgumentException("The value of " + N_SHUTDOWNS
                    + " is not a integer: " + nShutdowns).initCause(ex);
        }
    }

    private static class Listener extends Thread {

        private Socket m_socket;

        private Runner m_runner;

        Listener(int port, Socket socket, Runner runner) {
            m_socket = socket;
            m_runner = runner;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream(),
                        STREAM_ENCODING));
                String command;
                while (null != (command = reader.readLine())) {
                    if (SHUTDOWN_COMMAND.equals(command)) {
                        possiblyShutdown();

                        try {
                            m_socket.close();
                        } catch (IOException ex) {
                            ;
                        }
                        break;
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("can't happen", ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void possiblyShutdown() {
            synchronized (m_runner) {
                m_runner.decrementRemaining();
                if (!m_runner.hasRemaining()) {
                    final String exitStamp = getExitStamp();
                    if (exitStamp != null) {
                        File exitStampFile = new File(exitStamp);
                        try {
                            exitStampFile.createNewFile();
                        } catch (IOException ex) {
                            throw new RuntimeException("Couldn't create " + exitStamp, ex);
                        }
                    }
                    System.exit(0);
                }
            }
        }
    }

    private static class Runner extends Thread {

        private final String[] m_serverArgs;

        private final Method m_main;

        private int m_nRemaining;

        Runner(String serverClassname, String[] serverArgs, int remaining) {
            m_nRemaining = remaining;
            m_serverArgs = serverArgs;

            final Class serverClass;
            try {
                serverClass = Class.forName(serverClassname);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(serverClassname, ex);
            }

            try {
                m_main = serverClass.getMethod("main", new Class[] {String[].class});
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(serverClassname + " does not have a main method", ex);
            } catch (SecurityException ex) {
                throw new RuntimeException(serverClassname + " does not have a public main method", ex);
            }
            setDaemon(true);
        }

        /**
         * Not a thread-safe method. Caller must synchronize.
         */
        public void decrementRemaining() {
            m_nRemaining--;
        }

        /**
         * Not a thread-safe method. Caller must synchronize.
         */
        public boolean hasRemaining() {
            return m_nRemaining > 0;
        }

        public void run() {
            try {
                m_main.invoke(null, new Object[] {m_serverArgs});
            } catch (IllegalAccessException ex) {
                die(ex);
            } catch (IllegalArgumentException ex) {
                die(ex);
            } catch (InvocationTargetException ex) {
                die(ex);
            }
        }

        private static void die(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}