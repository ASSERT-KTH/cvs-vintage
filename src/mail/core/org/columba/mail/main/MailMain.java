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
package org.columba.mail.main;

import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.main.DefaultMain;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.gui.config.accountwizard.AccountWizardLauncher;
import org.columba.mail.pgp.MultipartEncryptedRenderer;
import org.columba.mail.pgp.MultipartSignedRenderer;
import org.columba.mail.shutdown.SaveAllFoldersPlugin;
import org.columba.mail.shutdown.SavePOP3CachePlugin;
import org.columba.mail.spam.SaveSpamDBPlugin;
import org.columba.ristretto.composer.MimeTreeRenderer;


/**
 * Main entrypoint for mail component.
 * 
 * @author fdietz
 */
public class MailMain extends DefaultMain {
	
    private static MailMain instance = new MailMain();
	
	public MailMain() {
		MailInterface.config = new MailConfig(MainInterface.config);
		
		 // Init PGP
        MimeTreeRenderer renderer = MimeTreeRenderer.getInstance();
        renderer.addMimePartRenderer(new MultipartSignedRenderer());
        renderer.addMimePartRenderer(new MultipartEncryptedRenderer());

        // Init Plugins
        MainInterface.pluginManager.addHandlers("org/columba/mail/plugin/pluginhandler.xml");
        
        try {
            ((ActionPluginHandler) MainInterface.pluginManager.getHandler(
                "org.columba.core.action")).addActionList(
                "org/columba/mail/action/action.xml");
        } catch (PluginHandlerNotFoundException ex) {
            ex.printStackTrace();
        }
        
        TaskInterface plugin = new SaveAllFoldersPlugin();
        MainInterface.backgroundTaskManager.register(plugin);

        plugin = new SavePOP3CachePlugin();
        MainInterface.backgroundTaskManager.register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);

        plugin = new SaveSpamDBPlugin();
        MainInterface.backgroundTaskManager.register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);

        // initialize cached headers which can be configured by the user
        // -> see documentation in class
        CachedHeaderfields.addConfiguration();
        
	}
	
	public static MailMain getInstance() {
		return instance;
	}
	
	/**
     * @see org.columba.core.main.DefaultMain#handleCommandLineParameters(java.lang.String[])
     */
    public void handleCommandLineParameters(String[] args) {
    	
    	if (MailInterface.config.getAccountList().count() == 0) {
            new AccountWizardLauncher().launchWizard(true);
        }
    	
    	 ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
         try {
             cmdLineParser.parseCmdLine(args);
         } catch (IllegalArgumentException e) {
         }
    }

}
