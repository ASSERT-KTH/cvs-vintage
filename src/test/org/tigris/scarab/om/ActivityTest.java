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
import org.tigris.scarab.util.ScarabException;

import java.util.Iterator;
import java.util.List;

import junit.framework.*;

/**
 * A Testing Suite for the om.Activity class.
 *
 * @author <a href="mailto:mumbly@oneofus.org">Tim McNerney</a>
 * @version $Id: ActivityTest.java,v 1.2 2002/01/18 22:26:17 jon Exp $
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
        Transaction trans = getEditTransaction();
        Activity activity = new Activity();
        activity.create(getIssue0(), getPlatformAttribute(),
          "new activity long create", trans, 0, 0, null, null,
          new NumberKey(5), new NumberKey(6), "before", "after");
        trans = activity.getTransaction();
        Transaction newtrans = activity.getTransaction();
        assertEquals("getTransaction expected: " + trans.getTransactionId() +
        " got: " + newtrans.getTransactionId(),
        trans.getTransactionId().toString(), newtrans.getTransactionId().toString());
        Activity retActivity = (Activity)ActivityPeer.retrieveByPK(activity.getActivityId());
        assertEquals("OldValue", activity.getOldValue(), retActivity.getOldValue());
        assertEquals("NewValue", activity.getNewValue(), retActivity.getNewValue());
        assertEquals("Attribute", activity.getAttribute(), retActivity.getAttribute());
    }

    public void testCreateShort() throws Exception
    {
        System.out.println("\ntestCreateShort()");
        Transaction trans = getEditTransaction();
        Activity activity = new Activity();
        activity.create(getIssue0(), null, "new activity short create",
          trans, "oldValue", "newValue");
        Transaction newtrans = activity.getTransaction();
        assertEquals("getTransaction expected: " + trans.getTransactionId() +
        " got: " + newtrans.getTransactionId(),
        trans.getTransactionId().toString(), newtrans.getTransactionId().toString());
        Activity retActivity = (Activity)ActivityPeer.retrieveByPK(activity.getActivityId());
        assertEquals("OldValue", activity.getOldValue(), retActivity.getOldValue());
        assertEquals("NewValue", activity.getNewValue(), retActivity.getNewValue());
        assertEquals("Attribute", activity.getAttribute(), retActivity.getAttribute());
    }

    public void testGetAttribute() throws Exception
    {
        System.out.println("\ntestGetAttribute()");
        Activity retActivity = (Activity)ActivityPeer.retrieveByPK(new NumberKey(1));
        NumberKey key = retActivity.getAttribute().getAttributeId();
        assertTrue("AttId expected: 11 got: " + key, key.equals("11"));
    }
}
