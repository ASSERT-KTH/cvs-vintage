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
import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.config.DefaultItem;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.main.DefaultMain;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionPluginHandler;
import org.columba.core.services.ServiceManager;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.gui.config.accountwizard.AccountWizardLauncher;
import org.columba.mail.nativ.defaultmailclient.SystemDefaultMailClientHandler;
import org.columba.mail.pgp.MultipartEncryptedRenderer;
import org.columba.mail.pgp.MultipartSignedRenderer;
import org.columba.mail.shutdown.SaveAllFoldersPlugin;
import org.columba.mail.shutdown.SavePOP3CachePlugin;
import org.columba.mail.spam.SaveSpamDBPlugin;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.composer.MimeTreeRenderer;
/**
 * Main entrypoint for mail component.
 * 
 * @author fdietz
 */
public class MailMain extends DefaultMain {
	
    private static MailMain instance = new MailMain();
	
	public MailMain() {
		 // Init PGP
        MimeTreeRenderer renderer = MimeTreeRenderer.getInstance();
        renderer.addMimePartRenderer(new MultipartSignedRenderer());
        renderer.addMimePartRenderer(new MultipartEncryptedRenderer());

        // Init Plugins
        PluginManager.getInstance().addHandlers("org/columba/mail/plugin/pluginhandler.xml");
        
        try {
            ((ActionPluginHandler) PluginManager.getInstance().getHandler(
                "org.columba.core.action")).addActionList(
                "org/columba/mail/action/action.xml");
        } catch (PluginHandlerNotFoundException ex) {
            ex.printStackTrace();
        }
        
        TaskInterface plugin = new SaveAllFoldersPlugin();
        BackgroundTaskManager.getInstance().register(plugin);

        plugin = new SavePOP3CachePlugin();
        BackgroundTaskManager.getInstance().register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);

        plugin = new SaveSpamDBPlugin();
        BackgroundTaskManager.getInstance().register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);

        // initialize cached headers which can be configured by the user
        // -> see documentation in class
        CachedHeaderfields.addConfiguration();
        
        ServiceManager.getInstance().register("org.columba.mail.facade.IConfigFactory", "org.columba.mail.facade.ConfigFactory");
        ServiceManager.getInstance().register("org.columba.mail.facade.IComposerFactory", "org.columba.mail.facade.ComposerFactory");
        ServiceManager.getInstance().register("org.columba.mail.facade.IDialogFactory", "org.columba.mail.facade.DialogFactory");
        ServiceManager.getInstance().register("org.columba.mail.facade.IFolderFactory", "org.columba.mail.facade.FolderFactory");
        ServiceManager.getInstance().register("org.columba.mail.facade.ISelectionFactory", "org.columba.mail.facade.SelectionFactory");
	}
	
	public static MailMain getInstance() {
		return instance;
	}
	
	/**
	 * @see org.columba.core.main.DefaultMain#handleCommandLineParameters(java.lang.String[])
	 */
	public void handleCommandLineParameters(String[] args) {
		if (MailConfig.getInstance().getAccountList().count() == 0) {
			new AccountWizardLauncher().launchWizard(true);
		}
		ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
		try {
			cmdLineParser.parseCmdLine(args);
		} catch (IllegalArgumentException e) {
		}
		// Check default mail client
		checkDefaultClient();
	}
	private void checkDefaultClient() {
		// Check if Columba is the default mail client
		SystemDefaultMailClientHandler defaultClientHandler = new SystemDefaultMailClientHandler();
		DefaultItem item = new DefaultItem(MailConfig.getInstance().get("options"));

		boolean checkDefault = item.getBoolean("options/defaultclient", "check", true);
		
		if (checkDefault
				&& defaultClientHandler.platfromSupportsDefaultMailClient()) {
			if (!defaultClientHandler.isDefaultMailClient()) {
			
			JPanel panel = new JPanel(new BorderLayout(0,10));
			
			panel.add(new MultiLineLabel(MailResourceLoader.getString("dialog", "defaultclient",
					"make_default")), BorderLayout.NORTH);
			
			JCheckBox askAgain = new JCheckBox(MailResourceLoader.getString("dialog", "defaultclient",
								"ask_no_more"));
			panel.add(askAgain, BorderLayout.CENTER );
			
				// Some error in the client/server communication
				//  --> fall back to default login process
				int result = JOptionPane.showConfirmDialog(
						FrameModel.getInstance().getActiveFrame(),
						panel,
						MailResourceLoader.getString("dialog", "defaultclient",
								"title"),
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					defaultClientHandler.setDefaultMailClient();
				}
				
				item.set("options/defaultclient", "check", !askAgain.isSelected());
			}
		}
	}
}