// The contents of this file are subject to the Mozilla Public License Version
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
package org.columba.mail.gui.composer.command;

import java.io.InputStream;
import java.util.List;

import org.columba.core.util.NullWorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.FolderTestHelper;
import org.columba.mail.folder.MailboxTestFactory;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.ristretto.message.InputStreamMimePart;

/**
 * @author fdietz
 *  
 */
public class ForwardInlineCommandTest extends AbstractComposerTestCase {

    /**
     * @param arg0
     */
    public ForwardInlineCommandTest(MailboxTestFactory factory, String arg0) {
        super(factory, arg0);
    }

    public void test() throws Exception {

        // add message "0.eml" as inputstream to folder
        String input = FolderTestHelper.getString(0);
        System.out.println("input=" + input);
        // create stream from string
        InputStream inputStream = FolderTestHelper
                .getByteArrayInputStream(input);
        // add stream to folder
        Object uid = getSourceFolder().addMessage(inputStream);

        // create Command reference
        FolderCommandReference[] ref = new FolderCommandReference[1];
        ref[0] = new FolderCommandReference(getSourceFolder(),
                new Object[] { uid});

        // create copy command
        ForwardInlineCommand command = new ForwardInlineCommand(ref);

        // execute command -> use mock object class as worker which does
        // nothing
        command.execute(NullWorkerStatusController.getInstance());

        // model should contain the data
        ComposerModel model = command.getModel();

        String subject = model.getSubject();

        assertEquals("Subject", "Fwd: test", subject);
    }
    
    public void testForewardWithAttachement() throws Exception {
        String input = FolderTestHelper.getString("0_attachement.eml");
        System.out.println("input=" + input);
        // create stream from string
        InputStream inputStream =
            FolderTestHelper.getByteArrayInputStream(input);
        // add stream to folder
        Object uid = getSourceFolder().addMessage(inputStream);
        // create Command refernce
        FolderCommandReference[] ref = new FolderCommandReference[1];
        ref[0] =
            new FolderCommandReference(getSourceFolder(), new Object[] { uid });
        // create copy command
        ForwardInlineCommand command = new ForwardInlineCommand(ref);
        //  execute command -> use mock object class as worker which does
        // nothing 
        command.execute(NullWorkerStatusController.getInstance());
        // model should contain the data
        ComposerModel model = command.getModel();
        List attachements = model.getAttachments();
        assertEquals("There should be one attachement", 1, attachements.size());
        Object mimePart = attachements.get(0);
        assertEquals(
            "Should be type of StreamableMimePart",
            true,
            (mimePart instanceof InputStreamMimePart));

    }
    
}