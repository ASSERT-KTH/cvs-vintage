package org.tigris.scarab.util;

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

import junit.framework.TestCase;

/**
 * Used for testing the ScarabUtil.java class.
 *
 * @author <a href="mailto:Sebastian.Dietrich@anecon.com">Sebastian Dietrich</a>
 * @version $Id: ScarabUtilTest.java,v 1.3 2004/01/31 18:15:39 dep4b Exp $
 */
public class ScarabUtilTest extends TestCase
{

	
	public void testUrlEncode()
	{
		assertEquals("urlEncode of null should be null", null, ScarabUtil.urlEncode(null));
		assertEquals("urlEncode of empty string should be empty string", "", ScarabUtil.urlEncode("").toString());
		assertEquals("urlEncode of 'test' should be 'test'", "test", ScarabUtil.urlEncode("test").toString());
		assertEquals("urlEncode of '-_.!~*\'()' should remain the same", "-_.!~*\'()", ScarabUtil.urlEncode("-_.!~*\'()").toString());
		assertEquals("urlEncode of 'äöü ßÄÖÜ?' should be some nasty url-string", "%E4%F6%FC+%DF%C4%D6%DC%3F", ScarabUtil.urlEncode("äöü ßÄÖÜ?").toString());
		assertEquals("urlEncode of 'test äöü' should be some nasty url-string", "test+%E4%F6%FC", ScarabUtil.urlEncode("test äöü").toString());
	}
}
