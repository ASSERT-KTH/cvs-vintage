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

/**
 * A Testing Suite for the om.Query class.
 *
 * @author <a href="mailto:mumbly@oneofus.org">Tim McNerney</a>
 * @version $Id: AttributeValueTest.java,v 1.12 2004/02/01 14:08:38 dep4b Exp $
 */
public class AttributeValueTest extends BaseTestCase
{
    private AttributeValue attValSeverity = null;
    private AttributeValue attValDescription = null;
    private AttributeValue attValSeverityCopy = null;
    private Issue issue = null;

   
    public void setUp() throws Exception
    {
    	super.setUp();
        issue = getIssue0();
        // severity
        attValSeverity = issue.getAttributeValue(AttributeManager.getInstance(new NumberKey("9")));
        // description
        attValDescription = issue.getAttributeValue(AttributeManager.getInstance(new NumberKey("1")));
        attValSeverityCopy = attValSeverity.copy();
    }
    


    public void testSaveAndDelete() throws Exception
    {
        System.out.println("\ntestSave()");
        Attachment attachment = AttachmentManager.getInstance();
        attachment.setName("activitySet test");
        attachment.setData("Test comment");
        attachment.setTextFields(getUser1(), issue, Attachment.COMMENT__PK);
        attachment.save();
        ActivitySet trans = 
        	ActivitySetManager.getInstance(new Integer(1), getUser1(), attachment);
        trans.save();
        attValSeverityCopy.startActivitySet(trans);
        attValSeverityCopy.setOptionId(new Integer(70));
        attValSeverityCopy.setUserId(new Integer(1));
        attValSeverityCopy.save();
        assertEquals(attValSeverityCopy.getValueId().toString(), attValSeverityCopy.getQueryKey());
        
        AttributeValuePeer.doDelete(attValSeverityCopy);
    }

    public void testGetOptionIdAsString() throws Exception
    {
        System.out.println("\ntestGetOptionIdAsString()");
        //assertEquals("70", attValSeverityCopy.getOptionIdAsString());
        assertEquals("66", attValSeverityCopy.getOptionIdAsString());
    }


    public void testIsRequired() throws Exception
    {
        System.out.println("\ntestIsRequired()");
        assertEquals(false, attValSeverityCopy.isRequired());
    }

    public void testIsSet() throws Exception
    {
        System.out.println("\ntestIsSet()");
        assertEquals(true, attValSeverityCopy.isSet());
    }

    public void testIsSet2() throws Exception
    {
        System.out.println("\ntestIsSet2()");
        attValDescription.setValue("description");
        assertEquals(true, attValDescription.isSet());
    }

    public void testGetRModuleAttribute() throws Exception
    {
        System.out.println("\ntestGetRModuleAttribute()");
        assertEquals(attValSeverity.getAttributeId(), attValSeverityCopy.getRModuleAttribute().getAttributeId());
    }

    public void testGetAttributeOption() throws Exception
    {
        System.out.println("\ntestGetAttributeOption()");
        System.out.println("get att opt = " + attValSeverityCopy.getAttributeOption());
    }
}
