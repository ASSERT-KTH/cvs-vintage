/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/security/file/Attic/FileRealmUser.java,v 1.1 2000/02/11 00:22:34 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/02/11 00:22:34 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.security.file;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * In-memory representation of an individual user, to which specific roles
 * may be assigned directly, or indirectly by virtue of membership in one
 * or more groups.  This class exhibits the following JavaBeans properties:
 * <ul>
 * <li><b>groups</b> - The set of groups this user is a member of.
 * <li><b>name</b> - Username that uniquely (within a particular security
 *     domain) identifies this user.
 * <li><b>password</b> - The password used to authenticate this user's
 *     identity.  Internally, this value is stored in an encrypted fashion,
 *     and is not available in clear text.
 * <li><b>roles</b> - The set of role names explicitly assigned to this user.
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/02/11 00:22:34 $
 */

final class FileRealmUser
    implements Principal {


    /**
     * The database containing this user.
     */
    private FileRealmDatabase database = null;


    /**
     * The set of groups this user is a member of, keyed by group name.
     * Each element is an instance of FileRealmGroup.
     */
    private Hashtable groups = new Hashtable();


    /**
     * The username assigned to this user.
     */
    private String name = null;


    /**
     * The (encrypted) password, stored as a byte array.
     */
    private byte[] password = new byte[0];


    /**
     * The set of roles assigned explicitly to this user, keyed by role name.
     * The values are arbitrary.
     */
    private Hashtable roles = new Hashtable();


    /**
     * [Package Private] Create a new user with the specified username.
     * It is assumed that the creating entity has ensured that this
     * username is unique within this security realm.
     *
     * @param database FileRealmDatabase containing the new user
     * @param name Username assigned to the new user
     * @param password Cleartext password
     */
    FileRealmUser(FileRealmDatabase database, String name, String password) {

	super();
	this.database = database;
	this.name = name;
	setPassword(password);
	database.addUser(this);

    }


    /**
     * [Package Private] Create a new user with the specified username.
     * It is assumed that the creating entity has ensured that this
     * username is unique within this security realm.
     *
     * @param database FileRealmDatabase containing the new user
     * @param name Username assigned to the new user
     * @param password Encrypted password
     */
    FileRealmUser(FileRealmDatabase database, String name, byte[] password) {

	super();
	this.database = database;
	this.name = name;
	setPassword(password);
	database.addUser(this);

    }


    /**
     * Add this user as a member of the specified group.
     *
     * @param group Group this user is now a member of
     */
    public void addGroup(FileRealmGroup group) {

	group.addUser(this);
	groups.put(group.getName(), group);

    }


    /**
     * Add the explicit assignment of the specified role to this user.
     *
     * @param role The role being assigned to this group
     */
    public void addRole(String role) {

	database.addRole(role);
	roles.put(role, role);

    }


    /**
     * Can this user be authenticated with the specified password?
     *
     * @param password Password (cleartext) to be used for authentication
     */
    public boolean authenticate(String password) {

	return (MessageDigest.isEqual(this.password, encrypt(password)));

    }


    /**
     * Remove this user from the database to which it belongs.
     */
    public void destroy() {

	database.remove(this);

    }


    /**
     * [Private] Return the encrypted version of this cleartext password.
     *
     * @param password Cleartext password to be encrypted
     */
    private byte[] encrypt(String password) {

	if (password == null)
	    return (new byte[0]);

	// Create a MessageDigest for use in performing the encryption
	MessageDigest digest = null;
	try {
	    digest = MessageDigest.getInstance("SHA");
	} catch (NoSuchAlgorithmException e) {
	    return (new byte[0]);
	}

	// Calculate the digest for the specified password
	// XXX Obviously this does not deal with Unicode correctly
	for (int i = 0; i < password.length(); i++) {
	    char ch = password.charAt(i);
	    digest.update((byte) (ch & 0x7f));
	}
	return (digest.digest());

    }


    /**
     * Return an enumeration of the groups to which this user belongs.
     * If this user is a member of no groups, an empty enumeration is
     * returned.  Each element in the enumeration is an instance of
     * FileRealmGroup.
     */
    public Enumeration getGroups() {

	return (groups.elements());

    }


    /**
     * Return the username of this user.
     */
    public String getName() {

	return (name);

    }


    /**
     * Return the encrypted version of the password.
     */
    public byte[] getPassword() {

	return (password);

    }


    /**
     * Return an enumeration of the roles explicitly assigned to this user.
     * If there are no assigned roles, an empty enumeration is returned.
     */
    public Enumeration getRoles() {

	return (roles.keys());

    }


    /**
     * Is this user a member of the specified group?
     *
     * @param group The group to be tested
     */
    public boolean hasGroup(FileRealmGroup group) {

	return (groups.get(group.getName()) != null);

    }


    /**
     * Has this user been explicitly assigned the specified role?
     *
     * @param role The role to be tested
     */
    public boolean hasRole(String role) {

	return (roles.get(role) != null);

    }


    /**
     * Remove this user from membership in the specified group.
     *
     * @param group Group from which to remove this user's membership.
     */
    public void remove(FileRealmGroup group) {

	groups.remove(group.getName());
	group.remove(this);

    }


    /**
     * Remove the specified explicitly assigned role from this user.
     *
     * @param role Role to be removed
     */
    public void remove(String role) {

	roles.remove(role);

    }


    /**
     * Set the password associated with this user.
     *
     * @param password Encrypted password to be stored
     */
    public void setPassword(byte[] password) {

	this.password = new byte[password.length];
	for (int i = 0; i < this.password.length; i++)
	    this.password[i] = password[i];

    }


    /**
     * Set the password associated with this user.  This cleartext value
     * will be immediately encrypted for storage.
     *
     * @param password Cleartext password to be stored
     */
    public void setPassword(String password) {

	this.password = encrypt(password);

    }


}
