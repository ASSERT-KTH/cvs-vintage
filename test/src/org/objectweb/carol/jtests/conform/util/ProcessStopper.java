package org.objectweb.carol.jtests.conform.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


/**
 * @author  Vadim Nasardinov (vadimn@redhat.com)
 * @version $Id: ProcessStopper.java,v 1.2 2005/02/01 18:40:02 el-vadimo Exp $
 * @since   2005-01-04
 * @see ProcessStopper
 **/
public class ProcessStopper {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException
                ("expected the port number, but got: " +
                 Arrays.asList(args));
        }
        final int port = Integer.parseInt(args[0]);
        final String host = "localhost";

        final Socket socket;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            throw new RuntimeException("no such host: " + host, ex);
        } catch (IOException ex) {
            System.err.println
                ("ProcessStopper: couldn't connect to " +
                 host + ":" + port);
            return;
        }

        final BufferedWriter writer;

        try {
            writer = new BufferedWriter
            (new OutputStreamWriter(socket.getOutputStream(),
                                    ProcessRunner.STREAM_ENCODING));
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
