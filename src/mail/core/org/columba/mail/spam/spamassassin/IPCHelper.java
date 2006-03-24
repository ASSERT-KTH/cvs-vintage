package org.columba.mail.spam.spamassassin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.columba.core.io.StreamUtils;


/*
 * Created on 15.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IPCHelper {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.mail.spam.spamassassin"); //$NON-NLS-1$
	
    protected StreamThread outputStream = null;
    protected StreamThread errorStream = null;
    protected OutputStream inputStream = null;
    protected Process p;

    public IPCHelper() {
    }

    /**
     *
     * execute command
     *
     * initialize streams
     *
     * @param command
     * @throws Exception
     */
    public void executeCommand(String command) throws Exception {
        p = Runtime.getRuntime().exec(command);

        errorStream = new StreamThread(p.getErrorStream());
        outputStream = new StreamThread(p.getInputStream());
        inputStream = p.getOutputStream();

        errorStream.start();
        outputStream.start();
    }

    public void send(String in) throws Exception {
        inputStream.write(in.getBytes());
        inputStream.flush();
        inputStream.close();
    }

    public void send(InputStream in) throws Exception {
        StreamUtils.streamCopy(in, inputStream);
        inputStream.flush();
        inputStream.close();
    }

    public int waitFor() throws Exception {
        int exitVal = p.waitFor();

        return exitVal;
    }

    /**
     *
     * return error
     *
     * @return @throws
     *         Exception
     */
    public String getErrorString() throws Exception {
        String str = errorStream.getBuffer();

        return str;
    }

    /**
     *
     * return output
     *
     * @return @throws
     *         Exception
     */
    public String getOutputString() throws Exception {
        String str = outputStream.getBuffer();

        return str;
    }

    /*
     * wait for stream threads to die
     *
     */
    public void waitForThreads() throws Exception {
        outputStream.join();
        errorStream.join();
    }

    public class StreamThread extends Thread {
        InputStream is;
        StringBuffer buf;

        public StreamThread(InputStream is) {
            this.is = is;

            buf = new StringBuffer();
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                while ((line = br.readLine()) != null) {
                    LOG.info(">" + line); //$NON-NLS-1$
                    buf.append(line + "\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public String getBuffer() {
            return buf.toString();
        }
    }
}
