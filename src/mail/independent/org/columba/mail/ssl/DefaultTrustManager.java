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
package org.columba.mail.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


/**
 * @author fdietz
 *
 * A simple default trust manager which trusts
 * ALL certificats.
 * 
 * Read the following articles on SSL to get you started:
 * - http://www.javaworld.com/javaworld/jw-05-2001/jw-0511-howto.html
 * - http://www.javaworld.com/javaworld/jw-04-2001/jw-0413-howto.html
 * - http://www.javaworld.com/javaworld/jw-01-2001/jw-0112-howto.html
 * 
 * javamail with SSL support:
 * - http://www.javaworld.com/javaworld/javatips/jw-javatip115.html
 * 
 * 
 */
public class DefaultTrustManager implements X509TrustManager {
	
	
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
	
	public boolean isClientTrusted(X509Certificate[] chain) {
		return true;
	}
	
	public boolean isServerTrusted(X509Certificate[] chain) {
		return true;
	}
	
	public void checkServerTrusted(X509Certificate[] chain, String authType) {
	}
	
	public void checkClientTrusted(X509Certificate[] chain, String authType) {
	}
}
