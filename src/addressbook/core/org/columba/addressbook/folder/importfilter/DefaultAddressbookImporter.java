// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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

package org.columba.addressbook.folder.importfilter;

import java.io.File;

import javax.swing.JOptionPane;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.util.AddressbookResourceLoader;

import org.columba.core.main.MainInterface;
import org.columba.core.plugin.Plugin;

/**
 * Abstract base class for addressbook data importers.
 */
public abstract class DefaultAddressbookImporter implements Plugin {
    public static int TYPE_FILE = 0;
    public static int TYPE_DIRECTORY = 1;
    
    protected AbstractFolder destinationFolder;
    protected File sourceFile;

    //protected AddressbookFolder tempFolder;
    protected int counter;

    public DefaultAddressbookImporter() {}

    public DefaultAddressbookImporter(File sourceFile, AbstractFolder destinationFolder) {
        this.sourceFile = sourceFile;
        this.destinationFolder = destinationFolder;
    }

    public void init() {
        counter = 0;

        //tempFolder = new AddressbookFolder(null,addressbookInterface);
    }

    /** ********* override the following messages ************************* */
    /**
     * override this method to specify type the wizard dialog will open the
     * correct file/directory dialog automatically
     */
    public int getType() {
        return TYPE_FILE;
    }

    /**
     * enter a description which will be shown to the user here
     */
    public String getDescription() {
        return "";
    }

    /**
     * this method does all the import work
     */
    public abstract void importAddressbook(File file) throws Exception;

    public void setSourceFile(File file) {
        this.sourceFile = file;
    }

    /**
     * set destination folder
     */
    public void setDestinationFolder(AbstractFolder folder) {
        destinationFolder = folder;
    }

    /**
     * counter for successfully imported messages
     */
    public int getCount() {
        return counter;
    }

    /**
     * this method calls your overridden importMailbox(File)-method and
     * handles exceptions
     */
    public void run() {
        try {
            importAddressbook(sourceFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(MainInterface.frameModel.getActiveFrame(),
                AddressbookResourceLoader.getString("dialog",
                    "addressbookimport", "addressbook_import_failed"), "",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getCount() == 0) {
            JOptionPane.showMessageDialog(MainInterface.frameModel.getActiveFrame(), 
                AddressbookResourceLoader.getString("dialog",
                    "addressbookimport", "addressbook_import_failed_2"), "",
                JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(MainInterface.frameModel.getActiveFrame(),
                AddressbookResourceLoader.getString("dialog",
                    "addressbookimport", "addressbook_import_was_successfull"),
                AddressbookResourceLoader.getString("dialog", "contact",
                    "information"),
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * use this method to save a message to the specified destination folder
     */
    protected void saveContact(Contact card) throws Exception {
        destinationFolder.add(card);

        counter++;
    }
}
