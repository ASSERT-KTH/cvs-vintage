//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.pop3;

import junit.framework.TestCase;

public class AuthenticationManagerTest extends TestCase {

	public void testCompare1() {
		int a = AuthenticationManager.APOP;
		int b = AuthenticationManager.LOGIN;
		
		assertEquals(1, AuthenticationManager.compare(a,b));
	}
	
	public void testCompare2() {
		int a = AuthenticationManager.SASL_LOGIN;
		int b = AuthenticationManager.LOGIN;
		
		assertEquals(0, AuthenticationManager.compare(a,b));
	}

	public void testCompare3() {
		int a = AuthenticationManager.POP_BEFORE_SMTP;
		int b = AuthenticationManager.LOGIN;
		
		assertEquals(-1, AuthenticationManager.compare(a,b));
	}

	public void testCompare4() {
		int a = AuthenticationManager.APOP;
		int b = AuthenticationManager.SASL_DIGEST_MD5;
		
		assertEquals(0, AuthenticationManager.compare(a,b));
	}

}
