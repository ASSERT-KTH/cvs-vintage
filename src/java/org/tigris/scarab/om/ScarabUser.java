package org.tigris.scarab.om;

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
import java.util.List;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.ScarabException;

/**
 * This is an interface which describes what a ScarabUser is...
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabUser.java,v 1.40 2001/10/20 00:10:44 elicia Exp $
 */
public interface ScarabUser extends User
{
    public void createNewUser() throws Exception;
    public List getModules() throws Exception;
    public List getModules(Role role) throws Exception;

    /**
     * Gets an issue stored in the temp hash under key.
     *
     * @param key a <code>String</code> used as the key to retrieve the issue
     * @return an <code>Issue</code> value
     * @exception Exception if an error occurs
     */
    public Issue getReportingIssue(String key)
        throws Exception;

    /**
     * Places an issue into the session that can be retrieved using the key
     * that is returned from the method.
     *
     * @param issue an <code>Issue</code> to store in the session under a 
     * new key
     * @return a <code>String</code> value that can be used to retrieve 
     * the issue
     * @exception ScarabException if issue is null.
     */
    public String setReportingIssue(Issue issue)
        throws ScarabException;

    /**
     * Places an issue into the session under the given key.  If another issue
     * was already using that key, it will be overwritten.  Giving a null issue
     * removes any issue stored using key.  This method is primarily used to
     * remove the issue from storage.  Inserting a new issue would be most 
     * likely done with setReportingIssue(Issue issue).
     *
     * @param key a <code>String</code> value under which to store the issue
     * @param issue an <code>Issue</code> value to store, null removes any 
     * issue already stored under key.
     */
    public void setReportingIssue(String key, Issue issue);

    public NumberKey getUserId();
    public List getRModuleUserAttributes(ModuleEntity module, 
                                         IssueType issueType)
            throws Exception;
    public RModuleUserAttribute getRModuleUserAttribute(ScarabModule module, 
                                                        Attribute attribute,
                                                        IssueType issueType)
            throws Exception;

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public String getQueryKey();

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public void setQueryKey(String key) throws Exception;
}
