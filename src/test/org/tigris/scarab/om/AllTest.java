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

import org.tigris.scarab.test.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Used for running all of the tests at once.
 *
 * @author <a href="mailto:tmcnerney@truis.com">Tim McNerney</a>
 * @version $Id: AllTest.java,v 1.12 2003/04/18 23:04:46 elicia Exp $
 */
public class AllTest extends BaseTestCase
{
    /**
     * @param name    Name of Object
     */
    public AllTest(String name)
    {
        super(name);
    }

    public AllTest()
    {
        super("AllTest");
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(AttributeTest.suite());
        suite.addTest(AttributeOptionTest.suite());
        suite.addTest(IssueTest.suite());
        suite.addTest(ScarabUserTest.suite());
        suite.addTest(ScarabModuleTest.suite());
        suite.addTest(QueryTest.suite());
        suite.addTest(RModuleOptionTest.suite());
        suite.addTest(ActivityTest.suite());
        suite.addTest(ActivitySetTest.suite());
        suite.addTest(RModuleAttributeTest.suite());
        suite.addTest(AttributeGroupTest.suite());
        suite.addTest(AttachmentTest.suite());
        suite.addTest(AttributeValueTest.suite());
        suite.addTest(IssueTypeTest.suite());
        suite.addTest(RModuleIssueTypeTest.suite());
        return suite;
    }

    /**
     * Main method needed to make a self runnable class
     *
     * @param args This is required for main method
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(new TestSuite(AllTest.class));
    }
}
