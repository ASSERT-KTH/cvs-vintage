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
package org.columba.mail.folder;

import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.message.Header;


/**
 * Storage for email headers. 
 * <p>
 * Email headers (Subject:, To:, etc.) can't be modified. 
 * 
 * @author fdietz
 *
 */
public interface IHeaderStorage {
    /**
     * @param uid
     *            UID of message
     * @return boolean true, if message exists
     * @throws Exception
     */
    public boolean exists(Object uid) throws Exception;
    
    /**
     * Gets all specified headerfields. An example headerfield might be
     * "Subject" or "From" (take care of lower/uppercaseletters).
     * <p>
     * Note, that you should use getAttributes() for fetching the internal
     * headerfields (for example: columba.subject, columba.flags.seen, etc.).
     * <p>
     * This method first tries to find the requested header in the header cache
     * (@see CachedFolder). If the headerfield is not cached, the message
     * source is parsed.
     * 
     * @param uid
     *            The uid of the desired message
     * @param keys
     *            The keys like defined in e.g. RFC2822
     * @return A {@link Header}containing the headerfields if they were
     *         present
     * @throws Exception
     */
    public Header getHeaderFields(Object uid, String[] keys) throws Exception;
    
    
    /**
     * Return list of headers.
     * 
     * @return HeaderList list of headers
     * @throws Exception
     */
    public IHeaderList getHeaderList() throws Exception;
    
    
    /**
     * Return array of uids this folder contains.
     *
     * @return Object[]                array of all UIDs this folder contains
     */
    public Object[] getUids() throws Exception;
    
    public void save() throws Exception;
    
    public void load() throws Exception;
    
    void removeMessage(Object uid) throws Exception;
    
}