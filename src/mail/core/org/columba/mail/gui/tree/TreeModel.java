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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.tree;

import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.xml.XmlElement;

import org.columba.mail.config.FolderItem;
import org.columba.mail.config.FolderXmlConfig;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.Root;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.folder.temp.TempFolder;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.mail.plugin.FolderPluginHandler;
import org.columba.mail.util.MailResourceLoader;

import java.util.Enumeration;
import java.util.MissingResourceException;

import javax.swing.tree.DefaultTreeModel;


/**
 * @author freddy
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of
 * type comments go to Window>Preferences>Java>Code Generation.
 */
public class TreeModel extends DefaultTreeModel {
    protected FolderXmlConfig folderXmlConfig;
    protected TempFolder tempFolder;
    private final Class[] FOLDER_ITEM_ARG = new Class[] { FolderItem.class };

    public TreeModel(FolderXmlConfig folderConfig) {
        super(new Root(folderConfig.getRoot().getElement("tree")));
        this.folderXmlConfig = folderConfig;

        // create temporary folder in "<your-config-folder>/mail/"
        tempFolder = new TempFolder(MainInterface.config.getConfigDirectory() +
                "/mail/");

        createDirectories(((AbstractFolder) getRoot()).getNode(),
            (AbstractFolder) getRoot());
    }

    public void createDirectories(XmlElement parentTreeNode,
        AbstractFolder parentFolder) {
        int count = parentTreeNode.count();
        XmlElement child;

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                child = parentTreeNode.getElement(i);

                String name = child.getName();

                if (name.equals("folder")) {
                    AbstractFolder folder = add(child, parentFolder);

                    if (folder != null) {
                        createDirectories(child, folder);
                    }
                }
            }
        }
    }

    public AbstractFolder add(XmlElement childNode, AbstractFolder parentFolder) {
        FolderItem item = new FolderItem(childNode);

        if (item == null) {
            return null;
        }

        // i18n stuff
        String name = null;

        //XmlElement.printNode(item.getRoot(), "");
        int uid = item.getInteger("uid");

        try {
            if (uid == 100) {
                name = MailResourceLoader.getString("tree", "localfolders");
            } else if (uid == 101) {
                name = MailResourceLoader.getString("tree", "inbox");
            } else if (uid == 102) {
                name = MailResourceLoader.getString("tree", "drafts");
            } else if (uid == 103) {
                name = MailResourceLoader.getString("tree", "outbox");
            } else if (uid == 104) {
                name = MailResourceLoader.getString("tree", "sent");
            } else if (uid == 105) {
                name = MailResourceLoader.getString("tree", "trash");
            } else if (uid == 106) {
                name = MailResourceLoader.getString("tree", "search");
            } else if (uid == 107) {
                name = MailResourceLoader.getString("tree", "templates");
            } else {
                name = item.get("property", "name");
            }

            item.set("property", "name", name);
        } catch (MissingResourceException ex) {
            name = item.get("property", "name");
        }

        // now instanciate the folder classes
        String type = item.get("type");
        FolderPluginHandler handler = null;

        try {
            handler = (FolderPluginHandler) MainInterface.pluginManager.getHandler(
                    "org.columba.mail.folder");
        } catch (PluginHandlerNotFoundException ex) {
            NotifyDialog d = new NotifyDialog();
            d.showDialog(ex);
        }

        // parent directory for mail folders
        // for example: ".columba/mail/"
        String path = MainInterface.config.getConfigDirectory() + "/mail/";
        Object[] args = { item, path };
        AbstractFolder folder = null;

        try {
            folder = (AbstractFolder) handler.getPlugin(type, args);
            parentFolder.add(folder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return folder;
    }

    public AbstractFolder getFolder(int uid) {
        AbstractFolder root = (AbstractFolder) getRoot();

        for (Enumeration e = root.breadthFirstEnumeration();
                e.hasMoreElements();) {
            AbstractFolder node = (AbstractFolder) e.nextElement();
            int id = node.getUid();

            if (uid == id) {
                return node;
            }
        }

        return null;
    }

    public AbstractFolder getTrashFolder() {
        return getFolder(105);
    }

    public AbstractFolder getImapFolder(int accountUid) {
        AbstractFolder root = (AbstractFolder) getRoot();

        for (Enumeration e = root.breadthFirstEnumeration();
                e.hasMoreElements();) {
            AbstractFolder node = (AbstractFolder) e.nextElement();
            FolderItem item = node.getFolderItem();

            if (item == null) {
                continue;
            }

            //if (item.get("type").equals("IMAPRootFolder")) {
            if (node instanceof IMAPRootFolder) {
                int account = item.getInteger("account_uid");

                if (account == accountUid) {
                    int uid = item.getInteger("uid");

                    return getFolder(uid);
                }
            }
        }

        return null;
    }

    public AbstractFolder getFolder(TreeNodeList list) {
        AbstractFolder parentFolder = (AbstractFolder) getRoot();

        if ((list == null) || (list.count() == 0)) {
            return parentFolder;
        }

        AbstractFolder child = parentFolder;

        for (int i = 0; i < list.count(); i++) {
            String str = list.get(i);
            child = findFolder(child, str);
        }

        return child;
    }

    public AbstractFolder findFolder(AbstractFolder parentFolder, String str) {
        AbstractFolder child;

        for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {
            child = (AbstractFolder) e.nextElement();

            if (child.getName().equals(str)) {
                return child;
            }
        }

        return null;
    }

    public SelectFolderDialog getSelectFolderDialog() {
        return new SelectFolderDialog();
    }

    public TempFolder getTempFolder() {
        return tempFolder;
    }
}
