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

package org.columba.mail.gui.config.mailboximport;

import java.io.File;

import net.javaprog.ui.wizard.DataModel;
import net.javaprog.ui.wizard.WizardModelEvent;
import net.javaprog.ui.wizard.WizardModelListener;

import org.columba.core.main.MainInterface;
import org.columba.mail.command.ImportFolderCommandReference;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.command.ImportMessageCommand;
import org.columba.mail.folder.mailboximport.DefaultMailboxImporter;
import org.columba.mail.plugin.ImportPluginHandler;

class MailboxImporter implements WizardModelListener {
    protected DataModel data;
    
    public MailboxImporter(DataModel data) {
        this.data = data;
    }
    
    public void wizardFinished(WizardModelEvent e) {
        ImportPluginHandler pluginHandler = 
                (ImportPluginHandler)data.getData("Plugin.handler");
        DefaultMailboxImporter importer = null;
        Object[] args = new Object[] {
            data.getData("Location.destination"),
            data.getData("Location.source")
        };
        try {
                importer = (DefaultMailboxImporter)pluginHandler.getPlugin(
                        (String)data.getData("Plugin.ID"), args);
        } catch (Exception ex) {
                ex.printStackTrace();
                return;
        }
        ImportFolderCommandReference[] r = new ImportFolderCommandReference[] {
            new ImportFolderCommandReference((FolderTreeNode)args[0],
                                                    (File[])args[1], importer)
        };
        MainInterface.processor.addOp(new ImportMessageCommand(r));
    }
    
    public void stepShown(WizardModelEvent e) {}
    public void wizardCanceled(WizardModelEvent e) {}
}
