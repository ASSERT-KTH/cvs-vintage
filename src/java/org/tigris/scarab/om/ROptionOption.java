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


// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.TurbineServices;

import org.tigris.scarab.util.ScarabException;

/** 
  * This class represents the SCARAB_R_OPTION_OPTION table.
  *
  * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
  * @version $Id: ROptionOption.java,v 1.12 2003/03/25 16:57:53 jmcnally Exp $
  */
public class ROptionOption 
    extends org.tigris.scarab.om.BaseROptionOption
    implements Persistent
{
    private int level;

    /** the name of this class */
    private static final String className = "ROptionOption";

    /**
     * Must call getInstance()
     */
    protected ROptionOption()
    {
    }

    /**
     * Creates a key for use in caching AttributeOptions
     */
    static String getCacheKey(Integer option1, Integer option2)
    {
         String keyStringA = option1.toString();
         String keyStringB = option2.toString();
         return new StringBuffer(className.length() + keyStringA.length() + keyStringB.length())
             .append(className).append(keyStringA).append(keyStringB).toString();
    }

    /**
     * Gets an instance of a new ROptionOption
     */
    public static ROptionOption getInstance()
    {
        return new ROptionOption();
    }


    /**
     * Gets an instance of a new ROptionOption
     */
    public static ROptionOption getInstance(Integer parent, Integer child)
        throws Exception
    {
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(parent, child);
        ROptionOption option = null;
        try
        {
            option = (ROptionOption)tgcs.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            try
            {
                Criteria crit = new Criteria();
                crit.add (ROptionOptionPeer.OPTION1_ID, parent);
                crit.add (ROptionOptionPeer.OPTION2_ID, child);
                option = (ROptionOption) (ROptionOptionPeer.doSelect(crit)).get(0);
            }
            catch (Exception e)
            {
                throw new ScarabException("ROptionOption with ID " + 
                                            parent.toString() + ":" + 
                                            child.toString() + 
                                          " can not be found");
            }
            tgcs.addObject(key, new CachedObject(option));
        }
        return option;
    }

    /**
     * This will also remove the ROptionOption from the internal cache
     * as well as from the database.
     */
    public static void doRemove(ROptionOption roo)
        throws Exception
    {
        // using Criteria because there is a bug in Torque
        // where doDelete(roo) doesn't work because it has
        // multple primary keys
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, roo.getOption1Id());
        crit.add (ROptionOptionPeer.OPTION2_ID, roo.getOption2Id());

        ROptionOptionPeer.doDelete(crit);

        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(roo.getOption1Id(), roo.getOption2Id());
        tgcs.removeObject(key);
    }

    /**
     * This will also remove the ROptionOption from the internal cache
     * as well as from the database.
     */
    public static void doRemove(Integer parent, Integer child)
        throws Exception
    {
        ROptionOption roo = getInstance();
        roo.setOption1Id(parent);
        roo.setOption2Id(child);
        ROptionOption.doRemove(roo);
    }

    /**
     * Gets the AttributeOption assigned to the Option1Id
     */
    public AttributeOption getOption1Option()
        throws TorqueException
    {
        return AttributeOptionManager.getInstance(getOption1Id());
    }

    /**
     * Gets the AttributeOption assigned to the Option2Id
     */
    public AttributeOption getOption2Option()
        throws TorqueException
    {
        return AttributeOptionManager.getInstance(getOption2Id());
    }

    /**
     * Get the level in the option parent-child tree.
     * Note: Not currently used.
     * @return value of level.
     */
    public int getLevel() 
    {
        return level;
    }
    
    /**
     * Get the level in the option parent-child tree.
     * Note: Not currently used.
     * @param v  Value to assign to level.
     */
    public void setLevel(int  v) 
    {
        this.level = v;
    }

    /**
     * Will return true if this ROptionOption is the parent
     * of the given AttributeOption. In other words, the value of
     * this.OPTION2_ID == option.getOptionId()
    public boolean isParentOf(AttributeOption option)
    {
        return option.getOptionId().equals(this.getOption2Id());
    }
     */

    /**
     * Will return true if this ROptionOption is the child
     * of the given AttributeOption. In other words, the value of
     * this.OPTION1_ID == option.getOptionId()
    public boolean isChildOf(AttributeOption option)
    {
        return option.getOptionId().equals(this.getOption1Id());
    }
     */

    /**
     * A String representation of this object.
     */
    public String toString()
    {
        return "Parent: " + getOption1Id() + " Child: " + 
                getOption2Id() + " : Order: " + getPreferredOrder();
    }
}



