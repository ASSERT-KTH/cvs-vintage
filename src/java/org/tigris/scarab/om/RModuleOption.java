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

// JDK classes
import java.util.Comparator;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.turbine.Log;

import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Fri Apr 13 13:24:10 PDT 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class RModuleOption 
    extends BaseRModuleOption
    implements Persistent
{

    private int level;

    private static final Comparator comparator = new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                int result = 1;
                RModuleOption opt1 = (RModuleOption)obj1; 
                RModuleOption opt2 = (RModuleOption)obj2;
                if (opt1.getOrder() < opt2.getOrder()) 
                {
                    result = -1;
                }
                else if (opt1.getOrder() == opt2.getOrder()) 
                {
                    result = opt1.getDisplayValue()
                        .compareTo(opt2.getDisplayValue()); 
                }
                return result;
            }            
        };


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
        NumberKey id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects.");
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
        ObjectKey id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }

    /**
     * Compares numeric value and in cases where the numeric value
     * is the same it compares the display values.
     */
    public static Comparator getComparator()
    {
        return comparator;
    }


    /**
     * A convenience method for getting the option name.  It is 
     * preferred over using getAttributeOption().getName() as it
     * leaves open the possibility of per module display values.
     */
    public String getDisplayValue()
    {
        String dispVal = super.getDisplayValue();
        if ( dispVal == null ) 
        {
            try
            {
                dispVal = getAttributeOption().getName();
            }
            catch (Exception e)
            {
                Log.error(e);
                dispVal = "!Error-Check Logs!";
            }
        }
        return dispVal;
        
        // return getAttributeOption().getName();
    }
    
    /**
     * Get the level in the option parent-child tree.
     * @return value of level.
     */
    public int getLevel() 
    {
        return level;
    }
    
    /**
     * Get the level in the option parent-child tree.
     * @param v  Value to assign to level.
     */
    public void setLevel(int  v) 
    {
        this.level = v;
    }

    
    public RModuleAttribute getRModuleAttribute(IssueType issueType)
        throws Exception
    {
        Module module = ModuleManager.getInstance(getModuleId());
        Attribute attribute = getAttributeOption().getAttribute();
        return module.getRModuleAttribute(attribute, issueType);
    }

    public void delete( ScarabUser user )
         throws Exception
    {                
        Module module = getModule();

        if (user.hasPermission(ScarabSecurity.MODULE__EDIT, module))
        {
            Criteria c = new Criteria()
                .add(RModuleOptionPeer.MODULE_ID, getModuleId())
                .add(RModuleOptionPeer.ISSUE_TYPE_ID, getIssueTypeId())
                .add(RModuleOptionPeer.OPTION_ID, getOptionId());
            RModuleOptionPeer.doDelete(c);
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }
}
