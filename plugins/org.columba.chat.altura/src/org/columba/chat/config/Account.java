// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.chat.config;

import org.columba.chat.config.api.IAccount;

/**
 * Contains account-related configuration.
 * 
 * @author fdietz
 * 
 */
public class Account implements IAccount {

	private String id;

	private char[] password;

	private String host;

	private String resource;

	private int port;

	private boolean enableSSL;

	public Account() {
		host = "jabber.org";
		port = 5222;
		enableSSL = false;
	}

	public Account(String id) {
		this();

		if (id == null)
			throw new IllegalArgumentException("id == null");

		this.id = id;
	}

	public Account(String id, String host) {
		this.id = id;
		this.host = host;

		port = 5222;
		enableSSL = false;
	}

	/**
	 * @return Returns the enableSSL.
	 */
	public boolean isEnableSSL() {
		return enableSSL;
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the password.
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param enableSSL
	 *            The enableSSL to set.
	 */
	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	/**
	 * @param host
	 *            The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

	/**
	 * @param port
	 *            The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return Returns the resource.
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource
	 *            The resource to set.
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}
}
