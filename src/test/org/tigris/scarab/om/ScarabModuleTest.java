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
import java.util.Iterator;

import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;

import org.tigris.scarab.test.BaseTestCase;

/**
 * A Testing Suite for the om.ScarabModule class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: ScarabModuleTest.java,v 1.4 2002/01/18 22:26:17 jon Exp $
 */
public class ScarabModuleTest extends BaseTestCase
{
    /**
     * Creates a new instance.
     *
     */
    public ScarabModuleTest()
    {
        super("ScarabModuleTest");
    }

    public static junit.framework.Test suite()
    {
        return new ScarabModuleTest();
    }

    protected void runTest()
        throws Throwable
    {
//        testGetParents();
        testCreateNew();
    }
    
    private void testGetParents() throws Exception
    {
        log("testGetParents()");
        ModuleEntity module = (ModuleEntity) ScarabModulePeer.retrieveByPK(new NumberKey(7));
        List parents = module.getAncestors();
        Iterator itr = parents.iterator();
        while (itr.hasNext())
        {
            ModuleEntity me = (ModuleEntity) itr.next();
            System.out.println (me.getName());
        }
//        assertEquals (map.size(), 10);  
    }

    private void testCreateNew() throws Exception
    {
        log("testCreateNew()");
        ModuleEntity me = ModuleManager.getInstance();
        me.setRealName("New Module");
        me.setOwnerId(new NumberKey(1));
        me.setParentId(new NumberKey(1));
//        me.setCode("NEWMOD");
        me.setDescription("This is the new module description");
        me.save();
    }
}
