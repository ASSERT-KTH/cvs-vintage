package org.tigris.scarab.services.user;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 


import org.apache.fulcrum.Service;
import org.apache.torque.om.ObjectKey;
import org.tigris.scarab.om.ScarabUser;
import java.util.List;

/**
 * This is the interface that describes a UserService implementation
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: UserService.java,v 1.2 2001/09/13 17:26:47 jmcnally Exp $
 */
public interface UserService extends Service
{
    /** The name of the service */
    public static final String SERVICE_NAME = "UserService";

    /**
     * Get the Class instance
     */
    public Class getUserClass();

    /**
     * Gets a new instance of a ScarabUser.
     *
     * @return a <code>ScarabUser</code> value
     * @exception Exception if an error occurs
     */
    public ScarabUser getInstance() throws Exception;

    /**
     * Gets an existing ScarabUser by id.
     *
     * @param key an <code>ObjectKey</code> value
     * @return a <code>ScarabUser</code> value
     * @exception Exception if an error occurs
     */
    public ScarabUser getInstance(ObjectKey key) throws Exception;

    /**
     * Return an instance of User based on username
     */
    public ScarabUser getInstance(String username) throws Exception;

    /**
     * Gets a list of ScarabUsers based on usernames.
     *
     * @param usernames a <code>String[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getUsers(String[] usernames) throws Exception;

    /**
     * Gets a list of ScarabUsers based on id's.
     *
     * @param userIds a <code>NumberKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getUsers(ObjectKey[] userIds) throws Exception;
}
