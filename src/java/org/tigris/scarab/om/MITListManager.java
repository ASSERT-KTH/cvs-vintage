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
import java.util.Iterator;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.localization.Localization;

import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.om.ScarabUser;

/** 
 * This class manages MITList objects.
 */
public class MITListManager
    extends BaseMITListManager
{
    /**
     * Creates a new <code>MITListManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public MITListManager()
        throws TorqueException
    {
        super();
    }

    public static MITList getSingleItemList(Module module, IssueType issueType,
                                            ScarabUser user)
        throws TorqueException
    {
        MITList list = getInstance();
        if (user != null) 
        {
            list.setScarabUser(user);
        }
        MITListItem item = MITListItemManager.getInstance();
        item.setModule(module);
        item.setIssueType(issueType);
        list.addMITListItem(item);
        return list;
    }

    public static MITList getSingleModuleAllIssueTypesList(Module module,
                                                           ScarabUser user)
        throws TorqueException
    {
        MITList list = MITListManager.getInstance();
        list.setModifiable(false);
        list.setScarabUser(user);
        list.setName(Localization.format(ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         user.getLocale(),
                                         "AllIssueTypesCurrentModule", 
                                         module.getRealName()));
        
        MITListItem item = MITListItemManager.getInstance();
        item.setModule(module);
        item.setIssueTypeId(MITListItem.MULTIPLE_KEY);
        list.addMITListItem(item);
        return list;
    }

    public static MITList getAllModulesAllIssueTypesList(ScarabUser user)
        throws TorqueException
    {
        MITList list = MITListManager.getInstance();
        list.setModifiable(false);
        list.setScarabUser(user);
        list.setName(Localization.getString(ScarabConstants.DEFAULT_BUNDLE_NAME,
                                      user.getLocale(),
                                      "AllModulesAndIssueTypes"));

        MITListItem item = MITListItemManager.getInstance();
        item.setModuleId(MITListItem.MULTIPLE_KEY);
        list.addMITListItem(item);
        item.setIssueTypeId(MITListItem.MULTIPLE_KEY);
        return list;
    }

    public static MITList getAllModulesSingleIssueTypeList(IssueType issueType,
                                                           ScarabUser user)
        throws TorqueException
    {
        MITList list = MITListManager.getInstance();
        list.setModifiable(false);
        list.setScarabUser(user);
        list.setName(Localization.format(ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         user.getLocale(),
                                         "CurrentIssueTypeAllModules", 
                                         issueType.getName()));

        MITListItem item = MITListItemManager.getInstance();
        item.setModuleId(MITListItem.MULTIPLE_KEY);
        item.setIssueType(issueType);
        list.addMITListItem(item);
        return list;
    }

    /**
     * An issue has an associated Module and IssueType, this method takes
     * a list of issues and creates an MITList from these associations.
     *
     * @param issues a <code>List</code> value
     * @param user a <code>ScarabUser</code> value
     * @return a <code>MITList</code> value
     * @exception TorqueException if an error occurs
     */
    public static MITList getInstanceFromIssueList(List issues, 
                                                   ScarabUser user)
        throws TorqueException
    {
        if (issues == null) 
        {
            throw new IllegalArgumentException("Null issue list is not allowed."); //EXCEPTION
        }        
        if (user == null) 
        {
            throw new IllegalArgumentException("Null user is not allowed."); //EXCEPTION
        }
        
        MITList list = getInstance();
        list.setScarabUser(user);
        List dupeCheck = list.getMITListItems();
        Iterator i = issues.iterator();
        if (i.hasNext()) 
        {
            Issue issue = (Issue)i.next();
            MITListItem item = MITListItemManager.getInstance();
            item.setModule(issue.getModule());
            item.setIssueType(issue.getIssueType());
            if (!dupeCheck.contains(item)) 
            {
                list.addMITListItem(item);
            }
        }
        
        return list;
    }

    public static MITList getInstanceByName(String name, ScarabUser user)
        throws TorqueException
    {
        MITList result = null;
        Criteria crit = new Criteria();
        crit.add(MITListPeer.NAME, name);
        crit.add(MITListPeer.ACTIVE, true);
        crit.add(MITListPeer.USER_ID, user.getUserId());
        List mitLists = MITListPeer.doSelect(crit);
        if (mitLists != null && !mitLists.isEmpty()) 
        {
            result = (MITList)mitLists.get(0);
            // it is not good if more than one active list has the 
            // same name (per user).  We could throw an exception here
            // but its possible the system can still function under
            // this circumstance, so just log it for now.
            Log.get().error("Multiple active lists exist with list name="
                            + name + " for user=" + user.getUserName() + 
                            "("+user.getUserId()+")");
        }
        return result;
    }
}





