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

import org.columba.mail.util.MailResourceLoader;

public class AuthenticationManager {
	
	// Default
	public static final int MOST_SECURE = 0;
	
	// Protocol defined Mechanisms
	public static final int USER = 1;
	public static final int LOGIN = 2;
	public static final int APOP = 3;
	
	//SMTP
	public static final int POP_BEFORE_SMTP = 4;
	public static final int NONE = 5;
	
	
	// SASL Mechanisms
	public static final int SASL = 10;
	public static final int SASL_PLAIN = 10;
	public static final int SASL_LOGIN= 11;
	public static final int SASL_DIGEST_MD5 = 12;

	public static final String[] SaslMechanism = { "PLAIN", "LOGIN", "DIGEST-MD5" };  
	
	public static int getSaslCode(String mechanism) {
		for( int i=0; i<SaslMechanism.length; i++) {
			if( SaslMechanism[i].equalsIgnoreCase(mechanism)) {
				return SASL+i;
			}
		}
		
		return -1;
	}
	
	public static String getSaslName(int code) {
		return SaslMechanism[code-SASL];
	}
	
	public static int compare(int code1, int code2) {
		if( code1 == code2) return 0;

		if( code1 < SASL ) code1 -= SASL - 1;
		if( code2 < SASL ) code2 -= SASL - 1;
		
		if( code1 < code2 ) return -1;
		else return 1;		
	}
	
	public static String getLocalizedString(int code) {
		switch( code ) {
			case MOST_SECURE : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_securest");
			}
			
			case USER : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_user");
			}

			case APOP : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_apop");
			}

			case LOGIN : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_login");
			}

			case SASL_PLAIN : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_sasl_plain");
			}

			case SASL_LOGIN : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_sasl_login");
			}

			case SASL_DIGEST_MD5 : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_sasl_digest_md5");
			}

			case POP_BEFORE_SMTP : {
				return MailResourceLoader.getString("dialog",
						"account", "authentication_pop_before_smtp");
			}

			default : {
				return "Invalid Code";
			}
		}
	}
	
}
