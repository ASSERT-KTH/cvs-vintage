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
package org.columba.mail.filter.plugins;

import java.io.ByteArrayInputStream;
import java.io.File;

import junit.framework.TestCase;

import org.columba.mail.folder.FolderTstHelper;
import org.columba.mail.folder.MailboxTstFactory;
import org.columba.mail.folder.MessageFolder;

/**
 * Base class for all filter tests.
 * <p>
 * Provides a test folder.
 * 
 * @author fdietz
 *  
 */
public class AbstractFilterTestCase extends TestCase {

    protected MessageFolder sourceFolder;
    protected MailboxTstFactory factory;

    /**
     * Constructor for AbstractFilterTest.
     * @param arg0
     * @param factory TODO
     */
    public AbstractFilterTestCase(MailboxTstFactory factory, String arg0 ) {
        super(arg0);
        this.factory = factory;
    }

    /**
     * @return Returns the folder.
     */
    public MessageFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	
    	sourceFolder = factory.createFolder(1);
    	
        //		 create MH folder
        // -> use homeDirectory as top-level folder
        // -> this has to be an absolute path
/*        sourceFolder = new CachedMHFolder("test", "CachedMHFolder",
                FolderTstHelper.homeDirectory + "/folders/");*/
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        File f = sourceFolder.getDirectoryFile();

        // delete all mails in folder
        File[] list = f.listFiles();

        for (int i = 0; i < list.length; i++) {
            list[i].delete();
        }

        // delete folder
        f.delete();
        
        sourceFolder.removeFolder();
    }

    /**
     * Add message to test folder.
     * 
     * @throws Exception
     */
    public Object addMessage() throws Exception {
        // add message "0.eml" as inputstream to folder
        String input = FolderTstHelper.getString(0);
        System.out.println("input=" + input);

        // create stream from string
        ByteArrayInputStream inputStream = FolderTstHelper
                .getByteArrayInputStream(input);

        // add stream to folder
        Object uid = getSourceFolder().addMessage(inputStream);

        // close stream
        inputStream.close();

        return uid;
    }
    
    /**
     * Empty test, so that the automatic ant task doesn't fail.
     *
     */
    public void testForAntTask() {
        
    }
}