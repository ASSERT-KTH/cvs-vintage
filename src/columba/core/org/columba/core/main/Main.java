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

package org.columba.core.main;

import javax.swing.RepaintManager;

import org.columba.addressbook.main.AddressbookMain;
import org.columba.core.config.Config;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.util.DebugRepaintManager;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.plugin.PluginManager;
import org.columba.core.profiles.Profile;
import org.columba.core.profiles.ProfileManager;
import org.columba.core.session.SessionController;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.main.MailMain;

/**
 * Columba's main class used to start the application.
 */
public class Main {
	private static boolean showStartUpFrame = true;
	/** If true, enables debugging output from org.columba.core.logging */
	public static boolean DEBUG = false;

	private Main() {
	}

	public static void main(String[] args) {
		ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
		try {
			cmdLineParser.parseCmdLine(args);
		} catch (IllegalArgumentException e) {
			ColumbaCmdLineParser.printUsage();
			System.exit(2);
		}

		// initialize configuration backend
		String path = cmdLineParser.getPathOption();

		// prompt user for profile
		Profile profile = ProfileManager.getInstance().getProfile(path);

		// initialize configuration with selected profile
		new Config(profile.getLocation());

		// if user doesn't overwrite logger settings with commandline arguments
		// just initialize default logging
		ColumbaLogger.createDefaultHandler();
		ColumbaLogger.createDefaultFileHandler();

		SessionController.passToRunningSessionAndExit(args);

		// enable debugging of repaint manager to track down swing gui
		// access from outside the awt-event dispatcher thread
		if (Main.DEBUG)
			RepaintManager.setCurrentManager(new DebugRepaintManager());

		// show splash screen
		StartUpFrame frame = null;
		if (showStartUpFrame) {
			frame = new StartUpFrame();
			frame.setVisible(true);
		}

		// register protocol handler
		System.setProperty("java.protocol.handler.pkgs", System.getProperty(
				"java.protocol.handler.pkgs", "")
				+ "|org.columba.core.url");

		// load user-customized language pack
		GlobalResourceLoader.loadLanguage();
	
		// init addressbook component
		AddressbookMain.getInstance();

		// init mail component
		MailMain.getInstance();

		// now load all available plugins
		PluginManager.getInstance().initPlugins();

		// set Look & Feel
		ThemeSwitcher.setTheme();

		// init font configuration
		new FontProperties();

		// set application wide font
		FontProperties.setFont();

		// hide splash screen
		if (frame != null) {
			frame.setVisible(false);
		}

		// restore frames of last session
		if (FrameModel.getInstance().getOpenFrames().length == 0) {
			FrameModel.getInstance().openStoredViews();
		}

		// handle commandline parameters
		handleCommandLineParameters(args);

	}

	public static void setShowStartUpFrame(boolean show) {
		showStartUpFrame = show;
	}

	/**
     * Uses the command line parser to validate the passed arguments
     * and invokes handlers to process the detected options.
     */
	public static void handleCommandLineParameters(String[] args) {

		// handle core framework arguments
		ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
		try {
			cmdLineParser.parseCmdLine(args);
		} catch (IllegalArgumentException e) {
		}

		AddressbookMain.getInstance().handleCommandLineParameters(args);
		MailMain.getInstance().handleCommandLineParameters(args);
	}
}