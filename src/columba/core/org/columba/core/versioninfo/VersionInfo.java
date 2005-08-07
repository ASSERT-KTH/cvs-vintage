//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.versioninfo;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;


public class VersionInfo {

	// Use static variables so the date is only updated once
	// during runtime
	private static Date TODAY = new Date();
	
	private static String YEAR = Integer.toString( Calendar.getInstance().get(Calendar.YEAR) );
	private static String MONTH = Integer.toString( Calendar.getInstance().get(Calendar.MONTH) + 1);
	private static String DAY = Integer.toString( Calendar.getInstance().get(Calendar.DAY_OF_MONTH) );
	
	private static String DATE = YEAR + (MONTH.length() == 1 ? "0" : "") + MONTH + (DAY.length() == 1 ? "0" : "") + DAY; 
	
	public static String getVersion() {
		try {
			Method getVersionMethod = Class.forName("org.columba.core.main.ColumbaVersionInfo").getMethod("getVersion", new Class[0]);
			
			return (String) getVersionMethod.invoke(null,new Object[0]);
		} catch (Exception e) {
			return "CVS-" + DATE;
		} 
	
	}

	public static Date getBuildDate() {
		try {
			Method getBuildDateMethod = Class.forName("org.columba.core.main.ColumbaVersionInfo").getMethod("getBuildDate", new Class[0]);
			
			return (Date) getBuildDateMethod.invoke(null,new Object[0]);
		} catch (Exception e) {
			return TODAY;
		} 
	
	}

}
