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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;

/**
 * A Testing Suite for the om.Attribute class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: AttributeTest.java,v 1.3 2002/03/14 01:13:14 jmcnally Exp $
 */
public class AttributeTest extends BaseTestCase
{
    /**
     * Creates a new instance.
     *
     */
    public AttributeTest()
    {
        super("AttributeTest");
    }

    public static junit.framework.Test suite()
    {
        return new AttributeTest();
    }

    protected void runTest()
        throws Throwable
    {
//        createROptionOptionMapping();
//        testSortAttributeOptions();
    }
/*
    private void createROptionOptionMapping()
        throws Exception
    {
        Attribute.createROptionOptionMapping();
    }
*/    
    private void testSortAttributeOptions()
        throws Exception
    {
        Attribute attribute = 
            AttributeManager.getInstance(new NumberKey(6));
        List before = (List) attribute.getAttributeOptions();
        List after = (List) attribute.getAttributeOptions();
        System.out.println ("Options Size Before: " + before.size());
        System.out.println ("Options Size After: " + after.size());
//        attribute.sortOptions(new ArrayList(after));

        for (int i=0; i<after.size(); i++)
        {
//            System.out.println (((AttributeOption)after.get(i)).getName());
//            System.out.println (
//                "Before: '" + ((AttributeOption)before.get(i)).getName() + 
//                "' - After: '" + ((AttributeOption)after.get(i)).getName() + "'");
        }
    }
}
