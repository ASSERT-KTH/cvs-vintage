
/*
 * $Id: CounterClient.java,v 1.2 1999/10/14 23:49:00 akv Exp $
 */

package tests.perf;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.EOFException;
import java.net.MalformedURLException;
import java.lang.SecurityException;
import java.lang.NumberFormatException;

public class CounterClient extends TestableBase {

    private String method = null;
    private URL resetURL = null;
    private URL incrementURL = null;
    private URLConnection connection = null;
    private static final String MethodName = "method";
    private static final String OperationName = "operation";
    private static final String HTML = "html";
    private static final String Octet = "octet";
    private static final String Reset = "reset";
    private static final String Increment = "increment";
    private static final String CounterFile = "/servlet/Counter";
    private static final String Host = "localhost";
    private static final int Port = 8080;
    private static final int Iterations = 1000;
    private static final boolean Debug = true;

    public String getDescription() {
        return "serlvet ping test";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();
        String elapsedTime = new String("");
        boolean returnStatus = false;

        try {
            init();
            elapsedTime = this.run();

            returnStatus = true;
        } catch (MalformedURLException mue) {
	    if (this.Debug) {
                mue.printStackTrace();
            }
        }

        testResult.setStatus(returnStatus);
        testResult.setMessage("elapsed time: " + elapsedTime);

        return testResult;
    }

    private void init() throws MalformedURLException {
        Properties sysProps = null;

        try {
            sysProps = System.getProperties();
        } catch (SecurityException se) {
	    if (this.Debug) {
                se.printStackTrace();
            }

            sysProps = new Properties();
        }

        this.method = sysProps.getProperty(this.MethodName, this.HTML);

        resetURL = URLHelper.getURL(this.CounterFile + "?" +
            URLEncoder.encode(this.MethodName) + "=" +
            URLEncoder.encode(this.method) + "&" +
            URLEncoder.encode(this.OperationName) + "=" +
            URLEncoder.encode(this.Reset));
        incrementURL = URLHelper.getURL(this.CounterFile + "?" +
            URLEncoder.encode(this.MethodName) + "=" +
            URLEncoder.encode(this.method) + "&" +
            URLEncoder.encode(this.OperationName) + "=" +
            URLEncoder.encode(this.Increment));
    }

    private String run() {
        try {
            connection = resetURL.openConnection();
        } catch (IOException ioe) {
            if (this.Debug) {
                ioe.printStackTrace();
            }

            System.out.println("can't establish a connection");
            //System.exit(-1);
        }

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);

        InputStream in = null;

        try {
            connection.connect();

            in = connection.getInputStream();
        } catch (IOException ioe) {
	    if (this.Debug) {
                ioe.printStackTrace();
            }

            System.out.println("can't connect to reset service");
            //System.exit(-1);
        }

        byte[] b = new byte[4];
        int count = 0;

        try {
            in.read(b);
            count = Integer.parseInt(new String(b).trim());
        } catch (IOException ioe) {
	    if (this.Debug) {
                ioe.printStackTrace();
            }

            System.out.println("can't read in data");
        } catch (NumberFormatException nfe) {
	    if (this.Debug) {
                nfe.printStackTrace();
	    }

            System.out.println("can't read in data");
        }

        long startTime = System.currentTimeMillis();

        for (int  i = 0; i < this.Iterations; i++) {
            try {
                connection = incrementURL.openConnection();
            } catch (IOException ioe) {
                if (this.Debug) {
                    ioe.printStackTrace();
                }

                System.out.println("can't establish a connection");
                //System.exit(-1);
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);

            try {
                connection.connect();

                in = connection.getInputStream();
            } catch (IOException ioe) {
	        if (this.Debug) {
                    ioe.printStackTrace();
                }

                System.out.println("can't connect to reset service");
                //System.exit(-1);
            } catch (NumberFormatException nfe) {
	        if (this.Debug) {
                    nfe.printStackTrace();
	        }

                System.out.println("can't read in data");
            }

            try {
                in.read(b);
                count = Integer.parseInt(new String(b).trim());
            } catch (IOException ioe) {
                if (this.Debug) {
                    ioe.printStackTrace();
                }

                System.out.println("can't read in data");
            }
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
	String runTime = (elapsedTime / 1000) + "." + (elapsedTime % 1000);

        //System.out.println("Count: " + count);
        //System.out.println("Time: " + (elapsedTime / this.Iterations));

        return runTime;
    }
}
