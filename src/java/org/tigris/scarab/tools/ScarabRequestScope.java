package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

// Turbine
import org.apache.turbine.services.pull.ApplicationTool;

// Scarab
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.ScarabUser;

import org.tigris.scarab.services.module.ModuleEntity;

/**
 * The is an object that is made available on a per request
 * basis. In other words, the lifecycle of the object is simply that it
 * is available as a new object for each and every request to the
 * system. This object need not be thread safe as it is tied to the
 * currently executing request. This object is made available in the
 * Context as $scarabR.
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabRequestScope.java,v 1.14 2001/08/29 23:35:49 dlr Exp $
 */
public interface ScarabRequestScope extends ApplicationTool
{
    /**
     * A Attribute object for use within the Scarab API.
     */
    public void setAttribute (Attribute attribute);

    /**
     * A Attribute object for use within the Scarab API.
     */
    public Attribute getAttribute() throws Exception;

    /**
     * A <code>User</code> object for use within the Scarab API,
     * generally <i>not</i> the user who is logged in.
     *
     * @param user A user used during this request.
     */
    public void setUser (ScarabUser user);

    /**
     * A <code>User</code> object for use within the Scarab API. This
     * is the result of whatever was set with <code>setUser()</code>
     * (generally <i>not</i> the user who is logged in).  It can
     * return <code>null</code> if <code>setUser()</code> has not been
     * previously called.  If you would like to get the currently
     * logged in <code>User</code>, retrieve that from the
     * data.getUser() method.
     *
     * @return A user used during this request.
     * @see org.tigris.scarab.tools.ScarabRequestTool#setUser(ScarabUser)
     */
    public ScarabUser getUser();

    /**
     * Return a specific User by ID from within the system.
     * You can pass in either a NumberKey or something that
     * will resolve to a String object as id.toString() is 
     * called on everything that isn't a NumberKey.
     */
    public ScarabUser getUser(Object id) throws Exception;

    /**
     * A Issue object for use within the Scarab API.
     */
    public void setIssue(Issue issue);

    /**
     * Get an Issue object. If it is the first time calling,
     * it will be a new blank issue object.
     *
     * @return a <code>Issue</code> value
     */
    public Issue getIssue() throws Exception;

    /**
     * Get a specific module by key value.
     *
     * @param key a <code>String</code> value
     * @return a <code>ModuleEntity</code> value
     */
    public ModuleEntity getModule(String key);

    /**
     * Gets the ModuleEntity associated with the information
     * passed around in the query string. Returns null if
     * the Module could not be found.
     */
    public ModuleEntity getCurrentModule();

    /**
     * Get a specific issue by key value.
     *
     * @param key a <code>String</code> value
     * @return a <code>Issue</code> value
     */
    public Issue getIssue(String key) throws Exception;
}
