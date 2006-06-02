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

package org.columba.core.main;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;

import org.columba.core.shutdown.ShutdownManager;

/**
 * Columba's main class used to start the application.
 */
public class Main {

	/**
	 * global classloader used as parent classloader in Columba everywhere
	 */
	public static MainClassLoader mainClassLoader;

	public static void main(String[] args) throws Exception {

		// @author: fdietz
		//
		// PROBLEM: Extensions don't run using Java Webstart (JWS)
		// 
		// Even though we assign "all-permission" in our columba.jnlp file, this only applies
		// to the initial Java Webstart classloader. But, we create our own classloaders for
		// loading extensions. These classloaders don't have the same permission settings anymore.
		
		// WORKAROUND: 
		// 
		// System.setSecurityManager(null);
		//
		// This call effectly disables the sandbox mode and seems to work fine.
		//
		// Below I use another way. The policy for all classloaders is set to "all-permissions". 
		// Don't really know the difference though.
		
		// grant "all-permissions"
		Policy.setPolicy(new Policy() {
			public PermissionCollection getPermissions(CodeSource codesource) {
				Permissions perms = new Permissions();
				perms.add(new AllPermission());
				return (perms);
			}

			public void refresh() {
			}
		});

		start(args);
	}

	public static void restart(String[] args) throws Exception {

		// shutdown Columba
		ShutdownManager.getInstance().shutdown(0);

		// set global class loader to null
		mainClassLoader = null;

		// force object finalization
		System.runFinalization();

		// run garbage collector
		System.gc();

		// startup Columba
		start(args);
	}

	private static void start(String[] args) throws Exception {
		// initialize global class loader
		mainClassLoader = new MainClassLoader(Main.class.getClassLoader());

		// use global class loader to bootstrap Columba
		Bootstrap startup = (Bootstrap) mainClassLoader.loadClass(
				Bootstrap.class.getName()).newInstance();
		startup.run(args);
	}

}