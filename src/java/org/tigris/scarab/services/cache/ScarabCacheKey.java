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

    public class ScarabCacheKey
    {
        private static final Category log = 
            Category.getInstance("org.apache.torque");

        private Object obj1;
        private Object obj2;
        private Object obj3;
        private Object obj4;
        private Object obj5;

        public ScarabCacheKey(Object o1, Object o2, 
                              Object o3, Object o4, Object o5)
        {
            obj1 = o1;
            obj2 = o2;
            obj3 = o3;
            obj4 = o4;
            obj5 = o5;
        }

        public boolean equals(Object obj)
        {
            boolean equal = false;
            if ( obj instanceof ScarabCacheKey ) 
            {
                ScarabCacheKey sck = (ScarabCacheKey)obj;
                equal = ObjectUtils.equals(sck.obj1, obj1);
                equal &= ObjectUtils.equals(sck.obj2, obj2);
                equal &= ObjectUtils.equals(sck.obj3, obj3);
                equal &= ObjectUtils.equals(sck.obj4, obj4);
                equal &= ObjectUtils.equals(sck.obj5, obj5);
            }
            if (equal) 
            {
                log.debug("Saved db hit on " + obj1 + "::" + obj2 + ". YAY!");
            }
            
            return equal;
        }

        public int hashCode()
        {
            int h = 0;
            if (obj1 != null) 
            {
                h += obj1.hashCode();
            }
            if (obj2 != null) 
            {
                h += obj2.hashCode();
            }
            if (obj3 != null) 
            {
                h += obj3.hashCode();
            }
            if (obj4 != null) 
            {
                h += obj4.hashCode();
            }
            if (obj5 != null) 
            {
                h += obj5.hashCode();
            }            
            return h;
        }
    }
