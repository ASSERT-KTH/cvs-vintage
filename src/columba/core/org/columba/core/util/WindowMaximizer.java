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

	Filename: WindowMaximizer.java
	Author: Hrk (Luca Santarelli) <hrk@users.sourceforge.net>
	Comments: this class provides some methods to enlarge or maximise a java.awt.Component object.
	It is needed to provide a true mazimisation on Win32 platform, which is not available in pure Java.
	As of today, JDK 1.4.1, blame SUN for not having done this thing themself, since they promised it for JDK 1.3.
*/

package org.columba.core.util;

//Resizing
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import org.columba.core.logging.ColumbaLogger;

public class WindowMaximizer {
	private static final String sLibrary = "WindowMaximizer";
	private static boolean bLibraryLoaded = false;

	public static void maximize(Object obj) {
		/*
			We do not know if we were given a valid object. This is not a problem for the
			native win32 method, since it will look for a window with this class name and will maximize
			it only if found.
			We need to know if the given object can be aximized if we use the Java way.
		*/
		String sClassName = obj.getClass().getName();
		if (OSInfo.isWin32Platform() && bLibraryLoaded) { //win32 has its own way to maximize windows, so we need a native method.
			try {
//System.out.println(bLibraryLoaded);
				WindowMaximizer wm = new WindowMaximizer();
//System.out.println(bLibraryLoaded);
				wm.maximizeWindow(sClassName);
//				new WindowMaximizer().maximizeWindow(sClassName);

			}
			catch (UnsatisfiedLinkError ex) {
				//We should get here only if the library hasn't been loaded, so we don't even need to notify the user: we already notified him.
			}
		}
		else { //We can use the Java way to maximize the window (hoping people has given us a window).
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Component c = (Component)obj;
			c.setSize(screenSize);
		}
		return;
	}

	//This is the native method which will be used only on win32 platforms (the other ones don't need it)
	public native void maximizeWindow(String sClassName);

	public static boolean isWindowMaximized(Object obj) {
		/*
			We do not know if we were given a valid object. This is not a problem for the
			native win32 method, since it will look for a window with this class name and will maximize
			it only if found.
			We need to know if the given object can be aximized if we use the Java way.
		*/
		String sClassName = obj.getClass().getName();
		if (OSInfo.isWin32Platform() && bLibraryLoaded) { //win32 has its own way to maximize windows, so we need a native method.
			try {
//System.out.println(bLibraryLoaded);
				WindowMaximizer wm = new WindowMaximizer();
//System.out.println(bLibraryLoaded);
				return wm.isWindowMaximized(sClassName);
			}
			catch (UnsatisfiedLinkError ex) {
				//We should get here only if the library hasn't been loaded, so we don't even need to notify the user: we already notified him.
			}
		}
		else { //We can use the Java way to maximize the window (hoping people has given us a window).
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Component c = (Component)obj;
			Dimension windowSize = c.getSize();
			return (windowSize.equals(screenSize));
		}
		//Stupid Java.
		return true;
	}

	public native boolean isWindowMaximized(String sClassName);

	//This method loads the library.
	static {
		try {
			System.loadLibrary(sLibrary);
			bLibraryLoaded = true;
System.out.println("Ho caricato");
		}
		catch (UnsatisfiedLinkError ex) {
			ColumbaLogger.log.error("Library: '" + sLibrary + "' could not be found.");
		}
	}
};