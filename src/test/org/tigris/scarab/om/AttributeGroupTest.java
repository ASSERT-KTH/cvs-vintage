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
import org.tigris.scarab.test.BaseScarabOMTestCase;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * A Testing Suite for the om.Query class.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeGroupTest.java,v 1.12 2004/04/07 20:12:22 dep4b Exp $
 */
public class AttributeGroupTest extends BaseScarabOMTestCase
{
    private AttributeGroup group = null;
    private Attribute severity = null;
    private Attribute desc = null;

    public void setUp() throws Exception
    {
        super.setUp();
        severity = AttributeManager.getInstance(new NumberKey("9"));
        desc = AttributeManager.getInstance(new NumberKey("1"));
        group = AttributeGroupManager.getInstance(new NumberKey("131"));

    }

    /**
     * I think we are screwing up other tests..
     * @throws Exception
     */
    public void testDeleteAddAttribute() throws Exception
    {

        Attribute test = severity.copyAttribute(getUser1());
        test.save();
        assertFalse(test.getAttributeId().equals(severity.getAttributeId()));
        int numberOfAttributes = group.getAttributes().size();
        group.addAttribute(test);
        assertEquals(numberOfAttributes + 1, group.getAttributes().size());
        try
        {
            group.deleteAttribute(test, getUser1(), getModule());
            fail("User 1 doesn't have permissions to delete attributes.");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof ScarabException);
        }
        group.deleteAttribute(test, getUser2(), getModule());
        assertEquals(numberOfAttributes, group.getAttributes().size());
    }

    public void testGetAttributes() throws Exception
    {
        System.out.println("\ntestGetAttributes()");
        assertTrue(group.getAttributes().size() > 0);
    }

    public void testGetRAttributeAttributeGroup() throws Exception
    {

        assertEquals(
            new Integer(9),
            group.getRAttributeAttributeGroup(severity).getAttributeId());
    }

    /**
     * Can't see to get to work.  I can delete the group, but that messes up other
     * units tests!  I can't seem to create and then delete a new group.  I get this error:
     * "Error accessing dedupe sequence for issue type '{org.tigris.scarab.om.IssueType@1f: name=Defect}'"
     * @throws Exception
     */
    public void testDelete() throws Exception
    {
        System.out.println("\ntestDelete()");
        AttributeGroup newGroup = group.copyGroup();
        newGroup.setIssueType(group.getIssueType());
        newGroup.save();
        assertFalse(
            newGroup.getAttributeGroupId().equals(group.getAttributeGroupId()));

        newGroup.delete();
        ScarabCache.clear();
        assertFalse(AttributeGroupManager.exists(newGroup));

        group = AttributeGroupManager.getInstance(new NumberKey("131"));
        assertNotNull(group);
        assertTrue(AttributeGroupManager.exists(group));

    }
}
