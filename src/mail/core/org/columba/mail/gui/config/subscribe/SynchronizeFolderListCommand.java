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
package org.columba.mail.gui.config.subscribe;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.columba.core.command.Command;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.util.ListTools;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.imap.IMAPServer;
import org.columba.ristretto.imap.ListInfo;
import org.frappucino.checkabletree.CheckableItemImpl;


public class SynchronizeFolderListCommand extends Command {
    private Pattern delimiterPattern;
    private IMAPRootFolder root;
    private IMAPServer store;
    private TreeNode node;
    private String delimiter;

    /**
 * @param references
 */
    public SynchronizeFolderListCommand(SubscribeCommandReference reference) {
        super(new SubscribeCommandReference[] { reference });
    }

    /* (non-Javadoc)
 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
 */
    public void execute(WorkerStatusController worker)
        throws Exception {
        root = (IMAPRootFolder) ((SubscribeCommandReference) getReferences()[0]).getFolder();

        store = root.getServer();

        node = createTreeStructure();
    }

    private TreeNode createTreeStructure() throws Exception {
        ListInfo[] list = store.list("", "*");
        ListInfo[] lsub = store.fetchSubscribedFolders();

        // Create list of unsubscribed folders
        List subscribedFolders = Arrays.asList(lsub);
        List unsubscribedFolders = new LinkedList(Arrays.asList(list));
        ListTools.substract(unsubscribedFolders, subscribedFolders);

        // Now we have the subscribed folders in subscribedFolders
        // and the unsubscribed folders in unsubscribedFolders
        // Next step: Create a treestructure
        DefaultMutableTreeNode root = new CheckableItemImpl();

        // Initialize the Pattern
        String pattern = "([^\\" + list[0].getDelimiter() + "]+)\\" +
            list[0].getDelimiter() + "?";
        delimiterPattern = Pattern.compile(pattern);
        delimiter = list[0].getDelimiter();

        Iterator it = subscribedFolders.iterator();

        while (it.hasNext()) {
            ListInfoTreeNode node = insertTreeNode((ListInfo) it.next(), root);
            node.setSelected(true);
        }

        it = unsubscribedFolders.iterator();

        while (it.hasNext()) {
            ListInfoTreeNode node = insertTreeNode((ListInfo) it.next(), root);
            node.setSelected(false);
        }

        return root;
    }

    private ListInfoTreeNode insertTreeNode(ListInfo listInfo,
        DefaultMutableTreeNode parent) {
        Matcher matcher = delimiterPattern.matcher(listInfo.getName());
        DefaultMutableTreeNode actParent = parent;
        StringBuffer mailboxName = new StringBuffer();

        matcher.find();
        mailboxName.append(matcher.group(1));
        actParent = ensureChild(matcher.group(1), mailboxName.toString(),
                actParent);

        while (matcher.find()) {
            mailboxName.append(delimiter);
            mailboxName.append(matcher.group(1));
            actParent = ensureChild(matcher.group(1), mailboxName.toString(),
                    actParent);
        }

        return (ListInfoTreeNode) actParent;
    }

    private DefaultMutableTreeNode ensureChild(String name, String mailbox,
        DefaultMutableTreeNode parent) {
        Enumeration children = parent.children();
        ListInfoTreeNode node;

        while (children.hasMoreElements()) {
            node = (ListInfoTreeNode) children.nextElement();

            if (node.toString().equals(name)) {
                return node;
            }
        }

        node = new ListInfoTreeNode(name, mailbox);
        parent.add(node);

        return node;
    }

    /* (non-Javadoc)
 * @see org.columba.core.command.Command#updateGUI()
 */
    public void updateGUI() throws Exception {
        SubscribeDialog dialog = ((SubscribeCommandReference) getReferences()[0]).getDialog();

        dialog.syncFolderListDone(new DefaultTreeModel(node));
    }
}
