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
import java.util.HashMap;
import java.io.Serializable;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.manager.CacheListener;

import org.tigris.scarab.util.Log;

/** 
 * This class manages AttributeGroup objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AttributeGroupManager.java,v 1.7 2003/03/20 00:57:31 jon Exp $
 */
public class AttributeGroupManager
    extends BaseAttributeGroupManager
    implements CacheListener
{
    /**
     * Creates a new <code>AttributeGroupManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public AttributeGroupManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
        validFields = new HashMap();
        validFields.put(AttributeGroupPeer.MODULE_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap.get(AttributeGroupPeer.MODULE_ID);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }

    protected AttributeGroup getInstanceImpl()
    {
        return new AttributeGroup();
    }

    /**
     * Notify other managers with relevant CacheEvents.
     */
    protected void registerAsListener()
    {
        RAttributeAttributeGroupManager.addCacheListener(this);
        AttributeManager.addCacheListener(this);
    }

    // -------------------------------------------------------------------
    // CacheListener implementation

    public void addedObject(Persistent om)
    {
        if (om instanceof RAttributeAttributeGroup)
        {
            RAttributeAttributeGroup castom = (RAttributeAttributeGroup)om;
            ObjectKey key = castom.getGroupId();            
            try
            {
                Serializable obj = getInstance(key);
                if (obj != null) 
                {
                    getMethodResult().remove(obj, AttributeGroup.GET_ATTRIBUTES);
                }
            }
            catch(TorqueException e)
            {
                Log.get().warn("Invalid AttributeGroup id ", e);
            }
        }
        else if (om instanceof Attribute) 
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
        interestedCacheFields.add(RAttributeAttributeGroupPeer.GROUP_ID);
        interestedCacheFields.add(AttributePeer.ATTRIBUTE_ID);
        interestedCacheFields.add(AttributeGroupPeer.ATTRIBUTE_GROUP_ID);
        return interestedCacheFields;
    }
}
