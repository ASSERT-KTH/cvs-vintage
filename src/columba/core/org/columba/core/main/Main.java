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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.columba.addressbook.main.AddressbookMain;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.command.DefaultCommandProcessor;
import org.columba.core.config.Config;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.focus.FocusManager;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.nativ.NativeWrapperHandler;
import org.columba.core.plugin.PluginManager;
import org.columba.core.profiles.Profile;
import org.columba.core.profiles.ProfileManager;
import org.columba.core.session.ColumbaServer;
import org.columba.core.session.SessionController;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.main.MailMain;

/**
 * Columba's main class used to start the application.
 */
public class Main {
	private static boolean showStartUpFrame = true;
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
		MainInterface.config = new Config(profile.getLocation());

		// if user doesn't overwrite logger settings with commandline arguments
		// just initialize default logging
		ColumbaLogger.createDefaultHandler();
		ColumbaLogger.createDefaultFileHandler();

		SessionController.passToRunningSessionAndExit(args);

		StartUpFrame frame = null;
		if (showStartUpFrame) {
			frame = new StartUpFrame();
			frame.setVisible(true);
		}

		MainInterface.connectionState = new ConnectionStateImpl();

		System.setProperty("java.protocol.handler.pkgs", System.getProperty(
				"java.protocol.handler.pkgs", "")
				+ "|org.columba.core.url");

		AddressbookMain addressbook = new AddressbookMain();
		addressbook.initConfiguration();

		MailMain mail = new MailMain();
		mail.initConfiguration();

		MainInterface.config.init();

		// load user-customized language pack
		GlobalResourceLoader.loadLanguage();

		MainInterface.clipboardManager = new ClipboardManager();
		MainInterface.focusManager = new FocusManager();

		MainInterface.processor = new DefaultCommandProcessor();

		MainInterface.pluginManager = new PluginManager();

		// load core plugin handlers
		MainInterface.pluginManager
				.addHandlers("org/columba/core/plugin/pluginhandler.xml");

		MainInterface.backgroundTaskManager = new BackgroundTaskManager();

		addressbook.initPlugins();
		mail.initPlugins();

		MainInterface.pluginManager.initPlugins();

		ThemeSwitcher.setTheme();

		// init font configuration
		new FontProperties();

		// set application wide font
		FontProperties.setFont();

		//MainInterface.frameModelManager = new FrameModelManager();
		addressbook.initGui();

		mail.initGui();

		MainInterface.frameModel = new FrameModel();

		ColumbaServer.getColumbaServer().handleCommandLineParameters(args);

		if (frame != null) {
			frame.setVisible(false);
		}

		if (MainInterface.frameModel.getOpenFrames().length == 0) {
			MainInterface.frameModel.openStoredViews();
		}

		// initialize native code wrapper
		MainInterface.nativeWrapper = new NativeWrapperHandler(
				MainInterface.frameModel.getOpenFrames()[0]);
	}

	public static void setShowStartUpFrame(boolean show) {
		showStartUpFrame = show;
	}

	/**
	 * Default implementation for ConnectionState.
	 */
	protected static class ConnectionStateImpl implements ConnectionState {
		protected boolean online = false;
		protected EventListenerList listenerList = new EventListenerList();
		protected ChangeEvent e;

		protected ConnectionStateImpl() {
			e = new ChangeEvent(this);
		}

		public void addChangeListener(ChangeListener l) {
			listenerList.add(ChangeListener.class, l);
		}

		public synchronized boolean isOnline() {
			return online;
		}

		public void removeChangeListener(ChangeListener l) {
			listenerList.remove(ChangeListener.class, l);
		}

		public synchronized void setOnline(boolean b) {
			if (online != b) {
				online = b;
				Object[] listeners = listenerList.getListenerList();
				// Process the listeners last to first, notifying
				// those that are interested in this event
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ChangeListener.class) {
						((ChangeListener) listeners[i + 1]).stateChanged(e);
					}
				}
			}
		}
	}
}