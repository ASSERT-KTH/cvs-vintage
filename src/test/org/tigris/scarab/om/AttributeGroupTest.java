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

import org.apache.torque.om.NumberKey;
import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.services.cache.ScarabCache;


/**
 * A Testing Suite for the om.Query class.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeGroupTest.java,v 1.5 2003/01/28 18:44:41 jmcnally Exp $
 */
public class AttributeGroupTest extends BaseTestCase
{
    private AttributeGroup group = null;
    private Attribute severity = null;
    private Attribute desc = null;

    /**
     * Creates a new instance.
     *
     */
    public AttributeGroupTest()
    {
        super("AttributeGroupTest");
    }

    public static junit.framework.Test suite()
    {
        return new AttributeGroupTest();
    }

    protected void runTest()
            throws Throwable
    {
        severity = AttributeManager.getInstance(new NumberKey("9"));
        desc = AttributeManager.getInstance(new NumberKey("1"));
        group = AttributeGroupManager.getInstance(new NumberKey("131"));

        testGetAttributes();
        testDeleteAttribute();
        testAddAttribute();
        testGetRAttributeAttributeGroup();
        testDelete();
    }

    private void testDeleteAttribute() throws Exception
    {
        System.out.println("\ntestDeleteAttribute()");
        group.deleteAttribute(severity, getUser1(), getModule());
        group.deleteAttribute(desc, getUser1(), getModule());
        assertEquals(5, group.getAttributes().size());
    }

    private void testAddAttribute() throws Exception
    {
        System.out.println("\ntestAddAttribute()");
        group.addAttribute(severity);
        assertEquals(6, group.getAttributes().size());
        group.addAttribute(desc);
        assertEquals(7, group.getAttributes().size());
    }

    private void testGetAttributes() throws Exception
    {
        System.out.println("\ntestGetAttributes()");
        assertEquals(7, group.getAttributes().size());
    }

    private void testGetRAttributeAttributeGroup() throws Exception
    {
        System.out.println("\ntestGetRAttributeAttributeGroup()");
        assertEquals("9", group.getRAttributeAttributeGroup(severity).getAttributeId().toString());
    }

    private void testDelete() throws Exception
    {
        System.out.println("\ntestDelete()");
        group.delete(getUser1(), getModule());
        ScarabCache.clear();
        assertEquals(1, getModule().getAttributeGroups(getDefaultIssueType()).size());
        getModule().addRModuleAttribute(getDefaultIssueType(), severity);
        getModule().addRModuleAttribute(getDefaultIssueType(), desc);
    }
}
