
/*
 * $Id: Post.java,v 1.1 1999/10/09 00:20:57 duncan Exp $
 */

/**
 * test various POST requests
 *
 * @author     Arun Jamwal [arunj@eng.sun.com]
 */

package tests.request;

import com.sun.moo.Testable;
import com.sun.moo.TestResult;
import com.sun.moo.SocketHelper;
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Post implements Testable {

    public String getDescription() {
        return "Method POST Test";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();
        props = new Properties();

        init();

        boolean status = true;
        StringBuffer msg = new StringBuffer("");
        String testsKey = props.getProperty("tests");
        String debugS = props.getProperty("Debug");
        debug = Boolean.valueOf(debugS).booleanValue();   

        if (testsKey != null) {
            Vector tests = new Vector();

            StringTokenizer stok = new StringTokenizer(testsKey, ",");

            while (stok.hasMoreTokens()) {
                tests.addElement(stok.nextToken().trim());
            }

            Enumeration testNames = tests.elements();
  
            while (testNames.hasMoreElements()) {

                String testId = (String)testNames.nextElement();

                if (! test(testId)) {
                     status = false;
                     String description = props.getProperty("test." + testId +
                        ".description");
                     msg.append("\tcan't run test " + testId + " : " +
                         description + "\n");
                }
            }
        }

        testResult.setStatus(status);

        if (msg.length() > 0) {
            testResult.setMessage(msg.toString());
        }

        return testResult;
    }

    private void init() {
        
        InputStream in = 
            this.getClass().getResourceAsStream(PropFileName); 
        if (in != null) {
            try {
                props.load(in);
                in.close();
            } catch (IOException ioe) {
                if (this.debug) {
                    ioe.printStackTrace();
                }
            }
        } else
            System.out.println("Resource file not found: " + PropFileName);
    }


    private boolean test(String testId) {

        boolean responseStatus = dispatch(testId);
        String magnitude = props.getProperty("test." + testId + ".magnitude", "true");
        boolean testCondition = Boolean.valueOf(magnitude).booleanValue(); 

        return (testCondition) ? responseStatus : ! responseStatus;
    }

    private boolean dispatch(String testId) {
        boolean responseStatus = false;

        openIO();

        if (ready()) {
            try {
                writeRequest(testId);
                String response = getResponse();

                String responseKey = props.getProperty("test." + testId + ".response");
                responseStatus =
                    (response.indexOf(responseKey) > -1) ? true : false;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        closeIO();

        return responseStatus; 
    }

    private void openIO() {
        if (ready()) {
            closeIO();
        }
        
        try {
            s = SocketHelper.getSocket();
            pw = new PrintWriter(new OutputStreamWriter(
                s.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(
                s.getInputStream()));
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private boolean ready() {
        return (s != null && pw != null && br != null);
    }

    private void closeIO() {
        try {
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        pw.close();

        try {
            s.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeRequest(String testId)
    throws IOException {
    	String request = props.getProperty("test." + testId + ".request");
        if (request == null) {
            return;
        }
    	String description = props.getProperty("test." + testId + ".description");
    	String response = props.getProperty("test." + testId + ".response");
    	String host = props.getProperty("test." + testId + ".host");
    	String connection = props.getProperty("test." + testId + ".connection");
    	String encoding = props.getProperty("test." + testId + ".encoding");
    	String content = props.getProperty("test." + testId + ".content");


        if (this.debug)
            System.out.println(testId + ". " + description + " Response"  + response);

        if (request != null && request.length() > 0)
            pw.println(request);
        if (host != null && host.length() > 0)
            pw.println("Host: " + host);
        if (encoding != null && encoding.length() > 0)
            pw.println("Content-Encoding: " + encoding);
        if (content != null && content.length() > 0)
            pw.println("Content-Length: " + content.length());
        if (connection != null && connection.length() > 0)
            pw.println("Connection: " + connection);
        pw.println("");
        if (content != null && content.length() > 0) {
            pw.println(content);
            pw.println("");
        }

        pw.flush();
    
    }

    private String getResponse()
    throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = null;

        if (this.debug)
            System.out.println("<--------");
        while ((line = br.readLine()) != null) {
            if (this.debug)
                System.out.println("\t" + line);
            sb.append(line);
            sb.append('\n');
        }
        if (this.debug)
            System.out.println("-------->");

        return sb.toString();
    }



    private Socket s = null;
    private PrintWriter pw = null;
    private BufferedReader br = null;
    private Properties props = null;
        
    private static final String PropFileName = "post.properties"; 
    private boolean debug = false;

}
