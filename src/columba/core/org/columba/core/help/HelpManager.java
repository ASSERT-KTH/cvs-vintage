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
package org.columba.core.help;

import java.awt.Component;
import java.awt.Font;
import java.net.URL;
import java.util.Locale;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * @author fdietz
 *
 * This class manages all JavaHelp relevant helpsets, its also
 * encapsulates the broker which is used for context sensitiv help
 * 
 */
public class HelpManager {

	// name of helpset resource
	final static String helpsetName = "jhelpset";

	private static JHelp jh = null;
	private static HelpSet hs = null;
	private static HelpBroker hb = null;

	private static String hsName = null; // name for the HelpSet 
	private static String hsPath = null; // URL spec to the HelpSet

	static String title = "";

	private static JFrame frame;

	public HelpManager() {
		initialize(helpsetName, HelpManager.class.getClassLoader());
	}

	public static void openHelpFrame() {
		if (frame == null) {

			new HelpFrame(jh);
			frame = HelpFrame.createFrame(hs.getTitle(), null);
			frame.setVisible(true);
		} else {
			frame.setVisible(true);
		}
	}

	protected static void initialize(String name, ClassLoader loader) {
		URL url = HelpSet.findHelpSet(loader, name, "", Locale.getDefault());
		if (url == null) {
			url = HelpSet.findHelpSet(loader, name, ".hs", Locale.getDefault());
			if (url == null) {
				// could not find it!
				JOptionPane.showMessageDialog(
					null,
					"HelpSet not found",
					"Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		initialize(url, loader);
	}

	protected static void initialize(URL url, ClassLoader loader) {
		try {
			hs = new HelpSet(loader, url);
		} catch (Exception ee) {
			JOptionPane.showMessageDialog(
				null,
				"HelpSet not found",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		// The JavaHelp can't be added to a BorderLayout because it
		// isnt' a component. For this demo we'll use the embeded method
		// since we don't want a Frame to be created.

		hb = hs.createHelpBroker();

		// TODO: fix the font settings for the content viewer
		// setting the fonts like this doesn't seem to work
		Font font = (Font) UIManager.get("Label.font");
		hb.setFont(font);

		jh = new JHelp(hs);

		// set main font
		jh.setFont(font);

		jh.getContentViewer().setFont(font);
		jh.getCurrentNavigator().setFont(font);

	}

	/**
	 * @return
	 */
	public static HelpBroker getHelpBroker() {
		return hb;
	}
	
	/**
	 * 
	 * Associate button with topic ID.
	 * 
	 * topic ID's are listed in jhelpmap.jhm in package lib/usermanual.jar
	 * 
	 * 
	 * @param c			component 
	 * @param helpID	helpID 
	 */
	public static void enableHelpOnButton(Component c, String helpID)
	{
		getHelpBroker().enableHelpOnButton(c, helpID, hs);
	}

}
