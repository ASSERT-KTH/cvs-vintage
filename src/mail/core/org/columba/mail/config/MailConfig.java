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

package org.columba.mail.config;

import org.columba.core.config.Config;
import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;

import java.io.File;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MailConfig {
    public static final String MODULE_NAME = "mail";
    
    protected Config config;
    protected File path;
    protected File accountFile;
    protected File accountTemplateFile;
    protected File folderFile;
    protected File mainFrameOptionsFile;
    protected File pop3Directory;
    protected File popManageOptionsFile;
    protected File composerOptionsFile;

    //private static File filterActionFile;
    //private static File localFilterFile;
    //private static File remoteFilterFile;

    /**
     * @see java.lang.Object#Object()
     */
    public MailConfig(Config config) {
        this.config = config;
        path = new File(config.getConfigDirectory(), MODULE_NAME);
        DiskIO.ensureDirectory(path);
        
        pop3Directory = new File(path, "pop3server");
        if (!pop3Directory.exists() || !pop3Directory.isDirectory()) {
            pop3Directory.mkdir();
        }

        accountFile = new File(path, "account.xml");
        registerPlugin(accountFile.getName(), new AccountXmlConfig(accountFile));

        accountTemplateFile = new File("account_template.xml");
        registerTemplatePlugin(accountTemplateFile.getName(),
            new AccountTemplateXmlConfig(accountTemplateFile));

        folderFile = new File(path, "tree.xml");
        registerPlugin(folderFile.getName(), new FolderXmlConfig(folderFile));

        mainFrameOptionsFile = new File(path, "options.xml");
        registerPlugin(mainFrameOptionsFile.getName(),
            new MainFrameOptionsXmlConfig(mainFrameOptionsFile));

        File mainToolBarFile = new File(path, "main_toolbar.xml");
        registerPlugin(mainToolBarFile.getName(),
            new DefaultXmlConfig(mainToolBarFile));

        File composerToolBarFile = new File(path,
                "composer_toolbar.xml");
        registerPlugin(composerToolBarFile.getName(),
            new DefaultXmlConfig(composerToolBarFile));

        File messageframeToolBarFile = new File(path,
                "messageframe_toolbar.xml");
        registerPlugin(messageframeToolBarFile.getName(),
            new DefaultXmlConfig(messageframeToolBarFile));

        /*
        popManageOptionsFile =
                new File(path, "popmanageoptions.xml");
        registerPlugin(
                popManageOptionsFile.getName(),
                new PopManageOptionsXmlConfig(popManageOptionsFile));
        */
        composerOptionsFile = new File(path, "composer_options.xml");
        registerPlugin(composerOptionsFile.getName(),
            new ComposerOptionsXmlConfig(composerOptionsFile));

        /*
        filterActionFile = new File(path, "filter_actions.xml");
        registerPlugin(
                filterActionFile.getName(),
                new FilterActionXmlConfig(filterActionFile));
        */
        /*
        localFilterFile = new File(path, "filter_local.xml");
        registerPlugin(
                localFilterFile.getName(),
                new LocalFilterXmlConfig(localFilterFile));
        remoteFilterFile = new File(path, "filter_remote.xml");
        registerPlugin(
                remoteFilterFile.getName(),
                new LocalFilterXmlConfig(remoteFilterFile));
        */
    }
    
    public File getConfigDirectory() {
        return path;
    }

    /**
     * Returns the POP3 directory.
     */
    public File getPOP3Directory() {
        return pop3Directory;
    }

    /**
     * Method registerPlugin.
     * @param id
     * @param plugin
     */
    protected void registerPlugin(String id, DefaultXmlConfig plugin) {
        config.registerPlugin(MODULE_NAME, id, plugin);
    }

    protected void registerTemplatePlugin(String id,
        DefaultXmlConfig plugin) {
        config.registerTemplatePlugin(MODULE_NAME, id, plugin);
    }

    /**
     * Method getPlugin.
     * @param id
     * @return DefaultXmlConfig
     */
    protected DefaultXmlConfig getPlugin(String id) {
        return config.getPlugin(MODULE_NAME, id);
    }

    protected DefaultXmlConfig getTemplatePlugin(String id) {
        return config.getTemplatePlugin(MODULE_NAME, id);
    }

    /**
     * Method getAccountList.
     * @return AccountList
     */
    public AccountList getAccountList() {
        return getAccountConfig().getAccountList();
    }

    public AccountTemplateXmlConfig getAccountTemplateConfig() {
        return (AccountTemplateXmlConfig) getTemplatePlugin(accountTemplateFile.getName());
    }

    public XmlElement get(String name) {
        DefaultXmlConfig xml = getPlugin(name + ".xml");

        return xml.getRoot();
    }

    /**
     * Method getAccountConfig.
     * @return AccountXmlConfig
     */
    public AccountXmlConfig getAccountConfig() {
        //return accountConfig;
        return (AccountXmlConfig) getPlugin(accountFile.getName());
    }

    /*
    public static FilterActionXmlConfig getFilterActionConfig() {
            return (FilterActionXmlConfig) getPlugin(filterActionFile.getName());
    }
    */
    /*
    public static LocalFilterXmlConfig getLocalFilterConfig() {
            return (LocalFilterXmlConfig) getPlugin(localFilterFile.getName());
    }
    public static RemoteFilterXmlConfig getRemoteFilterConfig() {
            return (RemoteFilterXmlConfig) getPlugin(remoteFilterFile.getName());
    }
    */

    /**
     * Method getFolderConfig.
     * @return FolderXmlConfig
     */
    public FolderXmlConfig getFolderConfig() {
        //return folderConfig;
        return (FolderXmlConfig) getPlugin(folderFile.getName());
    }

    /**
     * Method getMainFrameOptionsConfig.
     * @return MainFrameOptionsXmlConfig
     */
    public MainFrameOptionsXmlConfig getMainFrameOptionsConfig() {
        //return mainFrameOptionsConfig;
        return (MainFrameOptionsXmlConfig) getPlugin(mainFrameOptionsFile.getName());
    }

    /**
     * Method getComposerOptionsConfig.
     * @return ComposerOptionsXmlConfig
     */
    public ComposerOptionsXmlConfig getComposerOptionsConfig() {
        //return composerOptionsConfig;
        return (ComposerOptionsXmlConfig) getPlugin(composerOptionsFile.getName());
    }
}
