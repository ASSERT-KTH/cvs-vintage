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
package org.columba.mail.folder;

import java.io.InputStream;

import org.columba.ristretto.io.Source;


/**
 * Interface for local folders. This is a complete separation from the
 * headercache and the datastorage method.
 * <p>
 * This makes it very easy to add new mailbox formats.
 * <p>
 * {@link LocalFolder} uses this interface and the singleton pattern
 * to make this work in a plug'n'play manner.
 * <p>
 * If you want to add another custom local mailbox format, like mbox,
 * or maildir, etc. this is the interface you have to implement.
 * <p>
 * @see org.columba.mail.folder.LocalFolder
 *
 * @author fdietz
 */
public interface DataStorageInterface {
    /**
 * Remove message from datastorage
 *
 * @param uid                        UID of message
 * @throws Exception
 */
    public void removeMessage(Object uid) throws Exception;

    /**
 * Get message source from data storage
 *
 * @param uid                        UID of message
 * @return                                source of message
 * @throws Exception
 */
    public Source getMessageSource(Object uid) throws Exception;

    /**
 * Save message in data storage.
 *
 * @param uid                        UID of message
 * @param source                message source as stream
 * @throws Exception
 */
    public void saveMessage(Object uid, InputStream source)
        throws Exception;

    /**
 * Gets the total message count.
 *
 * @return                                return message count
 */
    public int getMessageCount();

    /**
 * Check if message with UID exists in data storage
 *
 * @param uid                        UID of message
 * @return                                true, if message exists. false, otherwise
 * @throws Exception
 */
    public boolean exists(Object uid) throws Exception;

    /**
 * Get list of message UIDs managed by this data storage
 *
 * @return                array containing message UIDs
 */
    public Object[] getMessageUids();
}
