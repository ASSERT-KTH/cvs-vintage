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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @author fdietz
 *
 * SSLProvider provides you with a SSLSocketFactory to create 
 * Sockets with SSL support.
 * 
 * Using <class>DefaultTrustManager</class> this version trusts
 * all certificates.
 * 
 * See DefaultTrustManager for a couple of interesting reads.
 * 
 * 
 */
public class SSLProvider {

	public static void initializeForSSL() {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		System.setProperty(
			"java.protocol.handler.pkgs",
			"com.sun.net.ssl.internal.www.protocol");
	}

	public static SSLSocketFactory createSocketFactory() {
		initializeForSSL();
		enableDebugging();
		
		X509TrustManager tm = new DefaultTrustManager();
		KeyManager[] km = null;
		TrustManager[] tma = new TrustManager[] { tm };
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(km, tma, new java.security.SecureRandom());

			return sslContext.getSocketFactory();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(System.out);
		} catch (KeyManagementException e) {
			e.printStackTrace(System.out);
		}

		return null;
	}

	public static void enableDebugging() {
		System.setProperty(
			"javax.net.debug",
			"ssl,handshake,data,trustmanager");
	}
}
