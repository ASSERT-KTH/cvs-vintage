/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.struts.action;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>org.apache.struts.action.ActionMessage</code> class.
 *
 * @author Dominique Plante
 * @version $Revision: 1.3 $ $Date: 2003/02/07 00:29:21 $
 */

public class TestActionMessage extends TestCase
{
	protected ActionMessage amWithNoValue = null;
	protected ActionMessage amWithOneValue = null;
    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
	public TestActionMessage(String theName)	
	{
		super(theName);
	}

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs)
    {
        junit.awtui.TestRunner.main(new String[] {TestActionMessage.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestActionMessage.class);
    }

	public void setUp()
	{
		amWithNoValue = new ActionMessage("amWithNoValue");
		amWithOneValue = new ActionMessage("amWithOneValue", new String("stringValue"));
	}

	public void tearDown()
	{
		amWithNoValue = null;
	}
	public void testActionMessageWithNoValue()
	{
		assertTrue("testActionMessageWithNoValue value is not null",
                   amWithNoValue.getValues() == null);
		assertTrue("testActionMessageWithNoValue key is not amWithNoValue",
					amWithNoValue.getKey() == "amWithNoValue");
	}

	public void testActionMessageWithAStringValue()
	{
		Object [] values = amWithOneValue.getValues();
		assertTrue("testActionMessageWithAStringValue value is not null",
                   values != null);
		assertTrue("testActionMessageWithAStringValue value[0] is not the string stringValue",
                   values[0].equals("stringValue"));
		assertTrue("testActionMessageWithAStringValue key is not amWithOneValue",
					amWithOneValue.getKey() == "amWithOneValue");
	}
}
