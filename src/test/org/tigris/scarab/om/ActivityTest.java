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

import junit.framework.*;

/**
 * A Testing Suite for the om.Activity class.
 *
 * @author <a href="mailto:mumbly@oneofus.org">Tim McNerney</a>
 * @version $Id: ActivityTest.java,v 1.9 2003/03/25 20:31:54 jmcnally Exp $
 */
public class ActivityTest extends BaseTestCase
{
    public ActivityTest(String name)
    {
        super(name);
    }

    /**
     * Creates a new instance.
     *
     */
    public ActivityTest()
    {
        super("ActivityTest");
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new ActivityTest("testCreateLong"));
        suite.addTest(new ActivityTest("testCreateShort"));
        suite.addTest(new ActivityTest("testGetAttribute"));
        return suite;
    }

    public void testCreateLong() throws Exception
    {
        System.out.println("\ntestCreateLong()");
        ActivitySet trans = getEditActivitySet();
        Activity activity = ActivityManager
            .createNumericActivity(getIssue0(), getPlatformAttribute(),
                                   trans,"new activity long create", null, 
                                   new Integer(5), new Integer(6));
        activity.save();
        ActivitySet newtrans = activity.getActivitySet();
        assertEquals("getActivitySet expected: " + trans.getActivitySetId() +
        " got: " + newtrans.getActivitySetId(),
        trans.getActivitySetId().toString(), newtrans.getActivitySetId().toString());
        Activity retActivity = ActivityManager
            .getInstance(activity.getActivityId(), false);
        assertEquals("OldValue", activity.getOldValue(), retActivity.getOldValue());
        assertEquals("NewValue", activity.getNewValue(), retActivity.getNewValue());
        assertEquals("Attribute", activity.getAttribute(), retActivity.getAttribute());
    }

    public void testCreateShort() throws Exception
    {
        System.out.println("\ntestCreateShort()");
        ActivitySet trans = getEditActivitySet();

        Activity activity = ActivityManager
            .createTextActivity(getIssue0(), getPlatformAttribute(),
            trans,"new activity long create",null,
            "oldValue", "newValue");
        activity.save();

        ActivitySet newtrans = activity.getActivitySet();
        assertEquals("getActivitySet expected: " + trans.getActivitySetId() +
        " got: " + newtrans.getActivitySetId(),
        trans.getActivitySetId().toString(), newtrans.getActivitySetId().toString());
        Activity retActivity = ActivityManager
            .getInstance(activity.getActivityId(), false);
        assertEquals("OldValue", activity.getOldValue(), retActivity.getOldValue());
        assertEquals("NewValue", activity.getNewValue(), retActivity.getNewValue());
        assertEquals("Attribute", activity.getAttribute(), retActivity.getAttribute());
    }

    public void testGetAttribute() throws Exception
    {
        System.out.println("\ntestGetAttribute()");
        Activity retActivity = ActivityManager
            .getInstance(new NumberKey(1), false);
        Integer key = retActivity.getAttribute().getAttributeId();
        assertTrue("AttId expected: 11 got: " + key, 
                   key.intValue() == 11);
    }
}
