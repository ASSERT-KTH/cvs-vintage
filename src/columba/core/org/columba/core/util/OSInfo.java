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
        
        private static final String OS_NAME = "os.name";

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
		return "Windows 95".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isWin98() {
		return "Windows 98".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isWinME() {
		return "Windows ME".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isWinNT() {
		return "Windows NT".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isWin2K() {
		return "Windows 2000".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isWinXP() {
		return "Windows XP".equalsIgnoreCase(System.getProperty(OS_NAME));
	}

	public static boolean isLinux() {
		return "Linux".equalsIgnoreCase(System.getProperty(OS_NAME));
	}
	
	public static boolean isSolaris() {
		return "Solaris".equalsIgnoreCase(System.getProperty(OS_NAME));
	}

	//User home
/*
	public static String userHome() {
		return "TO DO!!";
	}
*/

}
