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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import org.apache.torque.pool.DBConnection;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.commons.util.GenerateUniqueId;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class contains common code for the use in ScarabUser implementations.
 * It would be preferrable that the code here be provided in an 
 * AbstractScarabUser that implementations could extend.  This is possible
 * to do with the turbine security user, but will require the build process
 * to build fulcrum's security service using a modified xml schema.  Until
 * that is done, functionality that is not implementation specific should
 * go here.
 * 
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: AbstractScarabUser.java,v 1.16 2002/03/01 15:44:22 jon Exp $
 */
public abstract class AbstractScarabUser 
    extends BaseObject 
{
    private int issueCount = 0;
    private Map issueMap;
    private int reportCount = 0;
    private Map reportMap;

    /**
        Call the superclass constructor to initialize this object.
    */
    public AbstractScarabUser()
    {
        super();
        issueMap = new HashMap();
        reportMap = new HashMap();
    }

    public abstract NumberKey getUserId();
    public abstract String getEmail();
    public abstract String getFirstName();
    public abstract String getLastName();
    public abstract boolean hasPermission(String perm, ModuleEntity module);


    /**
     * The user's full name.
     */
    public String getName()
    {
        String first = getFirstName();
        String last = getLastName();
        int firstlength = 0;
        int lastlength = 0;
        if (first != null) 
        {
            firstlength = first.length();            
        }
        if (last != null) 
        {
            lastlength = last.length();
        }        
        StringBuffer sb = new StringBuffer(firstlength + lastlength + 1);
        if (firstlength > 0 ) 
        {
            sb.append(first);
            if (lastlength > 0) 
            {
                sb.append(' ');
            }
        }
        if ( lastlength > 0) 
        {
            sb.append(last);
        }
        
        return sb.toString();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules()
     */
    public abstract List getModules() throws Exception;

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules()
     */
    public List getEditableModules() throws Exception
    {
        List userModules = getModules();
        ArrayList editModules = new ArrayList();

        for (int i=0; i<userModules.size(); i++)
        {
            ModuleEntity module = (ModuleEntity)userModules.get(i);
            if (hasPermission(ScarabSecurity.MODULE__EDIT, module)
               && !(module.getModuleId().equals(ModuleEntity.ROOT_ID)))
            {
                editModules.add(module);
            }
        }
        return editModules;
     }

    private static final String GET_R_MODULE_USERATTRIBUTES = 
        "getRModuleUserAttributes";


    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttributes(ModuleEntity, IssueType)
     */
    public List getRModuleUserAttributes(ModuleEntity module,
                                         IssueType issueType)
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_R_MODULE_USERATTRIBUTES, 
                                     module, issueType); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
                .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId())
                .addDescendingOrderByColumn(
                    RModuleUserAttributePeer.PREFERRED_ORDER);
            
            result = getRModuleUserAttributes(crit);
            ScarabCache.put(result, this, GET_R_MODULE_USERATTRIBUTES,  
                            module, issueType);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    protected abstract Vector getRModuleUserAttributes(Criteria crit)
        throws Exception;
            
    private static final String GET_R_MODULE_USERATTRIBUTE = 
        "getRModuleUserAttribute";


    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttribute(ModuleEntity, Attribute, IssueType)
     */
    public RModuleUserAttribute getRModuleUserAttribute(ModuleEntity module, 
                                                       Attribute attribute,
                                                       IssueType issueType)
        throws Exception
    {
        RModuleUserAttribute result = null;
        Object obj = ScarabCache.get(this, GET_R_MODULE_USERATTRIBUTE, 
                                     module, attribute, issueType); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria(4)
                .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId())
                .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId());
            List muas = RModuleUserAttributePeer.doSelect(crit);
            if ( muas.size() == 1 ) 
            {
                result = (RModuleUserAttribute)muas.get(0);
            }
            else if ( muas.size() == 0 )
            {
                result = new RModuleUserAttribute();
                result.setModuleId(module.getModuleId());
                result.setUserId(getUserId());
                result.setIssueTypeId(issueType.getIssueTypeId());
                result.setAttributeId(attribute.getAttributeId());
            }
            else 
            {
                throw new ScarabException(
                "Not sure, but this should probably only return one - jdm");
        }
            ScarabCache.put(result, this, GET_R_MODULE_USERATTRIBUTE, 
                            module, attribute, issueType);
        }
        else 
        {
            result = (RModuleUserAttribute)obj;
        }
        return result;
    }



    /* *
     * @see org.tigris.scarab.om.ScarabUser#getModules(Role)
     * /
    public List getModules(Role role) 
        throws Exception
    {
        if (true)
            throw new Exception ("FIXME: This method doesn't belong here!");
        return null;
    }
    */

    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
        throws Exception
    {
        return (Issue)issueMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(Issue)
     */
    public String setReportingIssue(Issue issue)
        throws ScarabException
    {
        String key = null;
        if ( issue == null ) 
        {
            throw new ScarabException("Null Issue is not allowed.");
        }
        else 
        {
            key = String.valueOf(issueCount++);
            setReportingIssue(key, issue);
        }
        return key;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(String, Issue)
     */
    public void setReportingIssue(String key, Issue issue)
    {
        if ( issue == null ) 
        {
            issueMap.remove(String.valueOf(key));
        }
        else 
        {
            issueMap.put(String.valueOf(key), issue);
        }
    }



    /**
     * @see org.tigris.scarab.om.ScarabUser#getCurrentReport(String)
     */
    public Report getCurrentReport(String key)
    {
        return (Report)reportMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(Report)
     */
    public String setCurrentReport(Report report)
        throws ScarabException
    {
        String key = null;
        if ( report == null ) 
        {
            throw new ScarabException("Null Report is not allowed.");
        }
        else 
        {
            key = String.valueOf(reportCount++);
            setCurrentReport(key, report);
        }
        return key;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(String, Report)
     */
    public void setCurrentReport(String key, Report report)
    {
        if ( report == null ) 
        {
            reportMap.remove(key);
        }
        else 
        {
            // make sure reports are not being accumulated, set a reasonable
            // limit of 10 open reports
            int intKey = Integer.parseInt(key);
            int count = 0;
            for (int i=intKey-1; i>=0; i--) 
            {
                String testKey = String.valueOf(i);
                if (getCurrentReport(testKey) != null) 
                {
                    if (++count > 10) 
                    {
                        reportMap.remove(testKey);
                    }
                }
            }
            
            reportMap.put(String.valueOf(key), report);
        }
    }


    private static final String GET_DEFAULT_QUERY_USER = 
        "getDefaultQueryUser";


    /**
     * Clears default query-user map for this module/issuetype.
     */
    public RQueryUser getDefaultQueryUser(ModuleEntity me, IssueType issueType)
        throws Exception
    {
        RQueryUser rqu = null;
        List result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_QUERY_USER, 
                                     me, issueType); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.add(RQueryUserPeer.USER_ID, getUserId());
            crit.add(RQueryUserPeer.ISDEFAULT, 1);
            crit.addJoin(RQueryUserPeer.QUERY_ID,
                     QueryPeer.QUERY_ID);
            crit.add(QueryPeer.MODULE_ID, me.getModuleId());
            crit.add(QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
            result = RQueryUserPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_DEFAULT_QUERY_USER,  
                            me, issueType);
        }
        else 
        {
            result = (List)obj;
        }
        if (result.size() > 0)
        {
            rqu = (RQueryUser)result.get(0);
        }
        return rqu;
    }

    /**
     * gets default query for this module/issuetype.
     */
    public Query getDefaultQuery(ModuleEntity me, IssueType issueType)
        throws Exception
    {
        Query query = null;
        RQueryUser rqu = getDefaultQueryUser(me, issueType);
        if (rqu != null)
        { 
            query = (Query)rqu.getQuery();
        }
        return query;
    }

    /**
     * Clears default query for this module/issuetype.
     */
    public void resetDefaultQuery(ModuleEntity me, IssueType issueType)
        throws Exception
    {
        RQueryUser rqu = getDefaultQueryUser(me, issueType);
        if (rqu != null)
        { 
            rqu.setIsdefault(false);
            rqu.save();
        }
    }

    /**
     * If user has no default query set, gets a default default query.
     */
    public String getDefaultDefaultQuery() throws Exception
    {
        StringBuffer buf = new StringBuffer("&searchcb=");
        buf.append(getEmail());
        return buf.toString();
    }

    /**
     * @see org.apache.torque.om.Persistent#save()
     * this implementation will throw a ScarabException as it is 
     * not really implemented here.
     */
    public void save() throws Exception
    {
        throw new ScarabException("Not implemented");
    }

    /**
     * @see org.apache.torque.om.Persistent#save(String)
     * this implementation will throw a ScarabException as it is 
     * not really implemented here.
     */
    public void save(String dbName) throws Exception
    {
        throw new ScarabException("Not implemented");
    }

    /**
     * @see org.apache.torque.om.Persistent#save(DBConnection)
     * this implementation will throw a ScarabException as it is 
     * not really implemented here.
     */
    public void save(DBConnection dbCon) throws Exception
    {
        throw new ScarabException("Not implemented");
    }
}
