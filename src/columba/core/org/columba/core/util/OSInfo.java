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

package org.columba.core.util;

/**
 * This is a helper class providing a quick and easy way to obtain useful
 * information about the operating system Columba is running on.
 *
 * @author Hrk (Luca Santarelli) <hrk@users.sourceforge.net>
 */
public class OSInfo {
    private static final String OS_NAME = "os.name";

    //Platform identifiers: Windows, Linux, Mac OS, ...
    
    /**
     * Returns whether the underlying operating system is a Windows or Windows
     * NT platform.
     *
     * @see isWindowsPlatform
     * @see isWinNTPlatform
     */
    public static boolean isWin32Platform() {
        return (isWindowsPlatform() || isWinNTPlatform());
    }

    /**
     * Returns whether the underlying operating system is a Windows NT platform.
     *
     * @see isWinNT
     * @see isWin2K
     * @see isWin2K3
     * @see isWinXP
     */
    public static boolean isWinNTPlatform() {
        return (isWinNT() || isWin2K() || isWin2K3() || isWinXP());
    }

    /**
     * Returns whether the underlying operating system is a Windows platform.
     *
     * @see isWin95
     * @see isWin98
     * @see isWinME
     */
    public static boolean isWindowsPlatform() {
        return (isWin95() || isWin98() || isWinME());
    }

    //Single OS identifiers: Window 95, Window 98, ...
    
    /**
     * Returns whether the underlying operating system is Windows 95.
     */
    public static boolean isWin95() {
        return "Windows 95".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows 98.
     */
    public static boolean isWin98() {
        return "Windows 98".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows ME.
     */
    public static boolean isWinME() {
        return "Windows ME".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows NT.
     */
    public static boolean isWinNT() {
        return "Windows NT".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows 2000.
     */
    public static boolean isWin2K() {
        return "Windows 2000".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows 2003.
     */
    public static boolean isWin2K3() {
        return "Windows 2003".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Windows XP.
     */
    public static boolean isWinXP() {
        return "Windows XP".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Linux.
     */
    public static boolean isLinux() {
        return "Linux".equalsIgnoreCase(System.getProperty(OS_NAME));
    }

    /**
     * Returns whether the underlying operating system is Solaris.
     */
    public static boolean isSolaris() {
        return "Solaris".equalsIgnoreCase(System.getProperty(OS_NAME));
    }
}
