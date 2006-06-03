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
package org.columba.core.main;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.columba.api.plugin.IPluginManager;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.base.OSInfo;
import org.columba.core.component.ComponentManager;
import org.columba.core.config.Config;
import org.columba.core.config.SaveConfig;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.base.DebugRepaintManager;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.profiles.Profile;
import org.columba.core.gui.profiles.ProfileManager;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.trayicon.ColumbaTrayIcon;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.logging.Logging;
import org.columba.core.plugin.PluginManager;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.scripting.service.ServiceManager;
import org.columba.core.services.ServiceRegistry;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.StackProfiler;
import org.columba.core.versioninfo.VersionInfo;
import org.frapuccino.swing.ActiveWindowTracker;

import sun.misc.URLClassPath;

public class Bootstrap {

	private static final Logger LOG = Logger.getLogger("org.columba.core.main"); //$NON-NLS-1$

	private static final String RESOURCE_PATH = "org.columba.core.i18n.global"; //$NON-NLS-1$

	private String path;

	private boolean showSplashScreen = true;

	public void run(String args[]) throws Exception {

		addNativeJarsToClasspath();
		setLibraryPath();

		Logging.createDefaultHandler();
		registerCommandLineArguments();

		// handle commandline parameters
		if (handleCoreCommandLineParameters(args)) {
			System.exit(0);
		}

		StackProfiler profiler = new StackProfiler();
		profiler.push("main");
		profiler.push("config");
		profiler.push("profile");
		// prompt user for profile
		Profile profile = ProfileManager.getInstance().getProfile(path);
		profiler.pop("profile");

		// initialize configuration with selected profile
		new Config(profile.getLocation());
		profiler.pop("config");

		// if user doesn't overwrite logger settings with commandline arguments
		// just initialize default logging
		// Logging.createDefaultHandler();
		Logging.createDefaultFileHandler(Config.getInstance()
				.getConfigDirectory());

		for (int i = 0; i < args.length; i++) {
			LOG.info("arg[" + i + "]=" + args[i]);
		}

		SessionController.passToRunningSessionAndExit(args);

		// enable debugging of repaint manager to track down swing gui
		// access from outside the awt-event dispatcher thread

		if (Logging.DEBUG)
			RepaintManager.setCurrentManager(new DebugRepaintManager());

		// use heavy-weight popups to ensure they are always on top
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		// keep track of active windows (used by dialogs which don't have a
		// direct parent)
		ActiveWindowTracker.class.getClass();

		// show splash screen
		StartUpFrame frame = null;
		if (showSplashScreen) {
			frame = new StartUpFrame();
			frame.setVisible(true);
		}

		// register protocol handler
		System.setProperty("java.protocol.handler.pkgs",
				"org.columba.core.url|"
						+ System.getProperty("java.protocol.handler.pkgs", ""));

		profiler.push("i18n");
		// load user-customized language pack
		GlobalResourceLoader.loadLanguage();
		profiler.pop("i18n");

		SaveConfig task = new SaveConfig();
		BackgroundTaskManager.getInstance().register(task);
		ShutdownManager.getInstance().register(task);

		profiler.push("plugins core");
		initPlugins();
		profiler.pop("plugins core");

		ServiceRegistry.getInstance().register(IPluginManager.class,
				PluginManager.getInstance());

		profiler.push("components");
		// init all components
		ComponentManager.getInstance().init();
		ComponentManager.getInstance().registerCommandLineArguments();
		profiler.pop("components");

		// set Look & Feel
		ThemeSwitcher.setTheme();

		// initialize platform-dependend services
		initPlatformServices();

		// init font configuration
		new FontProperties();

		// set application wide font
		FontProperties.setFont();

		// handle the commandline arguments of the modules
		ComponentManager.getInstance().handleCommandLineParameters(
				ColumbaCmdLineParser.getInstance().getParsedCommandLine());

		profiler.push("plugins external");
		// now load all available plugins
		// PluginManager.getInstance().initExternalPlugins();
		profiler.pop("plugins external");

		profiler.push("frames");

		// restore frames of last session
		if (ColumbaCmdLineParser.getInstance().getRestoreLastSession()) {
			FrameManager.getInstance().openStoredViews();
		}

		/* initialize services before dismissing the splash screen */
		ServiceManager.getInstance().initServices();

		ShutdownManager.getInstance().register(new Runnable() {
			public void run() {

				ServiceManager.getInstance().stopServices();
				ServiceManager.getInstance().disposeServices();

			}

		});

		profiler.pop("frames");

		// Add the tray icon to the System tray
		// ColumbaTrayIcon.getInstance().addToSystemTray(
		// FrameManager.getInstance().getActiveFrameMediator()
		// .getFrameMediator());

		// hide splash screen
		if (frame != null) {
			frame.setVisible(false);
		}

		// call the postStartups of the modules
		// e.g. check for default mailclient
		ComponentManager.getInstance().postStartup();

		/* everything is up and running, start services */
		ServiceManager.getInstance().startServices();

		profiler.pop("main");

	}

	/**
	 * initialize all extension handlers from core, mail and contacts.
	 * Additionally, load all internally shipped plugins and last but not least,
	 * load all external plugins residing in /plugin directory.
	 */
	private void initPlugins() {

		//
		// first load all extension handlers
		//

		// load core extension handlers
		PluginManager.getInstance().addExtensionHandlers(
				"org/columba/core/plugin/extensionhandler.xml");

		// load addressbook extension handler
		PluginManager.getInstance().addExtensionHandlers(
				"org/columba/addressbook/plugin/extensionhandler.xml");

		// load mail extension handler
		PluginManager.getInstance().addExtensionHandlers(
				"org/columba/mail/plugin/extensionhandler.xml");

		// load all internal core plugins
		String path = "org/columba/core/plugin/plugin.xml";
		PluginManager.getInstance().addPlugin(path);

		//
		// following internal components plugin registration
		//

		// load all internal addressbook plugins
		path = "org/columba/addressbook/plugin/plugin.xml";
		PluginManager.getInstance().addPlugin(path);

		// load all internal mail plugins
		path = "org/columba/mail/plugin/plugin.xml";
		PluginManager.getInstance().addPlugin(path);

		//
		// now load all external plugins residing in /plugins directory
		//
		PluginManager.getInstance().initExternalPlugins();
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

		parser.addOption(OptionBuilder.create("profile"));

		parser.addOption(new Option("debug", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_debug")));

		parser.addOption(new Option("nosplash", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_nosplash")));

		// ComponentPluginHandler handler = null;
		// try {
		// handler = (ComponentPluginHandler) PluginManager.getInstance()
		// .getHandler("org.columba.core.component");
		// handler.registerCommandLineArguments();
		// } catch (PluginHandlerNotFoundException e) {
		// e.printStackTrace();
		// }

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
			LOG.info(MessageFormat.format(GlobalResourceLoader.getString(
					RESOURCE_PATH, "global", "info_version"), //$NON-NLS-2$
					new Object[] { VersionInfo.getVersion(),
							VersionInfo.getBuildDate() }));

			return true;
		}

		if (commandLine.hasOption("profile")) {
			path = commandLine.getOptionValue("profile");
		}

		if (commandLine.hasOption("debug")) {
			Logging.DEBUG = true;
			Logging.setDebugging(true);
		}

		if (commandLine.hasOption("nosplash")) {
			showSplashScreen = false;
		}

		// Do not exit
		return false;
	}

	/**
	 * This hacks the classloader to adjust the library path for convinient
	 * native support.
	 * 
	 * @author tstich
	 * 
	 * @throws Exception
	 */
	private void setLibraryPath() throws Exception {
		String libDir;
		if (OSInfo.isAMD64Bit()) {
			libDir = "amd64";
		} else {
			libDir = "lib";
		}
		// Platform maintainers: add your platform here

		String propertyPath = System.getProperty("java.library.path");
		if (OSInfo.isLinux())
			propertyPath += ":native/linux/";
		else if (OSInfo.isWin32Platform())
			propertyPath += ";native\\win32\\";
		// Platform maintainers: add your platform here

		propertyPath += libDir;

		System.setProperty("java.library.path", propertyPath);

		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		if (fieldSysPath != null) {
			fieldSysPath.set(System.class.getClassLoader(), null);
		}
	}

	/**
	 * This hacks the classloader to adjust the classpath for convinient native
	 * support.
	 * <p>
	 * I've cleaned this up using our new global class loader. This way we only
	 * add a few new <code>URLs</code> to our class loader instead of
	 * modifying the system class loader using reflection.
	 * 
	 * @author tstich,fdietz
	 * 
	 * @throws Exception
	 */
	private void addNativeJarsToClasspath() throws Exception {
		File nativeDir;

		String libDir;
		if (OSInfo.isAMD64Bit()) {
			libDir = "amd64";
		} else {
			libDir = "lib";
		}

		// Setup the path
		// Platform maintainers: add your platform here
		// see also initPlatformServices() method
		if (OSInfo.isLinux()) {
			nativeDir = new File("native/linux/" + libDir);
		} else if (OSInfo.isWin32Platform()) {
			nativeDir = new File("native/win32/" + libDir);
		} else {
			LOG.info("Native support for Platform not available.");
			return;
		}

		// Find all native jars
		File[] nativeJars = nativeDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("jar");
			}
		});
		if (nativeJars == null)
			return;

		// @author: fdietz
		//
		// The following line is not working - just don't know why 
		// Main.mainClassLoader.addURLs((URL[]) urlList.toArray(new URL[0]));
		//
		// WORKAROUND:
		//
		// Modify the system class loader instead - horribly! But it works!
		
		// Get the current classpath from the sysloader
		// through reflection
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();

		Field ucp = URLClassLoader.class.getDeclaredField("ucp");
		ucp.setAccessible(true);
		URLClassPath currentCP = (URLClassPath) ucp.get(sysloader);
		URL[] currentURLs = currentCP.getURLs();

		// add all native jars
		List<URL> urlList = new ArrayList<URL>();
		for (int i = 0; i < nativeJars.length; i++) {
			urlList.add(nativeJars[i].toURL());
		}

		// add the old classpath
		for (int i = 0; i < currentURLs.length; i++) {
			urlList.add(currentURLs[i]);
		}

		// replace with the modified classpath
		ucp.set(sysloader,
				new URLClassPath((URL[]) urlList.toArray(new URL[0])));

	}

	/**
	 * 
	 */
	private void initPlatformServices() {

		// Initilise system dependant stuff
		ColumbaDesktop.getInstance().initActiveDesktop();
		ColumbaTrayIcon.getInstance().initActiveIcon();
	}

}
