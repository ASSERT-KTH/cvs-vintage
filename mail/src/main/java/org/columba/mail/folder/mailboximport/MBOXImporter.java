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

package org.columba.mail.folder.mailboximport;

import java.io.File;
import java.io.FileInputStream;

import org.columba.api.command.IWorkerStatusController;
import org.columba.core.io.SteerableInputStream;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.mbox.MboxMessage;
import org.columba.mail.folder.mbox.MboxParser;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.io.FileSource;

public class MBOXImporter extends AbstractMailboxImporter {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.mail.folder.mailboximport"); //$NON-NLS-1$
	
    public MBOXImporter() {
        super();
    }

    public MBOXImporter(IMailbox destinationFolder, File[] sourceFiles) {
        super(destinationFolder, sourceFiles);
    }

    public int getType() {
        return TYPE_FILE;
    }

    public void importMailboxFile(File file, IWorkerStatusController worker,
    		IMailbox destFolder) throws Exception {
    	FileSource mboxSource = new FileSource(file);
    	MboxMessage[] messages = MboxParser.parseMbox(mboxSource);
    	mboxSource.close();
    	
    	LOG.info("Found " + messages.length + " messages in MBOX file"); //$NON-NLS-1$ //$NON-NLS-2$
    	
    	SteerableInputStream in = new SteerableInputStream(new FileInputStream(file));
    	
    	worker.setProgressBarMaximum(messages.length);
    	for( int i=0; i<messages.length && !worker.cancelled(); i++) {
    		worker.setProgressBarValue(i);
    		in.setPosition(messages[i].getStart());
    		in.setLengthLeft(messages[i].getLength());
    		destFolder.addMessage(in);
    		// this is necessary to do!
    		counter++;
    	}
    	
    	in.finalClose();
    }

    public String getDescription() {
        return MailResourceLoader.getString("dialog", "mailboximport",
            "MBOX_description");
    }
}
