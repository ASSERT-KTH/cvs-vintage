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