/*
 * $Id: HttpDateTest.java,v 1.2 1999/10/14 23:49:04 akv Exp $
 */

package tests.y2k;

import org.apache.tools.moo.TestableBase;
import org.apache.tools.moo.TestResult;
import org.apache.tomcat.util.*;
import java.text.*;
import java.util.*;

/**
 * Sample test module.
 */

public class HttpDateTest extends TestableBase {

    String[] rfc1123Strings = new String[11];
    Calendar[] calendars = new Calendar[11];

    public HttpDateTest() {
	rfc1123Strings[0] = "Thu, 31 Dec 1998 00:00:00 GMT";
	calendars[0] = new GregorianCalendar(1998,Calendar.DECEMBER,31,0,0,0);
	calendars[0].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[1] = "Fri, 01 Jan 1999 00:00:00 GMT";
	calendars[1] = new GregorianCalendar(1999,Calendar.JANUARY,1,0,0,0);
	calendars[1].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[2] = "Thu, 09 Sep 1999 00:00:00 GMT";
	calendars[2] = new GregorianCalendar(1999,Calendar.SEPTEMBER,9,0,0,0);
	calendars[2].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[3] = "Fri, 10 Sep 1999 00:00:00 GMT";
	calendars[3] = new GregorianCalendar(1999,Calendar.SEPTEMBER,10,0,0,0);
	calendars[3].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[4] = "Fri, 31 Dec 1999 00:00:00 GMT";
	calendars[4] = new GregorianCalendar(1999,Calendar.DECEMBER,31,0,0,0);
	calendars[4].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[5] = "Sat, 01 Jan 2000 00:00:00 GMT";
	calendars[5] = new GregorianCalendar(2000,Calendar.JANUARY,1,0,0,0);
	calendars[5].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[6] = "Mon, 28 Feb 2000 00:00:00 GMT";
	calendars[6] = new GregorianCalendar(2000,Calendar.FEBRUARY,28,0,0,0);
	calendars[6].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[7] = "Tue, 29 Feb 2000 00:00:00 GMT";
	calendars[7] = new GregorianCalendar(2000,Calendar.FEBRUARY,29,0,0,0);
	calendars[7].setTimeZone(TimeZone.getTimeZone("GMT"));

	rfc1123Strings[8] = "Wed, 01 Mar 2000 00:00:00 GMT";
	calendars[8] = new GregorianCalendar(2000,Calendar.MARCH,1,0,0,0);
	calendars[8].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[9] = "Sun, 31 Dec 2000 00:00:00 GMT";
	calendars[9] = new GregorianCalendar(2000,Calendar.DECEMBER,31,0,0,0);
	calendars[9].setTimeZone(TimeZone.getTimeZone("GMT"));
	
	rfc1123Strings[10] =  "Mon, 01 Jan 2001 00:00:00 GMT";
	calendars[10] = new GregorianCalendar(2001,Calendar.JANUARY,1,0,0,0);
	calendars[10].setTimeZone(TimeZone.getTimeZone("GMT"));	
	
    }

    public String getDescription() {
        return "Http Date y2k Test";
    }

    public TestResult runTest() {
        TestResult testResult = new TestResult();

	for(int i = 0; i < rfc1123Strings.length; i++) {
	    boolean result = testDate(rfc1123Strings[i], calendars[i]);
	    if (result == false) {
		testResult.setStatus(false);
		testResult.setMessage("Failed at: " + rfc1123Strings[i]);
		return testResult;
	    }
	}
	
        testResult.setStatus(true);
        return testResult;
    }

    private boolean testDate(String rfc1123String, Calendar calendar) {

	boolean phase1result = false;
	boolean phase2result = false;
	
	// first create an HttpDate based off of the rfc1123 string
	// and see if we get the correct number of milliseconds


	HttpDate httpDate = new HttpDate();
	httpDate.parse(rfc1123String);
	long httpDateMillis = httpDate.getTime();
	long calendarMillis = calendar.getTime().getTime();
	if (httpDateMillis == calendarMillis) {
	    phase1result = true;
	} 
	DateFormat df = new SimpleDateFormat();
	df.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date fooDate = new Date(httpDateMillis);
	
	// now create a HttpDate from the millis count of the
	// calendar and see if we get the correct header

	HttpDate httpDate2 = new HttpDate(calendar.getTime().getTime());
	String httpDateString = httpDate2.toString();
	if (httpDateString.equals(rfc1123String)) {
	    phase2result = true;
	}

	if (phase1result == true && phase2result == true) {
	    //System.out.println("TEST: " + rfc1123String + " OK");
	    return true;
	} else {
	    //System.out.println("TEST: " + rfc1123String + " FAIL");
	    return false;
	}

    }    
}
