package org.tigris.scarab.services.cache;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import org.apache.commons.util.ObjectUtils;
import org.apache.log4j.Category;
import org.apache.fulcrum.pool.RecyclableSupport;

public class ScarabCacheKey
    extends RecyclableSupport
{
    private static final Category log = 
        Category.getInstance("org.apache.torque");

    int n;
    private Object obj1;
    private Object obj2;
    private Object obj3;
    private Object obj4;
    private Object obj5;
    private Object obj6;
    private Object obj7;

    public ScarabCacheKey()
    {
    }
    
    public ScarabCacheKey(int numArgs, Object o1, Object o2, Object o3, 
                          Object o4, Object o5, Object o6, Object o7)
    {
        init(numArgs, o1, o2, o3, o4, o5, o6, o7);
    }
    
    /**
     * Describe <code>init</code> method here.
     *
     * @param numArgs, 0-5
     * @param o1 the Object on which the method is invoked.  if the method is
     * is static, a String representing the class name is used.
     * @param o2 the method name
     * @param o3 first method arg, may be null
     * @param o4 2nd method arg, may be null
     * @param o5 3rd method arg, may be null
     * @param o6 4th method arg, may be null
     * @param o7 5th method arg, may be null
     */
    public void init(int numArgs, Object o1, Object o2, Object o3, Object o4, 
                     Object o5, Object o6, Object o7)
    {
        n = numArgs;
        obj1 = o1;
        obj2 = o2;
        obj3 = o3;
        obj4 = o4;
        obj5 = o5;
        obj6 = o6;
        obj7 = o7;
    }

    public boolean equals(Object obj)
    {
        boolean equal = false;
        if ( obj instanceof ScarabCacheKey ) 
        {
            ScarabCacheKey sck = (ScarabCacheKey)obj;
            equal = ObjectUtils.equals(sck.obj1, obj1);
            equal &= ObjectUtils.equals(sck.obj2, obj2);
            if (n > 0) 
            {
                equal &= ObjectUtils.equals(sck.obj3, obj3);
                if (n > 1) 
                {
                    equal &= ObjectUtils.equals(sck.obj4, obj4);
                    if (n > 2) 
                    {
                        equal &= ObjectUtils.equals(sck.obj5, obj5);
                        if (n > 3) 
                        {
                            equal &= ObjectUtils.equals(sck.obj6, obj6);
                            if (n > 4) 
                            {
                                equal &= ObjectUtils.equals(sck.obj7, obj7);
                            }
                        }
                    }
                }
            }
            
        }
            if (equal) 
            {
                log.debug("Saved db hit on " + obj1 + "::" + obj2 + ". YAY!");
            }
            
            return equal;
        }

        public int hashCode()
        {
            int h = obj1.hashCode();
            h += obj2.hashCode();
            if (n > 0 && obj3 != null) 
            {
                h += obj3.hashCode();
            }
            if (n > 1 && obj4 != null) 
            {
                h += obj4.hashCode();
            }
            if (n > 2 && obj5 != null) 
            {
                h += obj5.hashCode();
            }            
            if (n > 3 && obj6 != null) 
            {
                h += obj6.hashCode();
            }            
            if (n > 4 && obj7 != null) 
            {
                h += obj7.hashCode();
            }            
            return h;
        }

    // ****************** Recyclable implementation ************************

    /**
     * Disposes the object after use. The method is called when the
     * object is returned to its pool.  The dispose method must call
     * its super.
     */
    public void dispose()
    {
        super.dispose();
        obj1 = null;
        obj2 = null;
        obj3 = null;
        obj4 = null;
        obj5 = null;
        obj6 = null;
        obj7 = null;
    }
}
