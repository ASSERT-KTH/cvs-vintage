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
import org.columba.core.plugin.ActionPluginHandler;
import org.columba.core.plugin.MenuPluginHandler;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.shutdown.ShutdownManager;

import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.pgp.MultipartEncryptedRenderer;
import org.columba.mail.pgp.MultipartSignedRenderer;
import org.columba.mail.plugin.FilterActionPluginHandler;
import org.columba.mail.plugin.FilterPluginHandler;
import org.columba.mail.plugin.FolderOptionsPluginHandler;
import org.columba.mail.plugin.FolderPluginHandler;
import org.columba.mail.plugin.ImportPluginHandler;
import org.columba.mail.plugin.POP3PreProcessingFilterPluginHandler;
import org.columba.mail.plugin.TableRendererPluginHandler;
import org.columba.mail.pop3.POP3ServerCollection;
import org.columba.mail.shutdown.SaveAllFoldersPlugin;
import org.columba.mail.shutdown.SavePOP3CachePlugin;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.composer.MimeTreeRenderer;

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
        System.setProperty("java.protocol.handler.pkgs",
                System.getProperty("java.protocol.handler.pkgs", "") +
                "|org.columba.mail.url");
        new MailConfig();
    }

    /* (non-Javadoc)
     * @see org.columba.core.main.DefaultMain#initGui()
     */
    public void initGui() {
        MailInterface.popServerCollection = new POP3ServerCollection();

        new MailResourceLoader();

        MailInterface.treeModel = new TreeModel(MailConfig.getFolderConfig());

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
        // Init PGP
        MimeTreeRenderer renderer = MimeTreeRenderer.getInstance();
        renderer.addMimePartRenderer(new MultipartSignedRenderer());
        renderer.addMimePartRenderer(new MultipartEncryptedRenderer());

        // Init Plugins
        MainInterface.pluginManager.registerHandler(new FilterActionPluginHandler());
        MainInterface.pluginManager.registerHandler(new FilterPluginHandler());
        MainInterface.pluginManager.registerHandler(new FolderPluginHandler());
        MainInterface.pluginManager.registerHandler(new POP3PreProcessingFilterPluginHandler());
        MainInterface.pluginManager.registerHandler(new TableRendererPluginHandler());
        MainInterface.pluginManager.registerHandler(new FolderOptionsPluginHandler());
        MainInterface.pluginManager.registerHandler(new MenuPluginHandler(
                "org.columba.mail.menu"));

        MainInterface.pluginManager.registerHandler(new MenuPluginHandler(
                "org.columba.mail.composer.menu"));

        MainInterface.pluginManager.registerHandler(new ImportPluginHandler());

        try {
            ((ActionPluginHandler) MainInterface.pluginManager.getHandler(
                "org.columba.core.action")).addActionList(
                "org/columba/mail/action/action.xml");
        } catch (PluginHandlerNotFoundException ex) {
            ex.printStackTrace();
        }

        TaskInterface plugin = new SaveAllFoldersPlugin();
        MainInterface.backgroundTaskManager.register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);
        
        plugin = new SavePOP3CachePlugin();
        MainInterface.backgroundTaskManager.register(plugin);
        ShutdownManager.getShutdownManager().register(plugin);
    }
}
