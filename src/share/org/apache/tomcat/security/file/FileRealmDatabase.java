/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/security/file/Attic/FileRealmDatabase.java,v 1.1 1999/10/18 03:34:04 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/18 03:34:04 $
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tomcat.util.XMLParser;
import org.apache.tomcat.util.XMLTree;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * In-memory cache of the set of users, groups, and their associated roles,
 * stored in an XML-formatted file that conforms to DTD found in the
 * <code>tomcat-users.dtd</code> file in this directory.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 1999/10/18 03:34:04 $
 */

public final class FileRealmDatabase {


    /**
     * The set of groups defined within this database, keyed by group name.
     */
    private Hashtable groups = new Hashtable();


    /**
     * The set of roles defined within this database, keyed by role name.
     * The value objects are arbitrary.
     */
    private Hashtable roles = new Hashtable();


    /**
     * The set of users defined within this database, keyed by username.
     */
    private Hashtable users = new Hashtable();


    /**
     * Construct a new empty database.
     */
    public FileRealmDatabase() {

	super();

    }


    /**
     * Construct a new database initialized from the specified input stream
     *
     * @param stream Stream from which to load the contents of this database
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXParseException if a parsing exception occurs
     * @exception SAXException if a processing exception occurs
     */
    public FileRealmDatabase(InputStream stream)
	throws IOException, SAXParseException, SAXException {

	super();
	read(stream);

    }


    /**
     * [Package Private] Add this group to the set of defined groups.
     */
    void add(FileRealmGroup group) {

	groups.put(group.getName(), group);

    }


    /**
     * [Package Private] Add this role to the set of defined roles.
     */
    void add(String role) {

	roles.put(role, role);

    }


    /**
     * [Package Private] Add this user to the set of defined users.
     */
    void add(FileRealmUser user) {

	users.put(user.getName(), user);

    }


    /**
     * [Private] Convert the hexadecimal digit representation of an encrypted
     * password into the corresponding byte array representation.
     *
     * @param digits Hexadecimal digits representation
     */
    private byte[] convert(String digits) {

	// XXX Any good hex->byte converters lying around?
	return (new byte[0]);

    }


    /**
     * Create and return a new group.
     *
     * @param name Group name of the newly created group
     *
     * @exception IllegalArgumentException if this group name is already in use
     */
    public FileRealmGroup createGroup(String name) {

	if (getGroup(name) != null) {
	    // XXX Internationalization
	    throw new IllegalArgumentException("createGroup:  " +
					       "Group " + name +
					       " already exists");
	}

	return (new FileRealmGroup(this, name));

    }


    /**
     * Create and return a new user.
     *
     * @param name Username of the newly created user
     * @param password Cleartext password of the newly created user
     *
     * @exception IllegalArgumentException if this username is already in use
     */
    public FileRealmUser createUser(String name, String password) {

	if (getUser(name) != null) {
	    // XXX Internationalization
	    throw new IllegalArgumentException("createUser:  " +
					       "User " + name +
					       " already exists");
	}

	return (new FileRealmUser(this, name, password));

    }


    /**
     * Create and return a new user.
     *
     * @param name Username of the newly created user
     * @param password Encrypted password of the newly created user
     *
     * @exception IllegalArgumentException if this username is already in use
     */
    public FileRealmUser createUser(String name, byte[] password) {

	if (getUser(name) != null) {
	    // XXX Internationalization
	    throw new IllegalArgumentException("createUser:  " +
					       "User " + name +
					       " already exists");
	}

	return (new FileRealmUser(this, name, password));

    }


    /**
     * Return the group with the specified name, if any.
     *
     * @param name Name of the desired group
     */
    public FileRealmGroup getGroup(String name) {

	return ((FileRealmGroup) groups.get(name));

    }


    /**
     * Return an enumeration of the defined groups in this database.
     */
    public Enumeration getGroups() {

	return (groups.elements());

    }


    /**
     * Return an enumeration of the defined roles in this database.
     */
    public Enumeration getRoles() {

	return (roles.keys());

    }


    /**
     * Return the user with the specified name, if any.
     *
     * @param name Name of the desired user
     */
    public FileRealmUser getUser(String name) {

	return ((FileRealmUser) users.get(name));

    }


    /**
     * Return an enumeration of the defined users in this database.
     */
    public Enumeration getUsers() {

	return (users.elements());

    }


    /**
     * Is the specified role valid within this database?
     *
     * @param role Role to be tested
     */
    public boolean hasRole(String role) {

	return (roles.get(role) != null);

    }


    /**
     * Load the contents of this database from the specified input stream.
     * IMPLEMENTATION NOTE:  The order of processing (users, groups, and
     * then roles) is important to correctly process XML files with forward
     * references in them.
     *
     * @param stream Input stream to read from
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXParseException if a parsing exception occurs
     * @exception SAXException if a processing exception occurs
     */
    public void read(InputStream stream)
	throws IOException, SAXParseException, SAXException {

	reset();

	// Parse the input stream into an XMLTree
	XMLParser parser = new XMLParser();
	XMLTree config = parser.process(stream);
	if (!config.getName().equals(Constants.Element.TOMCAT_USERS))
	    return;
	Enumeration e;

	// Process the defined users
	e = config.getElements(Constants.Element.USER).elements();
	while (e.hasMoreElements())
	    readUser((XMLTree) e.nextElement());

	// Process the defined groups
	e = config.getElements(Constants.Element.GROUP).elements();
	while (e.hasMoreElements())
	    readGroup((XMLTree) e.nextElement());

	// Process the defined roles
	e = config.getElements(Constants.Element.ROLE).elements();
	while (e.hasMoreElements())
	    readRole((XMLTree) e.nextElement());

    }


    /**
     * Convert the specified XML element into a new group.
     *
     * @param element XML element for this group
     */
    private void readGroup(XMLTree element) {

	// Construct the group itself
	String name =
	    (String) element.getAttribute(Constants.Attribute.NAME);
	FileRealmGroup group = createGroup(name);

	// Process the associated group memberships
	Enumeration e =
	    element.getElements(Constants.Element.USER_MEMBER).elements();
	while (e.hasMoreElements()) {
	    XMLTree um = (XMLTree) e.nextElement();
	    String username =
		(String) um.getAttribute(Constants.Attribute.NAME);
	    FileRealmUser user = getUser(username);
	    if (user != null)
		user.add(group);
	}

	// XXX: Does not support the "anyone" sub-element

    }


    /**
     * Convert the specified XML element into a new role.
     *
     * @param element XML element for this role
     */
    private void readRole(XMLTree element) {

	// Construct the role itself
	String role =
	    (String) element.getAttribute(Constants.Attribute.NAME);
	Enumeration e = null;

	// Process the associated group memberships
	e = element.getElements(Constants.Element.GROUP_MEMBER).elements();
	while (e.hasMoreElements()) {
	    XMLTree gm = (XMLTree) e.nextElement();
	    String groupname =
		(String) gm.getAttribute(Constants.Attribute.NAME);
	    FileRealmGroup group = getGroup(groupname);
	    if (group != null)
		group.add(role);
	}

	// Process the associated user memberships
	e = element.getElements(Constants.Element.USER_MEMBER).elements();
	while (e.hasMoreElements()) {
	    XMLTree um = (XMLTree) e.nextElement();
	    String username =
		(String) um.getAttribute(Constants.Attribute.NAME);
	    FileRealmUser user = getUser(username);
	    if (user != null)
		user.add(role);
	}

	// XXX: Does not support the "anyone" sub-element

    }


    /**
     * Convert the specified XML element into a new user.
     *
     * @param element XML element for this user
     */
    private void readUser(XMLTree element) {

	// Construct the user itself
	String name =
	    (String) element.getAttribute(Constants.Attribute.NAME);
	byte[] password =
          convert((String) element.getAttribute(Constants.Attribute.PASSWORD));
	createUser(name, password);

    }


    /**
     * [Package Private] Remove this group from the set of defined groups.
     *
     * @param group Group to be removed
     */
    void remove(FileRealmGroup group) {

	groups.remove(group.getName());

    }


    /**
     * [Package Private] Remove this role from the set of defined roles.
     *
     * @param role Role to be removed
     */
    void remove(String role) {

	roles.remove(role);

    }


    /**
     * [Package Private] Remove this user from the set of defined users.
     *
     * @param user User to be removed
     */
    void remove(FileRealmUser user) {

	users.remove(user.getName());

    }


    /**
     * Reset the contents of this database so that it can be reused
     */
    public void reset() {

	groups.clear();
	roles.clear();
	users.clear();

    }


    /**
     * Write the contents of this database to the specified output stream,
     * in a format suitable for loading via the read() method.
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(OutputStream stream) throws IOException {

	;	// XXX write() needs to build a DOM tree and write it out

    }


}
