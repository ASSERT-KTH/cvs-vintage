package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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
import java.util.Calendar;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.ScarabException;

/**
 * This is an interface which describes what a ScarabUser is...
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabUser.java,v 1.58 2002/03/15 04:07:59 jon Exp $
 */
public interface ScarabUser extends User
{
    /**
     * This method is responsible for creating a new user. It will throw an 
     * exception if there is any sort of error (such as a duplicate login id) 
     * and place the error message into e.getMessage(). This also creates a 
     * uniqueid and places it into this object in the perm table under the
     * Visitor.CONFIRM_VALUE key. It will use the current instance of this
     * object as the basis to create the new User.
     */
    public void createNewUser() throws Exception;

    /**
     * Gets all modules the user has permissions to edit.
     * The default is to not show global modules if you have 
     * the permission to edit it.
     * @see #getEditableModules(boolean)
     */
    public List getEditableModules() throws Exception;

    /**
     * Gets all modules the user has permissions to edit.
     * @param currEditModule the module we are currently editing
     */
    public List getEditableModules(Module currEditModule) throws Exception;

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

    /**
     * Gets an report stored in the temp hash under key.
     *
     * @param key a <code>String</code> used as the key to retrieve the report
     * @return an <code>Report</code> value
     * @exception Exception if an error occurs
     */
    public Report getCurrentReport(String key)
        throws Exception;

    /**
     * Places an report into the session that can be retrieved using the key
     * that is returned from the method.
     *
     * @param report an <code>Report</code> to store in the session under a 
     * new key
     * @return a <code>String</code> value that can be used to retrieve 
     * the report
     * @exception ScarabException if report is null.
     */
    public String setCurrentReport(Report report)
        throws ScarabException;

    /**
     * Places an report into the session under the given key.  If another report
     * was already using that key, it will be overwritten.  Giving a null report
     * removes any report stored using key.  This method is primarily used to
     * remove the report from storage.  Inserting a new report would be most 
     * likely done with setCurrentReport(Report report).
     *
     * @param key a <code>String</code> value under which to store the report
     * @param report an <code>Report</code> value to store, null removes any 
     * report already stored under key.
     */
    public void setCurrentReport(String key, Report report);

    /**
     * Gets default query-user map for this module/issue type.
     */
    public RQueryUser getDefaultQueryUser(Module me, IssueType issueType)
        throws Exception;

    /**
     * Gets default query for this module/issuetype.
     */
    public Query getDefaultQuery(Module me, IssueType issueType)
        throws Exception;

    /**
     * Clears default query for this module/issuetype.
     */
    public void resetDefaultQuery(Module me, IssueType issueType)
        throws Exception;


    /** Used for the password management features */
    public boolean isPasswordExpired() throws Exception;
    /** Used for the password management features */
    public void setPasswordExpire() throws Exception;
    /** Used for the password management features */
    public void setPasswordExpire(Calendar expire) throws Exception;
    
    public NumberKey getUserId();
    public void setUserId(NumberKey v) throws Exception;
    public ObjectKey getPrimaryKey();
    public void setPrimaryKey(ObjectKey v) throws Exception;


    /**
     * Returns list of RModuleUserAttribute objects for this
     * User and Module -- the attributes the user has selected
     * To appear on the IssueList for this module.
     */
    public List getRModuleUserAttributes(Module module, 
                                         IssueType issueType)
            throws Exception;

    /**
     * Returns an RModuleUserAttribute object.
     */
    public RModuleUserAttribute getRModuleUserAttribute(Module module, 
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

    /**
     * Returns true if this user has the given permission within the given
     * module, false otherwise.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @param module a <code>Module</code> value
     * @return true if the permission exists for the user within the
     * given module, false otherwise
     */
    public boolean hasPermission(String perm, Module module);

    /**
     * Gets all modules which are currently associated with this user 
     * (relationship has not been deleted.)
     */
    public List getModules() throws Exception;

    /**
     * Gets all modules which are currently associated with this user.
     * @param showDeletedModules show modules which have been marked as deleted
     */
    public List getModules(boolean showDeletedModules) throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * the permissions given. Does not show deleted modules.
     * (showDeleted = false)
     * @param permissions a <code>String</code> value
     * @return a <code>Module[]</code> value
     */
    public Module[] getModules(String permission) 
        throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * at least one of the permissions given. Does not show deleted modules.
     * (showDeleted = false)
     * @param permissions a <code>String[]</code> value
     * @return a <code>Module[]</code> value
     */
    public Module[] getModules(String[] permissions) 
        throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * at least one of the permissions given. Does not show deleted modules.
     *
     * @param permissions a <code>String[]</code> value
     * @param showDeleted a <code>boolean</code> value
     * @return a <code>Module[]</code> value
     */
    public Module[] getModules(String[] permissions, boolean showDeleted) 
        throws Exception;

    /**
     * Determine whether the user is associated with the given module.
     * This translates to a check whether the user has any permissions within
     * the module.
     *
     * @param module a <code>Module</code> value
     * @return a <code>boolean</code> value
     */
    public boolean hasAnyRoleIn(Module module)
        throws Exception;

    /**
     * The user's full name.
     */
    public String getName();
}
