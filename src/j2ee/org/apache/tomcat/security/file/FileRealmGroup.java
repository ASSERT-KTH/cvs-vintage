/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/security/file/Attic/FileRealmGroup.java,v 1.1 2000/02/11 00:22:34 costin Exp $
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


import java.util.Enumeration;
import java.util.Hashtable;


/**
 * In-memory representation of a defined group of users, which may be granted
 * specific roles indirectly by virtue of their membership in a group.  This
 * class exhibits the following JavaBeans properties:
 * <ul>
 * <li><b>name</b> - Username that uniquely (within a particular security
 *     domain) identifies this user.
 * <li><b>roles</b> - The set of role names explicitly assigned to this user.
 * <li><b>users</b> - The set of users who are members of this group.
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/02/11 00:22:34 $
 */

final class FileRealmGroup {


    /**
     * The database containing this group.
     */
    private FileRealmDatabase database = null;


    /**
     * The group name assigned to this group.
     */
    private String name = null;


    /**
     * The set of roles assigned explicitly to this group, keyed by role name.
     * The values are arbitrary.
     */
    private Hashtable roles = new Hashtable();


    /**
     * The set of users who are members of this group, keyed by username.
     */
    private Hashtable users = new Hashtable();


    /**
     * [Package Private] Create a new group with the specified group name.
     * It is assumed that the creating entity has ensured that this
     * group name is unique within this security realm.
     *
     * @param database The FileRealmDatabase containing the new group
     * @param name The group name assigned to the new group
     */
    FileRealmGroup(FileRealmDatabase database, String name) {

	super();
	this.database = database;
	this.name = name;
	database.addGroup(this);

    }


    /**
     * Add the explicit assignment of the specified role to this group.
     *
     * @param role The role being assigned to this group
     */
    public void addRole(String role) {

	database.addRole(role);
	roles.put(role, role);

    }


    /**
     * [Package Private] Add the specified user to the group members of
     * this group.
     *
     * @param user User to be added
     */
    void addUser(FileRealmUser user) {

	users.put(user.getName(), user);

    }


    /**
     * Remove this group from the database to which it belongs.
     */
    public void destroy() {

	Enumeration users = database.getUsers();
	while (users.hasMoreElements()) {
	    FileRealmUser user = (FileRealmUser) users.nextElement();
	    user.remove(this);
	}
	database.remove(this);

    }


    /**
     * Return the group name of this group.
     */
    public String getName() {

	return (name);

    }


    /**
     * Return an enumeration of the roles explicitly assigned to this group.
     * If there are no assigned roles, an empty enumeration is returned.
     */
    public Enumeration getRoles() {

	return (roles.elements());

    }


    /**
     * Return an enumeration of the users who are members of this group.
     * Each element is an instance of FileRealmUser.
     */
    public Enumeration getUsers() {

	return (users.elements());

    }


    /**
     * Has this user been assigned the specified role, either directly or
     * indirectly by virtue of group membership?
     *
     * @param role The role to be tested
     */
    public boolean hasRole(String role) {

	return (roles.get(role) != null);

    }


    /**
     * Remove the specified explicitly assigned role from this group.
     *
     * @param role Role to be removed
     */
    public void remove(String role) {

	roles.remove(role);

    }


    /**
     * [Package Private] Remove the specified user from membership in
     * this group.
     *
     * @param user The user to be removed
     */
    void remove(FileRealmUser user) {

	users.remove(user.getName());

    }


}
