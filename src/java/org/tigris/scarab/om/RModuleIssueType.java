package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.List;
import java.util.ArrayList;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabConstants;

import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.MITListItem;
import org.tigris.scarab.om.MITListItemPeer;
import org.tigris.scarab.workflow.WorkflowFactory;

/** 
 * This class represents a RModuleIssueType
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: RModuleIssueType.java,v 1.36 2004/05/07 05:57:42 dabbous Exp $
 */
public  class RModuleIssueType 
    extends org.tigris.scarab.om.BaseRModuleIssueType
    implements Persistent
{

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule"); //EXCEPTION
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new."); //EXCEPTION
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        Integer id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects."); //EXCEPTION
        }
        setModuleId(id);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>Module</code> value
     */
    public Module getModule()
        throws TorqueException
    {
        Module module = null;
        Integer id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }


    /**
     * Checks if user has permission to delete module-issue type mapping.
     */
    public void delete(ScarabUser user)
         throws Exception
    {                
        Module module = getModule();
        IssueType issueType = getIssueType();

        if (user.hasPermission(ScarabSecurity.MODULE__CONFIGURE, module))
        {
            // Delete both active and inactive attribute groups first
            List attGroups = issueType.getAttributeGroups(module, false);
            for (int j=0; j<attGroups.size(); j++)
            {
                // delete attribute-attribute group map
                AttributeGroup attGroup = 
                              (AttributeGroup)attGroups.get(j);
                attGroup.delete();
            }

            // Delete mappings with user attributes
            List rmas = module.getRModuleAttributes(issueType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete();
            }
            // Delete mappings with user attributes for template type
            IssueType templateType = issueType.getTemplateIssueType();
            rmas = module.getRModuleAttributes(templateType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete();
            }
 
            // delete workflows
            WorkflowFactory.getInstance().resetAllWorkflowsForIssueType(module, 
                                                                        issueType);
            // delete templates
            Criteria c = new Criteria()
                .add(IssuePeer.TYPE_ID, templateType.getIssueTypeId());
            List result = IssuePeer.doSelect(c);
            List issueIdList = new ArrayList(result.size());
            for (int i=0; i < result.size(); i++) 
            {
                Issue issue = (Issue)result.get(i);
                issueIdList.add(issue.getIssueId());
            }
            IssuePeer.doDelete(c);
            if (!issueIdList.isEmpty())
            {
                c = new Criteria()
                    .addIn(IssueTemplateInfoPeer.ISSUE_ID, issueIdList);
                IssueTemplateInfoPeer.doDelete(c);
            }
            
            // mit list items
            c = new Criteria()
                .add(MITListItemPeer.MODULE_ID, module.getModuleId())
                .add(MITListItemPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
            List listItems = MITListItemPeer.doSelect(c);
            List listIds = new ArrayList(listItems.size());

            for (int i=0; i < listItems.size(); i++) 
            {
                MITList list = ((MITListItem)listItems.get(i)).getMITList();
                Long listId = list.getListId();
                if (list.isSingleModuleIssueType() && !listIds.contains(listId))
                {
                    listIds.add(listId);
                }
            }
            MITListItemPeer.doDelete(c);

            if (!listIds.isEmpty())
            {
                // delete single module-issuetype mit lists
                c = new Criteria()
                    .addIn(MITListPeer.LIST_ID, listIds);
                MITListPeer.doDelete(c);

                // delete queries
                c = new Criteria()
                    .addIn(QueryPeer.LIST_ID, listIds);
                QueryPeer.doDelete(c);
            }

            c = new Criteria()
                .add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, getIssueTypeId());
            RModuleIssueTypePeer.doDelete(c);
            RModuleIssueTypeManager.removeFromCache(this);
            List rmits = module.getRModuleIssueTypes();
            rmits.remove(this);
        }
        else
        {
            throw new ScarabException(L10NKeySet.YouDoNotHavePermissionToAction);
        }
    }

    /**
     * Not really sure why getDisplayText was created because 
     * it really should just be getDisplayName() (JSS)
     *
     * @see #getDisplayText()
     */
    public String getDisplayName()
    {
        String display = super.getDisplayName();
        if (display == null)
        {
            try
            {
                display = getIssueType().getName();
            }
            catch (TorqueException e)
            {
                getLog().error("Error getting the issue type name: ", e);
            }
        }
        return display;
    }

    /**
     * Gets name to display. First tries to get the DisplayName 
     * for the RMIT, if that is null, then it will get the IssueType's
     * name and use that.
     *
     * @deprecated use getDisplayName() instead
     */
    public String getDisplayText()
    {
        return this.getDisplayName();
    }

    public String getDisplayDescription()
    {
        String display = super.getDisplayDescription();
        if (display == null)
        {
            try
            {
                display = getIssueType().getDescription();
            }
            catch (TorqueException e)
            {
                getLog().error("Error getting the issue type description: ", e);
            }
        }
        return display;
    }

    /**
     * Copies object.
     */
    public RModuleIssueType copy()
         throws TorqueException
    {
        RModuleIssueType rmit2 = new RModuleIssueType();
        rmit2.setModuleId(getModuleId());
        rmit2.setIssueTypeId(getIssueTypeId());
        rmit2.setActive(getActive());
        rmit2.setDisplay(getDisplay());
        rmit2.setDisplayDescription(getDisplayDescription());
        rmit2.setOrder(getOrder());
        rmit2.setDedupe(getDedupe());
        rmit2.setHistory(getHistory());
        rmit2.setComments(getComments());
        return rmit2;
    }

}
