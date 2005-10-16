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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.RepaintManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.columba.api.backgroundtask.IBackgroundTaskManager;
import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.api.plugin.IPluginManager;
import org.columba.api.shutdown.IShutdownManager;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.base.OSInfo;
import org.columba.core.component.ComponentManager;
import org.columba.core.config.Config;
import org.columba.core.config.IConfig;
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
import org.columba.core.pluginhandler.ServiceExtensionHandler;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.services.ServiceRegistry;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.StackProfiler;
import org.columba.core.versioninfo.VersionInfo;

import sun.misc.URLClassPath;

/**
 * Columba's main class used to start the application.
 */
public class Main {
	private static final Logger LOG = Logger.getLogger("org.columba.core.main"); //$NON-NLS-1$

	private static final String RESOURCE_PATH = "org.columba.core.i18n.global"; //$NON-NLS-1$

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
		addNativeJarsToClasspath();
		setLibraryPath();

		Main.getInstance().run(args);
	}

	/**
	 * This hacks the classloader to adjust the library path for convinient
	 * native support.
	 * 
	 * @author tstich
	 * 
	 * @throws Exception
	 */
	private static void setLibraryPath() throws Exception {
		if (OSInfo.isLinux()) {
			System.setProperty("java.library.path", System
					.getProperty("java.library.path")
					+ ":native/linux/lib");
		} else if (OSInfo.isWin32Platform()) {
			System.setProperty("java.library.path", System
					.getProperty("java.library.path")
					+ ";native\\win32\\lib");
		}
		// Platform maintainers: add your platform here

		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		if (fieldSysPath != null) {
			fieldSysPath.set(System.class.getClassLoader(), null);
		}
	}

	/**
	 * This hacks the classloader to adjust the classpath for convinient native
	 * support.
	 * 
	 * @author tstich
	 * 
	 * @throws Exception
	 */
	private static void addNativeJarsToClasspath() throws Exception {
		File nativeDir;

		// Setup the path
		// Platform maintainers: add your platform here
		// see also initPlatformServices() method
		if (OSInfo.isLinux()) {
			nativeDir = new File("native/linux/lib");
		} else if (OSInfo.isWin32Platform()) {
			nativeDir = new File("native/win32/lib");
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

		// Get the current classpath from the sysloader
		// through reflection
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		Field ucp = URLClassLoader.class.getDeclaredField("ucp");
		ucp.setAccessible(true);
		URLClassPath currentCP = (URLClassPath) ucp.get(sysloader);
		URL[] currentURLs = currentCP.getURLs();

		// add all native jars
		List urlList = new ArrayList();
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
	private static void initPlatformServices() {

		// Initilise system dependant stuff
		ColumbaDesktop.getInstance().initActiveDesktop();
		ColumbaTrayIcon.getInstance().initActiveIcon();
	}

	public void run(String args[]) {

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

		// register shutdown manager in service registry
		ServiceRegistry.getInstance().register(IShutdownManager.class,
				ShutdownManager.getInstance());

		// register background task manager in service registry
		ServiceRegistry.getInstance().register(IBackgroundTaskManager.class,
				BackgroundTaskManager.getInstance());

		// initialize configuration with selected profile
		new Config(profile.getLocation());
		profiler.pop("config");

		// register Config in service registry
		ServiceRegistry.getInstance().register(IConfig.class,
				Config.getInstance());

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
		PluginManager.getInstance().initCorePlugins();
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
		PluginManager.getInstance().initExternalPlugins();
		profiler.pop("plugins external");

		// hide splash screen
		if (frame != null) {
			frame.setVisible(false);
		}

		profiler.push("frames");

		// restore frames of last session
		if (restoreLastSession) {
			FrameManager.getInstance().openStoredViews();
		}

    /* initialize services before dismissing the splash screen */
    ServiceExtensionHandler serviceHandler = null;

    try
    {
      serviceHandler = (ServiceExtensionHandler)PluginManager.getInstance().getHandler("org.columba.core.service");
      serviceHandler.initServices();
      ShutdownManager.getInstance().register(new Runnable()
        {
          public void run()
          {
            ServiceExtensionHandler serviceHandler = null;
            try
            {
              serviceHandler = (ServiceExtensionHandler)PluginManager.getInstance().getHandler("org.columba.core.service");
              serviceHandler.stopServices();
              serviceHandler.disposeServices();
            }
            catch(PluginHandlerNotFoundException ex)
            {
              LOG.log(Level.SEVERE,"",ex);
            }
          }
          
        });
    }
    catch(PluginHandlerNotFoundException ex)
    {
      LOG.log(Level.SEVERE,"",ex);
    }
        
		profiler.pop("frames");

		// for ( int i=0; i<frameMediator.length; i++) {
		// frameMediator[i].getContainer().getFrame().setVisible(true);
		// }

		// Add the tray icon to the System tray
		// ColumbaTrayIcon.getInstance().addToSystemTray(
		// FrameManager.getInstance().getActiveFrameMediator()
		// .getFrameMediator());

		// call the postStartups of the modules
		// e.g. check for default mailclient
		ComponentManager.getInstance().postStartup();

		profiler.pop("main");
    
    /* everything is up and running, start services */
    if (serviceHandler != null)
      serviceHandler.startServices();
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
	 * @param restoreLastSession
	 *            The restoreLastSession to set.
	 */
	public void setRestoreLastSession(boolean restoreLastSession) {
		this.restoreLastSession = restoreLastSession;
	}
}