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
package org.columba.addressbook.main;

import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.columba.addressbook.facade.AddressbookServiceProvider;
import org.columba.addressbook.shutdown.SaveAllAddressbooksPlugin;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.main.ColumbaCmdLineParser;
import org.columba.core.main.IModuleMain;
import org.columba.core.main.Main;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginLoadingFailedException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.services.ServiceManager;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.GlobalResourceLoader;


/**
 * Main entrypoint for addressbook component
 * 
 * @author fdietz
 */
public class AddressbookMain implements IModuleMain {
    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.addressbook.main");

    private static final String RESOURCE_PATH = "org.columba.addressbook.i18n.global";
    
    
	private static AddressbookMain instance = new AddressbookMain(); 
	
	private AddressbookMain() {	
	}
	
	public static AddressbookMain getInstance() {
		return instance;
	}
	
	/**
	 * @see org.columba.core.main.IModuleMain#handleCommandLineParameters()
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {
		if (commandLine.hasOption("addressbook")) {
			try {
				FrameModel.getInstance().openView( "Addressbook");

				Main.getInstance().setRestoreLastSession(false);
			} catch (PluginLoadingFailedException e) {
				LOG.severe(e.getLocalizedMessage());
			}			
		}
	}
	/**
	 * @see org.columba.core.main.IModuleMain#init()
	 */
	public void init() {
		 // init addressbook plugin handlers
		PluginManager.getInstance().addHandlers("org/columba/addressbook/plugin/pluginhandler.xml");
       
        try {
            ((ActionPluginHandler) PluginManager.getInstance().getHandler(
                "org.columba.core.action")).addActionList(
                "org/columba/addressbook/action/action.xml");
        } catch (PluginHandlerNotFoundException ex) {
        }

        TaskInterface plugin = new SaveAllAddressbooksPlugin();
        BackgroundTaskManager.getInstance().register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);
        
        ServiceManager.getInstance().register(AddressbookServiceProvider.CONTACT, "org.columba.addressbook.facade.ContactFacade");
        ServiceManager.getInstance().register(AddressbookServiceProvider.FOLDER, "org.columba.addressbook.facade.FolderFacade");
        ServiceManager.getInstance().register(AddressbookServiceProvider.CONFIG, "org.columba.addressbook.facade.ConfigFacade");		
	}
	/**
	 * @see org.columba.core.main.IModuleMain#postStartup()
	 */
	public void postStartup() {
	}
	/**
	 * @see org.columba.core.main.IModuleMain#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {
		ColumbaCmdLineParser parser = ColumbaCmdLineParser.getInstance();

		parser.addOption(new Option("addressbook", GlobalResourceLoader.getString(
				RESOURCE_PATH, "global", "cmdline_addressbook")));
		
	}
}
