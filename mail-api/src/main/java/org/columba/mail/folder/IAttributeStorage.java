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

import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;


/**
 * Attribute storage for email messages.
 * <p>
 * Attributes can be modified and include numerous additional variables,
 * including message {@link Flags} and Columba-specific items.
 * <p>
 * NOTE: This interface is not used at all.
 * @see IHeaderListStorage
 *
 * @author fdietz
 *
 */
public interface IAttributeStorage {
    /**
     * Gets a attribute from the message
     * 
     * @param uid
     *            The UID of the message
     * @param key
     *            The name of the attribute (e.g. "columba.subject",
     *            "columba.size")
     * @return @throws
     *         Exception
     */
    public Object getAttribute(Object uid, String key) throws Exception;

    /**
     * Gets the attributes from the message
     * 
     * @param uid
     *            The UID of the message
     * @return @throws
     *         Exception
     */
    public Attributes getAttributes(Object uid) throws Exception;

    /**
     * Set attribute for message with UID.
     * 
     * @param uid
     *            UID of message
     * @param key
     *            name of attribute (e.g."columba.subject");
     * @param value
     *            value of attribute
     * @throws Exception
     */
    public void setAttribute(Object uid, String key, Object value)
            throws Exception;
    
    /**
     * @param uid
     *            UID of message
     * @return boolean true, if message exists
     * @throws Exception
     */
    public boolean exists(Object uid) throws Exception;
    
    /**
     * Gets the Flags of the message.
     * 
     * @param uid
     *            The UID of the message
     * @return @throws
     *         Exception
     */
    public Flags getFlags(Object uid) throws Exception;
    

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
