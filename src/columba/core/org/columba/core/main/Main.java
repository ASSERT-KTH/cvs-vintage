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
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.pluginhandler.ConfigPluginHandler;
import org.columba.core.pluginhandler.ExternalToolsPluginHandler;
import org.columba.core.pluginhandler.FramePluginHandler;
import org.columba.core.pluginhandler.InterpreterHandler;
import org.columba.core.pluginhandler.MenuPluginHandler;
import org.columba.core.pluginhandler.ThemePluginHandler;
import org.columba.core.pluginhandler.ViewPluginHandler;
import org.columba.core.session.ColumbaServer;
import org.columba.core.session.SessionController;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.main.MailMain;

public class Main {
    private static boolean showStartUpFrame = true;
    private Main() {}
    
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
        MainInterface.config = new Config(path == null ? null : new File(path));

        // if user doesn't overwrite logger settings with commandline arguments
        // just initialize default logging
        ColumbaLogger.createDefaultHandler();
        ColumbaLogger.createDefaultFileHandler();
        
        SessionController.passToRunningSessionAndExit(args);

        StartUpFrame frame = new StartUpFrame();
        if(showStartUpFrame) {
            frame.setVisible(true);
        }
        
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

        MainInterface.pluginManager.registerHandler(new InterpreterHandler());

        MainInterface.pluginManager.registerHandler(new ExternalToolsPluginHandler());

        MainInterface.pluginManager.registerHandler(new ActionPluginHandler());

        MainInterface.pluginManager.registerHandler(new MenuPluginHandler(
                "org.columba.core.menu"));
        MainInterface.pluginManager.registerHandler(new ConfigPluginHandler());

        MainInterface.pluginManager.registerHandler(new FramePluginHandler());

        MainInterface.pluginManager.registerHandler(new ThemePluginHandler());

        MainInterface.pluginManager.registerHandler(new ViewPluginHandler());

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

        frame.setVisible(false);

        if (MainInterface.frameModel.getOpenFrames().length == 0) {
            MainInterface.frameModel.openStoredViews();
        }
    }
    
    public static void setShowStartUpFrame(boolean show) {
        showStartUpFrame = show;
    }
}
