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
// Java classes
import java.util.List;

// Turbine classes
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

// Scarab classes
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * This class represents an AttributeType
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: AttributeType.java,v 1.14 2003/02/04 11:26:00 jon Exp $
 */
public class AttributeType 
    extends BaseAttributeType
    implements Persistent
{
    // the following Strings are method names that are used in caching results
    private static final String ATTRIBUTETYPE = 
        "AttributeType";
    private static final String GET_INSTANCE = 
        "getInstance";
    private static final String GET_ATTRIBUTE_CLASS = "getAttributeClass";
    
    /* This interpretation of attribute type stretches its original 
       meaning so I will try to avoid it. - jdm
    protected boolean onePerModule;
    
    /**
     * Get the value of onePerModule.
     * @return value of onePerModule.
     * /
    public boolean isOnePerModule() 
    {
        return onePerModule;
    }
    
    /**
     * Set the value of onePerModule.
     * @param v  Value to assign to onePerModule.
     * /
    public void setOnePerModule(boolean  v) 
    {
        this.onePerModule = v;
    }
     */

    /*

    private static final String  = 
        "";
        List result = null;
        Object obj = ScarabCache.get(this, , 
                                    ); 
        if (obj == null) 
        {        

            ScarabCache.put(result, this, ,);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    */

    /**
     * Override the base class to provide caching of AttributeClass objects
     * and save a hit to the database.
     */
    public AttributeClass getAttributeClass()
        throws TorqueException
    {
        AttributeClass result = null;
        Object obj = ScarabCache.get(this, GET_ATTRIBUTE_CLASS);
        if (obj == null) 
        {
            result = super.getAttributeClass();
            ScarabCache.put(result, this, GET_ATTRIBUTE_CLASS);
        }
        else
        {
            result = (AttributeClass)obj;
        }
        return result;
    }
    
    public static AttributeType getInstance(String attributeTypeName) 
        throws Exception
    {
        AttributeType result = null;
        Object obj = ScarabCache.get(ATTRIBUTETYPE, GET_INSTANCE, 
                                     attributeTypeName); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(AttributeTypePeer.ATTRIBUTE_TYPE_NAME, attributeTypeName);
            List attributeTypes = AttributeTypePeer.doSelect(crit);
            if(attributeTypes.size() > 1)
            {
                throw new ScarabException("duplicate attribute type name found");
            }
            result = (AttributeType)attributeTypes.get(0);
            ScarabCache.put(result, ATTRIBUTETYPE, GET_INSTANCE, 
                            attributeTypeName);
        }
        else 
        {
            result = (AttributeType)obj;
        }
        return result;
    }
}



