//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Abstract testcase creates a folder in setUp and removes it in tearDown.
 * <p>
 * Create new testcases by subclassing this class and using getSourceFolder()
 * and getDestFolder() directly.
 * 
 * @author fdietz
 * @author redsolo
 */
public class AbstractFolderTest extends TestCase {

    /** A source folder. */
    protected MessageFolder sourceFolder;

    /** A destination folder. */
    protected MessageFolder destFolder;

    /** A set with all created folders. */
    private Set folders;

    private static int folderId = 0;

    private MailboxTstFactory factory;

    /**
     * Constructor for test.
     * <p>
     * This is used when executing this individual test only or
     * by the ant task.
     * <p>
     */
    public AbstractFolderTest(String test) {
        super(test);

        this.factory = new MHFolderFactory();
    }
    
    /**
     * Constructor for test.
     * <p>
     * Used by {@link AllTests}.
     */
    public AbstractFolderTest(MailboxTstFactory factory, String test) {
        super(test);

        this.factory = factory;
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        folders = new HashSet();
        sourceFolder = factory.createFolder(folderId++);
        folders.add(sourceFolder);
        destFolder = factory.createFolder(folderId++);
        folders.add(destFolder);
    }

    public MessageFolder createFolder() {
        MessageFolder folder = factory.createFolder(folderId++);
        folders.add(folder);
        
        return folder;
    }
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        for (Iterator iterator = folders.iterator(); iterator.hasNext();) {
            MessageFolder folder = (MessageFolder) iterator.next();
            File f = folder.getDirectoryFile();

            // delete all mails in folder
            File[] list = f.listFiles();

            for (int i = 0; i < list.length; i++) {
                list[i].delete();
            }

            // delete folder
            f.delete();
        }
        new File(FolderTstHelper.homeDirectory + "/folders/").delete();
    }

    /**
     * @return Returns the folder.
     */
    public MessageFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * @return Returns the destFolder.
     */
    public MessageFolder getDestFolder() {
        return destFolder;
    }
    
    /**
     * Empty test, so that the automatic ant task doesn't fail.
     *
     */
    public void testForAntTask() {
        
    }
}
