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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.columba.core.config.Config;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ComponentPluginHandler;
import org.columba.core.profiles.Profile;
import org.columba.core.profiles.ProfileManager;
import org.columba.core.session.SessionController;
import org.columba.core.trayicon.ColumbaTrayIcon;
import org.columba.core.util.GlobalResourceLoader;

/**
 * Columba's main class used to start the application.
 */
public class Main {
	/** If true, enables debugging output from org.columba.core.logging */
	public static boolean DEBUG = false;

	private static final Logger LOG = Logger.getLogger("org.columba.core.main");

	private static final String RESOURCE_PATH = "org.columba.core.i18n.global";

	private static Main instance;

	private String path;

	private boolean showSplashScreen = true;

	private boolean restoreLastSession = true;

	private Main() {
	}

	public static Main getInstance() {
		if (instance == null) {
			instance = new Main();
		}

		return instance;
	}

	public static void main(String[] args) throws Exception {
		addCustomClasspath();
		setLibraryPath();
		
		Main.getInstance().run(args);
	}

	
	private static void setLibraryPath() throws Exception {		
		System.setProperty("java.library.path", System.getProperty("java.library.path") + ":native/" + System.getProperty("os.name").toLowerCase() + "/lib");
		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		if (fieldSysPath != null) {
		fieldSysPath.set(System.class.getClassLoader(), null);
		}		
	}
	
	private static void addCustomClasspath() throws Exception {
		String columbaPath = System.getProperty("columba.class.path");
		if( columbaPath == null || columbaPath.length() == 0) return;
		
		String[] paths = columbaPath.split(":|;");
		
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;
		
		for( int i=0; i<paths.length; i++) {
		try {
			Method method = sysclass.getDeclaredMethod("addURL",new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ new URL("file:" + paths[i]) });
		} catch (Throwable t) {
			throw new Exception(t);
		}
		}
	}
	
	public void run(String args[]) {
		ColumbaLogger.createDefaultHandler();
		registerCommandLineArguments();

		// handle commandline parameters
		if (handleCoreCommandLineParameters(args)) {
			System.exit(0);
		}

		// prompt user for profile
		Profile profile = ProfileManager.getInstance().getProfile(path);

		// initialize configuration with selected profile
		new Config(profile.getLocation());

		// if user doesn't overwrite logger settings with commandline arguments
		// just initialize default logging
		ColumbaLogger.createDefaultHandler();
		ColumbaLogger.createDefaultFileHandler();

		for ( int i=0; i<args.length; i++) {
			LOG.info("arg["+i+"]="+args[i]);
		}
		
		SessionController.passToRunningSessionAndExit(args);

		// enable debugging of repaint manager to track down swing gui
		// access from outside the awt-event dispatcher thread
		/*
		if (Main.DEBUG)
			RepaintManager.setCurrentManager(new DebugRepaintManager());
			*/
		
		// show splash screen
		StartUpFrame frame = null;
		if (showSplashScreen) {
			frame = new StartUpFrame();
			frame.setVisible(true);
		}
		
		// Add the tray icon to the System tray
		ColumbaTrayIcon.getInstance().addToSystemTray();

		// register protocol handler
		System.setProperty("java.protocol.handler.pkgs", "org.columba.core.url|"+System.getProperty(
				"java.protocol.handler.pkgs", "")
				);

		// load user-customized language pack
		GlobalResourceLoader.loadLanguage();

		ComponentPluginHandler handler = null;
		try {
			handler = (ComponentPluginHandler) PluginManager.getInstance()
					.getHandler("org.columba.core.component");
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
		}

		// init all components
		handler.init();

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

		// handle the commandline arguments of the modules
		handler.handleCommandLineParameters(ColumbaCmdLineParser.getInstance()
				.getParsedCommandLine());

		// restore frames of last session
		if (restoreLastSession) {
			FrameModel.getInstance().openStoredViews();
		}

		// call the postStartups of the modules
		// e.g. check for default mailclient
		handler.postStartup();
	}

	/**
	 *  
	 */
	private void registerCommandLineArguments() {
		ColumbaCmdLineParser parser = ColumbaCmdLineParser.getInstance();

		parser.addOption(new Option("version", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_version")));

		parser.addOption(new Option("help", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_help")));

		parser.addOption(OptionBuilder.withArgName("name_or_path").hasArg()
				.withDescription(
						GlobalResourceLoader.getString(RESOURCE_PATH, "global",
								"cmdline_profile")).create("profile"));

		parser.addOption(new Option("debug", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_debug")));

		parser.addOption(new Option("nosplash", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_nosplash")));

		ComponentPluginHandler handler = null;
		try {
			handler = (ComponentPluginHandler) PluginManager.getInstance()
					.getHandler("org.columba.core.component");
			handler.registerCommandLineArguments();
		} catch (PluginHandlerNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Uses the command line parser to validate the passed arguments and invokes
	 * handlers to process the detected options.
	 */
	private boolean handleCoreCommandLineParameters(String[] args) {
		ColumbaCmdLineParser parser = ColumbaCmdLineParser.getInstance();
		CommandLine commandLine;

		try {
			commandLine = parser.parse(args);
		} catch (ParseException e) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			parser.printUsage();

			return true;
		}

		if (commandLine.hasOption("help")) {
			parser.printUsage();

			return true;
		}

		if (commandLine.hasOption("version")) {
			System.out.println(MessageFormat.format(GlobalResourceLoader
					.getString(RESOURCE_PATH, "global", "info_version"),
					new Object[] { VersionInfo.getVersion(),
							VersionInfo.getBuildDate() }));

			return true;
		}

		if (commandLine.hasOption("profile")) {
			path = commandLine.getOptionValue("profile");
		}

		if (commandLine.hasOption("debug")) {
			DEBUG = true;
			ColumbaLogger.setDebugging(true);			
		}

		if (commandLine.hasOption("nosplash")) {
			showSplashScreen = false;
		}

		// Do not exit
		return false;
	}

	/**
	 * @param restoreLastSession
	 *            The restoreLastSession to set.
	 */
	public void setRestoreLastSession(boolean restoreLastSession) {
		this.restoreLastSession = restoreLastSession;
	}
}