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
/*
	columba: a Java open source email client
	http://columba.sourceforge.net/

	Filename: OSInfo.java
	Author: Hrk (Luca Santarelli) <hrk@users.sourceforge.net>
	Comments: this is a util class which provides a quick and easy way to get useful informations on the OS where the JVM is running.
*/

package org.columba.core.util;

public class OSInfo {

	private static final String UNKNOWN = new String();

	//Public methods
	//Platform identifiers: Windows, Linux, Mac OS, ...
	public static boolean isWin32Platform() {
		return (isWindowsPlatform() || isWinNTPlatform());
	}
	
	public static boolean isWinNTPlatform() {
		return (isWinNT() || isWin2K() || isWinXP());
	}
	
	public static boolean isWindowsPlatform() {
		return (isWin95() || isWin98() || isWinME());
	}
	
	//Single OS identifiers: Window 95, Window 98, ...
	public static boolean isWin95() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows 95"));
	}
	
	public static boolean isWin98() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows 98"));
	}
	
	public static boolean isWinME() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows ME"));
	}
	
	public static boolean isWinNT() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows NT"));
	}
	
	public static boolean isWin2K() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows 2000"));
	}
	
	public static boolean isWinXP() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Windows XP")); //Not tested.
	}

	public static boolean isLinux() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Linux"));
	}
	
	public static boolean isSolaris() {
		String sOSName = System.getProperty("os.name", UNKNOWN);
		return (sOSName.equalsIgnoreCase("Solaris"));
	}

	//User home
/*
	public static String userHome() {
		return "TO DO!!";
	}
*/

}
