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
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.manager.CacheListener;
import org.tigris.scarab.util.Log;

/** 
 * This class manages Module objects.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ModuleManager.java,v 1.26 2003/03/27 23:57:19 jon Exp $
 */
public class ModuleManager
    extends BaseModuleManager
    implements CacheListener
{
    /**
     * Creates a new <code>ModuleManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ModuleManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    protected Module getInstanceImpl()
    {
        return new ScarabModule();
    }

    /**
     * Get an instance of a Module by realName and code. If the result
     * != 1, then throw a TorqueException.
     *
     * FIXME: Use caching? John?
     */
    public static Module getInstance(String moduleDomain, 
                                     String moduleRealName, 
                                     String moduleCode)
        throws TorqueException
    {
        return getManager().getInstanceImpl(moduleDomain, moduleRealName, 
                                            moduleCode);
    }

    /**
     * Get an instance of a Module by realName and code. If the result
     * != 1, then throw a TorqueException.
     *
     * FIXME: Use caching? John?
     */
    protected Module getInstanceImpl(String moduleDomain, 
                                     String moduleRealName, 
                                     String moduleCode)
        throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(ScarabModulePeer.MODULE_NAME, moduleDomain);
        crit.add(ScarabModulePeer.MODULE_NAME, moduleRealName);
        crit.add(ScarabModulePeer.MODULE_CODE, moduleCode);
        List result = ScarabModulePeer.doSelect(crit);
        if (result.size() != 1)
        {
            throw new TorqueException ("Selected: " + result.size() + 
                " rows. Expected 1.");
        }
        return (Module) result.get(0);
    }

    /**
     * Create a list of Modules from the given list of issues.  Each
     * Module in the list of issues will only occur once in the list of 
     * Modules.
     *
     * @param issues a <code>List</code> value
     * @return a <code>List</code> value
     * @exception TorqueException if an error occurs
     */
    public static List getInstancesFromIssueList(List issues)
        throws TorqueException
    {
        if (issues == null) 
        {
            throw new IllegalArgumentException("Null issue list is not allowed.");
        }        

        List modules = new ArrayList();
        Iterator i = issues.iterator();
        if (i.hasNext()) 
        {
            Issue issue = (Issue)i.next();
            if (issue != null)
            {
                Module module = issue.getModule();
                if (module != null && !modules.contains(module)) 
                {
                    modules.add(module);
                }
            }
            else
            {
                throw new TorqueException("Null issue in list is not allowed.");
            }
        }
        return modules;
    }


    /**
     * Notify other managers with relevant CacheEvents.
     */
    protected void registerAsListener()
    {
        RModuleIssueTypeManager.addCacheListener(this);
        RModuleAttributeManager.addCacheListener(this);
        AttributeGroupManager.addCacheListener(this);
        RModuleOptionManager.addCacheListener(this);
        AttributeManager.addCacheListener(this);
        AttributeOptionManager.addCacheListener(this);
        IssueTypeManager.addCacheListener(this);
    }

    // -------------------------------------------------------------------
    // CacheListener implementation

    public void addedObject(Persistent om)
    {
        if (om instanceof RModuleAttribute)
        {
            RModuleAttribute castom = (RModuleAttribute)om;
            Integer key = castom.getModuleId();
            try
            {
                Serializable obj = getInstance(key);
                if (obj != null) 
                {
                    getMethodResult().removeAll(obj, 
                        AbstractScarabModule.GET_R_MODULE_ATTRIBUTES);
                }
            }
            catch(TorqueException e)
            {
                Log.get().warn("Invalid Module id ", e);
            }
        }
        else if (om instanceof RModuleOption)
        {
            RModuleOption castom = (RModuleOption)om;
            Integer key = castom.getModuleId();
            try
            {
                Serializable obj = getInstance(key);
                if (obj != null) 
                {
                    getMethodResult().removeAll(obj, 
                        AbstractScarabModule.GET_LEAF_R_MODULE_OPTIONS);
                }
            }
            catch(TorqueException e)
            {
                Log.get().warn("Invalid Module id ", e);
            }
        }
        else if (om instanceof RModuleIssueType) 
        {
            RModuleIssueType castom = (RModuleIssueType)om;
            Integer key = castom.getModuleId();
            try
            {
                Serializable obj = getInstance(key);
                if (obj != null) 
                {
                    getMethodResult().remove(obj, 
                        AbstractScarabModule.GET_NAV_ISSUE_TYPES);
                }
            }
            catch(TorqueException e)
            {
                Log.get().warn("Invalid Module id ", e);
            }
        }
        else if (om instanceof IssueType) 
        {
            getMethodResult().clear();
        }
        else if (om instanceof Attribute) 
        {
            getMethodResult().clear();
        }
        else if (om instanceof AttributeOption) 
        {
            getMethodResult().clear();
        }
    }

    public void refreshedObject(Persistent om)
    {
        addedObject(om);
    }

    /** fields which interest us with respect to cache events */
    public List getInterestedFields()
    {
        List interestedCacheFields = new LinkedList();
        interestedCacheFields.add(RModuleOptionPeer.MODULE_ID);
        interestedCacheFields.add(RModuleAttributePeer.MODULE_ID);
        interestedCacheFields.add(RModuleIssueTypePeer.MODULE_ID);
        interestedCacheFields.add(AttributeGroupPeer.MODULE_ID);
        interestedCacheFields.add(AttributePeer.ATTRIBUTE_ID);
        interestedCacheFields.add(AttributeOptionPeer.OPTION_ID);
        interestedCacheFields.add(IssueTypePeer.ISSUE_TYPE_ID);
        return interestedCacheFields;
    }
}
