// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

/*
 * Author: Hrk (Luca Santarelli) <hrk@users.sourceforge.net> Comments: this
 * class provides some methods to enlarge or maximise a java.awt.Component
 * object.
 */
package org.columba.core.gui.frame;

//Resizing
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.logging.Logger;

public class WindowMaximizer {
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.frame");

	public static void maximize(Object obj) {
		//We can use the Java way to maximize the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Frame frame = (Frame) obj;
		frame.setSize(screenSize);

		if (Toolkit.getDefaultToolkit().isFrameStateSupported(
				Frame.MAXIMIZED_BOTH) == false)
			LOG.severe("System doesn't support maximize frame state.");

		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

	}

	public static boolean isWindowMaximized(Object obj) {
		//We can use the Java way to maximize the window
		Frame frame = (Frame) obj;
		int state = frame.getExtendedState();

		if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
			return true;
		}

		return false;
	}
};
