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
import org.columba.ristretto.message.Header;

/**
 * Combination of {@link HeaderStorage} and {@link AttributeStorage}.
 * <p>
 * The current implementation combines both storage models. 
 * <p>
 * This may change in the future. Adding a more-failsafe attribute 
 * persistence mechanism might be on our wishlist. If so, we should
 * remove this interface and stick with the two above only.
 * 
 * @author fdietz 
 */
public interface HeaderListStorage extends HeaderStorage{

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
     * Gets the Flags of the message.
     * 
     * @param uid
     *            The UID of the message
     * @return @throws
     *         Exception
     */
    public Flags getFlags(Object uid) throws Exception;
   
    /**
     * Sets the Flags of the selected message.
     * 
     * @param uid			selected message uid
     * @param flags			new flags
     * @throws Exception
     */
    public void setFlags(Object uid, Flags flags) throws Exception;
     
    /**
     * @param uid
     * @param header
     * @param attributes
     * @param flags
     * @return
     * @throws Exception
     */
    public Object addMessage(Object uid, Header header, Attributes attributes, Flags flags)
    throws Exception;
    
    /**
     * @see org.columba.mail.folder.HeaderStorage#removeMessage(java.lang.Object)
     */
    void removeMessage(Object uid) throws Exception;
    
    
    
    /**
     * @see org.columba.mail.folder.HeaderStorage#save()
     */
    public void save() throws Exception;
    
    /**
     * @see org.columba.mail.folder.HeaderStorage#load()
     */
    public void load() throws Exception;
}
