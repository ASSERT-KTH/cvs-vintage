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

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.shutdown.SaveAllAddressbooksPlugin;
import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.main.DefaultMain;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.shutdown.ShutdownManager;


/**
 * Main entrypoint for addressbook component
 * 
 * @author fdietz
 */
public class AddressbookMain extends DefaultMain {
    
	private static AddressbookMain instance = new AddressbookMain(); 
	
	public AddressbookMain() {
		AddressbookInterface.config = new AddressbookConfig(MainInterface.config);
		
		 // init addressbook plugin handlers
        MainInterface.pluginManager.addHandlers("org/columba/addressbook/plugin/pluginhandler.xml");
       
        try {
            ((ActionPluginHandler) MainInterface.pluginManager.getHandler(
                "org.columba.core.action")).addActionList(
                "org/columba/addressbook/action/action.xml");
        } catch (PluginHandlerNotFoundException ex) {
        }

        TaskInterface plugin = new SaveAllAddressbooksPlugin();
        MainInterface.backgroundTaskManager.register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);
		
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
