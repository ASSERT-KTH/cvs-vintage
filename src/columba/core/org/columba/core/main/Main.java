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

import java.io.PrintWriter;
import java.net.Socket;

import org.columba.addressbook.main.AddressbookMain;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.command.DefaultProcessor;
import org.columba.core.config.Config;
import org.columba.core.gui.ClipboardManager;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.gui.menu.MenuPluginHandler;
import org.columba.core.gui.themes.ThemeSwitcher;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.core.plugin.FramePluginHandler;
import org.columba.core.plugin.InterpreterHandler;
import org.columba.core.plugin.PluginManager;
import org.columba.core.plugin.ThemePluginHandler;
import org.columba.core.shutdown.SaveConfigPlugin;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.config.accountwizard.AccountWizardLauncher;
import org.columba.mail.main.MailMain;

public class Main {
  private static ColumbaLoader columbaLoader;

  public static void loadInVMInstance(String[] arguments) {
    try {
      Socket clientSocket = new Socket("127.0.0.1", ColumbaLoader.COLUMBA_PORT);

      PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());

      StringBuffer buf = new StringBuffer();
      buf.append("columba:");
      for (int i = 0; i < arguments.length; i++) {
        buf.append(arguments[i]);
        buf.append("%");
      }

	  if ( MainInterface.DEBUG )
	  	ColumbaLogger.log.debug("trying to pass arguments to a running Columba session:\n"+buf.toString());
	  
      writer.write(buf.toString());
      writer.flush();
      writer.close();

      clientSocket.close();

      System.exit(5);

    } catch (Exception ex) { // we get a java.net.ConnectException: Connection refused
      //  -> this means that no server is running
      //      -> lets start one
      columbaLoader = new ColumbaLoader();
    }

  }

  public static void main(String[] args) {
    ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
    cmdLineParser.initCmdLine(args);

    MainInterface.DEBUG = cmdLineParser.isDebugOption();
    // the configPath settings are made in the commandlineParser @see ColumbaCmdLineParser  

    loadInVMInstance(args);

    final StartUpFrame frame = new StartUpFrame();
    frame.setVisible(true);

    // enable logging 
    new ColumbaLogger();

    new Config();

    AddressbookMain addressbook = new AddressbookMain();
    addressbook.initConfiguration();

    MailMain mail = new MailMain();
    mail.initConfiguration();

    Config.init();


    MainInterface.clipboardManager = new ClipboardManager();

    MainInterface.processor = new DefaultProcessor();
    MainInterface.processor.start();

    MainInterface.pluginManager = new PluginManager();
    MainInterface.pluginManager.registerHandler(new InterpreterHandler());

    MainInterface.pluginManager.registerHandler(new ActionPluginHandler());

    MainInterface.pluginManager.registerHandler(new MenuPluginHandler("org.columba.core.menu"));

    MainInterface.pluginManager.registerHandler(new FramePluginHandler());
    
    MainInterface.pluginManager.registerHandler(new ThemePluginHandler());
	
    MainInterface.shutdownManager = new ShutdownManager();

    MainInterface.shutdownManager.register(new SaveConfigPlugin());
    
    MainInterface.backgroundTaskManager = new BackgroundTaskManager();
	MainInterface.backgroundTaskManager.register( new SaveConfigPlugin() );

    addressbook.initPlugins();
    mail.initPlugins();

    MainInterface.pluginManager.initPlugins();
    
	ThemeSwitcher.setTheme();

    frame.advance();

    //MainInterface.frameModelManager = new FrameModelManager();

    addressbook.initGui();

    frame.advance();

    mail.initGui();

    new FrameModel();

    frame.setVisible(false);

    if (MailConfig.getAccountList().count() == 0) {
      try {
        new AccountWizardLauncher().launchWizard();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    }
    
    new CmdLineArgumentHandler(args);

  } // main
}
