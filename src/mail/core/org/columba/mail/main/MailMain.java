/*
 * Created on 28.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.main;

import org.columba.core.action.ActionPluginHandler;
import org.columba.core.gui.menu.MenuPluginHandler;
import org.columba.core.main.DefaultMain;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.mail.coder.Base64Decoder;
import org.columba.mail.coder.Base64Encoder;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.QuotedPrintableDecoder;
import org.columba.mail.coder.QuotedPrintableEncoder;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.plugin.FilterActionPluginHandler;
import org.columba.mail.plugin.FilterPluginHandler;
import org.columba.mail.plugin.FolderPluginHandler;
import org.columba.mail.plugin.ImportPluginHandler;
import org.columba.mail.pop3.POP3ServerCollection;
import org.columba.mail.shutdown.SaveAllFoldersPlugin;
import org.columba.mail.shutdown.SavePOP3CachePlugin;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MailMain extends DefaultMain {

	
	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#handleCommandLineParameters(java.lang.String[])
	 */
	public void handleCommandLineParameters(String[] args) {
		

	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initConfiguration()
	 */
	public void initConfiguration() {
		new MailConfig();

		

		new CoderRouter();
		new QuotedPrintableDecoder();
		new QuotedPrintableEncoder();
		new Base64Decoder();
		new Base64Encoder();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initGui()
	 */
	public void initGui() {
		
		MainInterface.popServerCollection = new POP3ServerCollection();
		
		new MailResourceLoader();
		
		MainInterface.treeModel = new TreeModel(MailConfig.getFolderConfig());
		
		/*
		MainInterface.frameModel =
			new MailFrameModel(
				MailConfig.get("options").getElement("/options/gui/viewlist"));
		*/
		

		
	}

	/* (non-Javadoc)
	 * @see org.columba.core.main.DefaultMain#initPlugins()
	 */
	public void initPlugins() {

		MainInterface.pluginManager.registerHandler(
			new FilterActionPluginHandler());
		MainInterface.pluginManager.registerHandler(new FilterPluginHandler());
		MainInterface.pluginManager.registerHandler(new FolderPluginHandler());

		MainInterface.pluginManager.registerHandler(
			new MenuPluginHandler("org.columba.mail.menu"));
			
		MainInterface.pluginManager.registerHandler(
					new MenuPluginHandler("org.columba.mail.composer.menu"));

		MainInterface.pluginManager.registerHandler(new ImportPluginHandler());

		try {

			(
				(ActionPluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.action")).addActionList(
				"org/columba/mail/action/action.xml");

		} catch (PluginHandlerNotFoundException ex) {
			ex.printStackTrace();
		}

		MainInterface.shutdownManager.register(new SaveAllFoldersPlugin());

		MainInterface.shutdownManager.register(new SavePOP3CachePlugin());

	}

}
