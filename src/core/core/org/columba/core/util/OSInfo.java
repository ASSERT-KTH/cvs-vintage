// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

/*
	columba: a Java open source email client
	http://columba.sourceforge.net/

	Filename: OSInfo.java
	Author: Hrk (Luca Santarelli) <hrk@users.sourceforge.net>
	Comments: this is a util class which provides a quick and easy way to get useful informations on the OS where the JVM is running.
*/

package org.columba.core.util;

public class OSInfo {
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
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows 95"));
	}
	public static boolean isWin98() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows 98"));
	}
	public static boolean isWinME() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows ME"));
	}
	public static boolean isWinNT() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows NT"));
	}
	public static boolean isWin2K() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows 2000"));
	}
	public static boolean isWinXP() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Windows XP")); //Not tested.
	}

	public static boolean isLinux() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Linux"));
	}
	public static boolean isSolaris() {
		String sOSName = System.getProperty("os.name", "not_found");
		return (sOSName.equalsIgnoreCase("Solaris"));
	}

	//User home
/*
	public static String userHome() {
		return "TO DO!!";
	}
*/

};