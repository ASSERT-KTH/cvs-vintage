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
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

import org.apache.fulcrum.security.entity.User;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.TorqueException;

import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.util.ScarabException;

/**
 * This is an interface which describes what a ScarabUser is...
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabUser.java,v 1.90 2003/08/21 00:10:24 jmcnally Exp $
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
    void createNewUser() throws Exception;

    /**
     * Gets all modules the user has permissions to edit.
     * The default is to not show global modules if you have 
     * the permission to edit it.
     * @see #getEditableModules(Module)
     */
    List getEditableModules() throws Exception;

    /**
     * Gets all modules the user has permissions to edit.
     * @param currEditModule the module we are currently editing
     */
    List getEditableModules(Module currEditModule) throws Exception;

    /**
     * Gets an issue stored in the temp hash under key.
     *
     * @param key a <code>String</code> used as the key to retrieve the issue
     * @return an <code>Issue</code> value
     * @exception Exception if an error occurs
     */
    Issue getReportingIssue(String key)
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
    String setReportingIssue(Issue issue)
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
    void setReportingIssue(String key, Issue issue);

    /**
     * Gets an report stored in the temp hash under key.
     *
     * @param key a <code>String</code> used as the key to retrieve the report
     * @return an <code>Report</code> value
     * @exception Exception if an error occurs
     */
    ReportBridge getCurrentReport(String key)
        throws Exception;

    /**
     * Places an report into the session that can be retrieved using the key
     * that is returned from the method.
     *
     * @param report an <code>ReportBridge</code> to store in the session under a 
     * new key
     * @return a <code>String</code> value that can be used to retrieve 
     * the report
     * @exception ScarabException if report is null.
     */
    String setCurrentReport(ReportBridge report)
        throws ScarabException;

    /**
     * Places an report into the session under the given key.  If another report
     * was already using that key, it will be overwritten.  Giving a null report
     * removes any report stored using key.  This method is primarily used to
     * remove the report from storage.  Inserting a new report would be most 
     * likely done with setCurrentReport(ReportBridge report).
     *
     * @param key a <code>String</code> value under which to store the report
     * @param report an <code>ReportBridge</code> value to store, null removes any 
     * report already stored under key.
     */
    void setCurrentReport(String key, ReportBridge report);

    /** Used for the password management features */
    boolean isPasswordExpired() throws Exception;
    /** Used for the password management features */
    void setPasswordExpire() throws Exception;
    /** Used for the password management features */
    void setPasswordExpire(Calendar expire) throws Exception;
    
    Integer getUserId();
    void setUserId(Integer v) throws Exception;
    ObjectKey getPrimaryKey();
    void setPrimaryKey(ObjectKey v) throws Exception;

    /**
     * Returns list of RModuleUserAttribute objects for this
     * User and Module -- the attributes the user has selected
     * To appear on the IssueList for this module.
     */
    List getRModuleUserAttributes(Module module, 
                                         IssueType issueType)
            throws Exception;

    /**
     * Returns an RModuleUserAttribute object.
     */
    RModuleUserAttribute getRModuleUserAttribute(Module module, 
                                                        Attribute attribute,
                                                        IssueType issueType)
            throws Exception;

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    String getQueryKey();

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    void setQueryKey(String key) throws Exception;

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
    boolean hasPermission(String permission, Module module);

    /**
     * Returns true if this user has the given permission within all the given
     * modules, false otherwise.  If the list is null or empty
     */
    boolean hasPermission(String permission, List modules);

    /**
     * Gets all modules which are currently associated with this user 
     * (relationship has not been deleted.)
     */
    List getModules() throws Exception;

    /**
     * Gets all modules which are currently associated with this user.
     * @param showDeletedModules show modules which have been marked as deleted
     */
    List getModules(boolean showDeletedModules) throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * the specified permission. Does not show deleted modules.
     * (showDeleted = false)
     * @param permission a <code>String</code> value
     * @return a <code>Module[]</code> value
     */
    Module[] getModules(String permission) 
        throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * at least one of the permissions given. Does not show deleted modules.
     * (showDeleted = false)
     * @param permissions a <code>String[]</code> value
     * @return a <code>Module[]</code> value
     */
    Module[] getModules(String[] permissions) 
        throws Exception;

    /**
     * Get a list of <code>Module</code>'s that where a user has
     * at least one of the permissions given. Does not show deleted modules.
     *
     * @param permissions a <code>String[]</code> value
     * @param showDeleted a <code>boolean</code> value
     * @return a <code>Module[]</code> value
     */
    Module[] getModules(String[] permissions, boolean showDeleted) 
        throws Exception;

    List getCopyToModules(Module currentModule)
        throws Exception;
    List getCopyToModules(Module currentModule, String action)
        throws Exception;
    List getCopyToModules(Module currentModule, String action, String searchString)
        throws Exception;


    /**
     * Determine whether the user is associated with the given module.
     * This translates to a check whether the user has any permissions within
     * the module.
     *
     * @param module a <code>Module</code> value
     * @return a <code>boolean</code> value
     */
    boolean hasAnyRoleIn(Module module)
        throws Exception;

    /**
     * The user's full name.
     */
    String getName();

    /**
     * Sets integer representing user preference for
     * Which screen to return to after entering an issue.
     */
    void setEnterIssueRedirect(int templateCode)
        throws Exception;

    /**
     * Returns integer representing user preference for
     * Which screen to return to after entering an issue.
     */
    int getEnterIssueRedirect()
        throws Exception;

    /**
     * The template/tab to show for the home page using the current module.
     */
    String getHomePage()
        throws Exception;
    
    /**
     * The template/tab to show for the home page in the given module.
     */
    String getHomePage(Module module)
        throws Exception;

    /**
     * The template/tab to show for the home page.
     */
    void setHomePage(String homePage)
        throws Exception;

    /**
     * The template to show if the user is going to start a new query.
     * if the user has not selected a set of issue types it will return
     * IssueTypeList.vm.  If a list is already selected, then it will go to
     * AdvancedQuery.vm unless the list is only one issue type and the user
     * has last selected Custom query for that issue type.
     */
    public String getQueryTarget();

    /**
     * Setup the users preference when entering a query for the given
     * issue type.  Valid values of target are AdvancedQuery.vm 
     * and Search.vm (Custom query).
     */
    public void setSingleIssueTypeQueryTarget(IssueType type, String target);


    List getMITLists()
        throws TorqueException;

    /**
     * Checks if the user can search for issues of at least one issue type
     * in one module.
     */
    public boolean hasAnySearchableRMITs()
        throws Exception;

    /**
     * Returns a List of RModuleIssueTypes for which the user has the
     * permission to search for issues. 
     *
     * @param skipModule do not include issue types for this module.  Useful
     * for separating the current module.
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    List getSearchableRMITs(String searchField, String searchString, 
                            String sortColumn, String sortPolarity,
                            Module skipModule)
        throws Exception;

    /**
     * returns a list of RModuleIssueTypes for the given module, excluding
     * any that that have a corresponding MITListItem in the CurrentMITList.
     */
    public List getUnusedRModuleIssueTypes(Module module)
        throws Exception;

    void addRMITsToCurrentMITList(List rmits)
        throws TorqueException;

    MITList getCurrentMITList();
    void setCurrentMITList(MITList list);
    void removeItemsFromCurrentMITList(String[] ids);

    Object lastEnteredIssueTypeOrTemplate();
    void setLastEnteredIssueType(IssueType type);
    void setLastEnteredTemplate(Issue template);

    /**
     * The most recent query entered.  This method has the effect of loading
     * the MITList used for the query (if one exists) as the CurrentMITList
     * so it should only be called if the query is to be used.  
     * Use @see #hasMostRecentQuery() to determine existence.
     */
    String getMostRecentQuery();

    /**
     * The most recent query entered.
     */
    void setMostRecentQuery(String queryString);

    /**
     * Check if the user has a previous query
     */
    boolean hasMostRecentQuery();

    /**
     * key used to keep concurrent activities by the same
     * user from overwriting each others state.
     */
    Object getThreadKey();

    /**
     * key used to keep concurrent activities by the same
     * user from overwriting each others state.
     */
    void setThreadKey(Integer key);

    /**
     * Get the working list of associated users
     * For the AssignIssue screen
     */
    Map getAssociatedUsersMap()
        throws Exception;

    /**
     * Set the working list of associated users
     * For the AssignIssue screen
     */
    void setAssociatedUsersMap(Map associatedUsers)
        throws Exception;

    /**
     * Get the working list of associated users
     * For the AssignIssue screen
     */
    Map getSelectedUsersMap()
        throws Exception;

    /**
     * Set the working list of associated users
     * For the AssignIssue screen
     */
    void setSelectedUsersMap(Map selectedUsers)
        throws Exception;
    
    /**
     * The current module which represents the module
     * selected by the user within a request.
     */
    Module getCurrentModule();
    
    /**
     * The current module which represents the module
     * selected by the user within a request.
     */
    void setCurrentModule(Module  v);
     
    /**
     * The current issue type which represents the issue type
     * selected by the user within a request.
     */
    IssueType getCurrentIssueType()
        throws Exception;

    /**
     * The current issue type which represents the issue type
     * selected by the user within a request.
     */
    void setCurrentIssueType(IssueType  v);

    /**
     * The current RModuleIssueType which represents the module and issue type
     * selected by the user within a request.
     */
    RModuleIssueType getCurrentRModuleIssueType()
        throws Exception;

    /**
     * Updates the attributes shown in IssueList.vm
     * Removes any saved preferences for the current mit list or current module
     * and issue type.  And replaces them with the attributes given.
     * The order of the attributes is preserved.
     */
    void updateIssueListAttributes(List attributes)
        throws Exception;

    List getRoleNames(Module module)
       throws Exception;

    /**
     * Gets the users default locale from the users preferences.
     */
    Locale getLocale();

    /**
     * Saves a user's locale information under specific conditions.
     *
     * @param localeInfo Information regarding the user's locale.
     */
    void noticeLocale(Object localeInfo)
        throws Exception;
}
