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

import org.columba.addressbook.shutdown.SaveAllAddressbooksPlugin;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.main.DefaultMain;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.services.ServiceManager;
import org.columba.core.shutdown.ShutdownManager;


/**
 * Main entrypoint for addressbook component
 * 
 * @author fdietz
 */
public class AddressbookMain extends DefaultMain {
    
	private static AddressbookMain instance = new AddressbookMain(); 
	
	public AddressbookMain() {	
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
        
        ServiceManager.getInstance().register("IContactFacade", "org.columba.addressbook.facade.ContactFacade");
        ServiceManager.getInstance().register("IFolderFacade", "org.columba.addressbook.facade.FolderFacade");
        ServiceManager.getInstance().register("IConfigFacade", "org.columba.addressbook.facade.ConfigFacade");
		
	}
	
	public static AddressbookMain getInstance() {
		return instance;
	}
	
	/** (non-Javadoc)
     * @see org.columba.core.main.DefaultMain#handleCommandLineParameters(java.lang.String[])
     */
    public void handleCommandLineParameters(String[] args) {
    }
}
