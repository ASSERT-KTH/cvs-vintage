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
 * $Id: ProcessStopper.java,v 1.3 2005/02/11 10:12:59 benoitf Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.jtests.conform.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Vadim Nasardinov (vadimn@redhat.com)
 * @since 2005-01-04
 * @see ProcessStopper
 */
public class ProcessStopper {

    /**
     * No default constructor, utility class
     */
    private ProcessStopper() {

    }

    /**
     * Main method
     * @param args arguments of the program
     * @throws IOException
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("expected the port number, but got: " + Arrays.asList(args));
        }
        final int port = Integer.parseInt(args[0]);
        final String host = "localhost";

        final Socket socket;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            throw new RuntimeException("no such host: " + host, ex);
        } catch (IOException ex) {
            System.err.println("ProcessStopper: couldn't connect to " + host + ":" + port);
            return;
        }

        final BufferedWriter writer;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), ProcessRunner.STREAM_ENCODING));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("can't happen", ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            writer.write(ProcessRunner.SHUTDOWN_COMMAND);
            writer.newLine();
            writer.flush();
            socket.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error sending a shutdown command", ex);
        }
    }
}