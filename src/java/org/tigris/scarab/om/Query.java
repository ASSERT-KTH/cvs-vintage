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
import java.util.Arrays;
import java.util.Date;

import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.tools.ScarabLocalizationTool;

/** 
 * This class manages the Query table.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: Query.java,v 1.59 2003/04/30 01:11:05 jon Exp $
 */
public class Query 
    extends org.tigris.scarab.om.BaseQuery
    implements Persistent
{
    private static final String GET_R_QUERY_USER = 
        "getRQueryUser";

    /**
     * A local reference, so that getScarabUser is guaranteed to return
     * the same instance as was passed to setScarabUser
     */
    private ScarabUser scarabUser;
    
    /**
     * Get the value of scarabUser.
     * @return value of scarabUser.
     */
    public ScarabUser getScarabUser() 
        throws TorqueException
    {
        ScarabUser user = this.scarabUser;
        if (user == null) 
        {
            user = super.getScarabUser();
        }
        
        return user;
    }
    
    /**
     * Set the value of scarabUser.
     * @param v  Value to assign to scarabUser.
     */
    public void setScarabUser(ScarabUser  v) 
        throws TorqueException
    {
        this.scarabUser = v;
        super.setScarabUser(v);
    }
    
    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule");
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new.");
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        if (me == null) 
        {
            setModuleId((Integer)null);            
        }
        else 
        {
            Integer id = me.getModuleId();
            if (id == null) 
            {
                throw new TorqueException("Modules must be saved prior to " +
                    "being associated with other objects.");
            }
            setModuleId(id);
        }        
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
     * A new Query object
     */
    public static Query getInstance() 
    {
        return new Query();
    }

    public boolean canDelete(ScarabUser user)
        throws Exception
    {
        // can delete a query if they have delete permission
        // Or if is their personal query
        return (user.hasPermission(ScarabSecurity.ITEM__DELETE, getModule())
                || (user.getUserId().equals(getUserId()) 
                   && (getScopeId().equals(Scope.PERSONAL__PK))));
    }

    public boolean canEdit(ScarabUser user)
        throws Exception
    {
        return canDelete(user);
    }

    public boolean saveAndSendEmail(ScarabUser user, Module module, 
                                    TemplateContext context)
        throws Exception
    {
        // If it's a module scoped query, user must have Item | Approve 
        //   permission, Or its Approved field gets set to false
        boolean success = true;
        if (getScopeId().equals(Scope.PERSONAL__PK) 
            || user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
        }
        else
        {
            setApproved(false);

            // Send Email to the people with module edit ability so
            // that they can approve the new template
            if (context != null)
            {
                String template = Turbine.getConfiguration().
                    getString("scarab.email.requireapproval.template",
                              "RequireApproval.vm");

                ScarabUser[] toUsers = module
                    .getUsers(ScarabSecurity.ITEM__APPROVE);

                if (Log.get().isDebugEnabled()) 
                {
                    if (toUsers == null || toUsers.length ==0) 
                    {
                        Log.get().debug("No users to approve query.");    
                    }
                    else 
                    {
                        Log.get().debug("Users to approve query: ");    
                        for (int i=0; i<toUsers.length; i++) 
                        {
                            Log.get().debug(toUsers[i].getEmail());
                        }  
                    }          
                }
                
                EmailContext ectx = new EmailContext();
                ectx.setUser(user);
                ectx.setModule(module);
                ectx.setDefaultTextKey("NewQueryRequiresApproval");

                String fromUser = "scarab.email.default";
                if (!Email.sendEmail(ectx, module, 
                    fromUser, module.getSystemEmail(), Arrays.asList(toUsers),
                    null, template))
                {
                    success = false;
                }
            }
        }
        if (getMITList() != null) 
        {
            getMITList().save();
            // it would be good if this updated our list id, but it doesn't
            // happen automatically so reset it.
            setMITList(getMITList());            
        }
        save();
        return success;
    }

    /**
     * Subscribes user to query.
     */
    public void subscribe(ScarabUser user, Integer frequencyId)
        throws Exception
    {
        RQueryUser rqu = getRQueryUser(user);
        rqu.setSubscriptionFrequency(frequencyId);
        rqu.setIsSubscribed(true);
        rqu.save();
    }

    /**
     * Unsubscribes user from query.
     */
    public void unSubscribe(ScarabUser user)
        throws Exception
    {
        RQueryUser rqu = getRQueryUser(user);
        if (rqu.getIsdefault())
        {
            rqu.setIsSubscribed(false);
            rqu.save();
        }
        else
        {
            rqu.delete(user);
        }
    }


    /**
     * Gets RQueryUser object for this query and user.
     */
    public RQueryUser getRQueryUser(ScarabUser user)
        throws Exception
    {
        RQueryUser result = null;
        Object obj = ScarabCache.get(this, GET_R_QUERY_USER, user); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(RQueryUserPeer.QUERY_ID, getQueryId());
            crit.add(RQueryUserPeer.USER_ID, user.getUserId());
            List rqus = RQueryUserPeer.doSelect(crit);
            if (!rqus.isEmpty())
            {
                result = (RQueryUser)rqus.get(0);
            }
            else
            {
                result = new RQueryUser();
                result.setQuery(this);
                result.setUserId(user.getUserId());
            }
            ScarabCache.put(result, this, GET_R_QUERY_USER, user);
        }
        else 
        {
            result = (RQueryUser)obj;
        }
        return result;
    }

    /**
     * Checks permission and approves or rejects query. If query
     * is approved, query type set to "module", else set to "personal".
     */
    public void approve(ScarabUser user, boolean approved)
         throws Exception
    {                
        Module module = getModule();

        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
            if (!approved)
            {
                setScopeId(Scope.PERSONAL__PK);
            }
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }


    /**
     * Checks if user has permission to delete query.
     * Only the creating user can delete a personal query.
     * Only project owner or admin can delete a project-wide query.
     */
    public void delete(ScarabUser user)
         throws Exception
    {                
        Module module = getModule();
        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module)
          || (user.getUserId().equals(getUserId()) 
             && getScopeId().equals(Scope.PERSONAL__PK)))
        {
            // Delete user-query maps.
            List rqus = getRQueryUsers();
            for (int i=0; i<rqus.size(); i++)
            {
                RQueryUser rqu = (RQueryUser)rqus.get(i);
                rqu.delete(user);
            }
            setDeleted(true);
            save();
            
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

    /**
     * Checks if user has permission to delete query.
     * Only the creating user can delete a personal query.
     * Only project owner or admin can delete a project-wide query.
     */
    public void copyQuery(ScarabUser user)
         throws Exception
    {                
         Query newQuery = new Query();
         newQuery.setName(getName() + " (copy)");
         newQuery.setDescription(getDescription());
         newQuery.setValue(getValue());
         newQuery.setModuleId(getModuleId());
         newQuery.setIssueTypeId(getIssueTypeId());
         newQuery.setListId(getListId());
         newQuery.setApproved(getApproved());
         newQuery.setCreatedDate(new Date());
         newQuery.setUserId(user.getUserId());
         newQuery.setScopeId(getScopeId());
         newQuery.save();

         RQueryUser rqu = getRQueryUser(user);
         if (rqu != null)
         {
             RQueryUser rquNew = new RQueryUser();
             rquNew.setQueryId(newQuery.getQueryId());
             rquNew.setUserId(user.getUserId());
             rquNew.setSubscriptionFrequency(rqu.getSubscriptionFrequency());
             rquNew.setIsdefault(rqu.getIsdefault());
             rquNew.setIsSubscribed(rqu.getIsSubscribed());
             rquNew.save();
         }
    }
}
