/*
 * Created on 31.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.mail.nativ.defaultmailclient;

import com.jniwrapper.win32.registry.RegistryKey;


/**
 * @author Timo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Win32SystemDefaultMailClient implements SystemDefaultMailClient {

	
	/**
	 * @see org.columba.mail.nativ.defaultmailclient.SystemDefaultMailClient#isDefaultMailClient()
	 */
	public boolean isDefaultMailClient() {
		RegistryKey mailClients = RegistryKey.LOCAL_MACHINE.openSubKey("Software").openSubKey("Clients").openSubKey("Mail", true);
		if( mailClients == null ) return true;
		
		return mailClients.values().get("").equals("Columba");
	}

	/**
	 * @see org.columba.mail.nativ.defaultmailclient.SystemDefaultMailClient#setDefaultMailClient()
	 */
	public void setDefaultMailClient() {
		RegistryKey mailClients = RegistryKey.LOCAL_MACHINE.openSubKey("Software").openSubKey("Clients").openSubKey("Mail", true);
		
		if( mailClients.exists("Columba")) {
			mailClients.values().put("", "Columba");
			
			// Copy the commandline from the client info to the Classes mailto entry
			RegistryKey classesMailto = RegistryKey.CLASSES_ROOT.openSubKey("mailto").openSubKey("shell").openSubKey("open").openSubKey("command",true);
			
			RegistryKey clientMailto = mailClients.openSubKey("Columba").openSubKey("Protocols").openSubKey("mailto").openSubKey("shell").openSubKey("open").openSubKey("command");
			
			classesMailto.values().put("",clientMailto.values().get(""));
		}
	}
}
