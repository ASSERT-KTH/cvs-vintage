/* $Id: ForwardWithQuery.java,v 1.2 1999/10/14 23:48:48 akv Exp $
 */

/**
 * Test FORWARD with query string
 *
 * @author Arun Jamwal [arunj@eng.sun.com]
 */

package tests.dispatch;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tools.moo.URLHelper;

public class ForwardWithQuery extends TestableBase {


  public String getDescription() {
    return "Forwarding With Query String Test";
  }

  public TestResult runTest() {
    TestResult testResult = new TestResult();
  
    hash = new Hashtable();
    props = new Properties();
    init();       
    String expBuf = loadQuery(hash);

    try {       
      URL url = URLHelper.getURL(urlEncodeQuery(hash));
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setFollowRedirects(false);
      con.connect();
      String ct = con.getContentType();

      if (! ct.equals(ContentType)) {
        testResult.setStatus(false);
        testResult.setMessage("Wrong content type. Got: " + ct);
  
        return testResult; 
      }
  
      InputStream in = con.getInputStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(in));
      String s = r.readLine();

      if (s.equals(TargetTag)) {
        testResult.setStatus(true);

        // Read the query string back
        StringBuffer recvBuf = new StringBuffer("");

        while((s = r.readLine()) != null) {
          recvBuf.append(s);
        }
        if (this.debug) {
          System.out.println("expected: " + expBuf);
          System.out.println("recevied: " + recvBuf);
        }
        if (!recvBuf.toString().equals(expBuf.toString())) {
          testResult.setStatus(false);   
          testResult.setMessage("Incorrect content in forward");
        }

        return testResult;
      } else {
        testResult.setStatus(false);
        testResult.setMessage("Incorrect target executed");
   
        return testResult;
      }
    } catch (Exception e) {
      testResult.setStatus(false);
      testResult.setMessage("Exception: " + e);
    
      return testResult; 
    }
  }

  /**
   * load properties file containing name/value pairs
   */
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


  /**
   * generate expected return query string 
   */
  private String loadQuery(Hashtable hash) {
    boolean status = true;

    ServletName = props.getProperty("ServletName");
    TargetTag = props.getProperty("TargetTag");
    ContentType = props.getProperty("ContentType");
    String debugS = props.getProperty("Debug");
    debug = Boolean.valueOf(debugS).booleanValue(); 


    StringBuffer expBuf = new StringBuffer();
    String testsKey = props.getProperty("QueryStrings");
  
    if (testsKey != null) {
      Vector tests = new Vector();
      StringTokenizer stok = new StringTokenizer(testsKey, ",");
  
      while (stok.hasMoreTokens()) {
        tests.addElement(stok.nextToken().trim());
      }

      Enumeration testNames = tests.elements();

      while (testNames.hasMoreElements()) {
        boolean localStatus = true;
        String testId = (String)testNames.nextElement();
        String qName = "query." + testId + ".name";
        String qValue = "query." + testId + ".value";

        hash.put(props.getProperty(qName),
          props.getProperty(qValue));
      }   

      Enumeration e = hash.keys();

      boolean firstPair = true;
      while(e.hasMoreElements()) {
        if (firstPair) 
          firstPair = false;
        else 
          expBuf.append("&");
        String key = (String)e.nextElement();
        expBuf.append(key).append("=").append((String)hash.get(key));
      }
    }
    return expBuf.toString();
  }

  private String urlEncodeQuery(Hashtable hash) {
    StringBuffer query = new StringBuffer(this.ServletName).append("?");  
    Enumeration e = hash.keys();
    boolean firstPair = true;

    while(e.hasMoreElements()) {
      if (firstPair) 
        firstPair = false;
      else 
        query.append("&");
      String key = (String)e.nextElement();
      query.append(URLEncoder.encode(key)).append("=");
      query.append(URLEncoder.encode((String)hash.get(key)));
    }
    return query.toString();
  }


  private boolean debug = false;
  private static String ServletName = null;
  private static String TargetTag = null;
  private static String ContentType = null;
  private Properties props = null;
  private Hashtable hash = null;
  private static final String PropFileName = "ForwardWithQuery.prop"; 
}
