/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/security/file/Attic/FileRealmTool.java,v 1.1 1999/10/23 22:30:18 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/23 22:30:18 $
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import org.apache.tomcat.util.StringManager;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Command-line tool to manipulate database files containing FileRealmDatabase
 * information.  Usage:
 * <pre>
 *	java org.apache.tomcat.security.file.FileRealmTool {path} {commands}
 * </pre>
 * where <code>{path}</code> is the pathname to a local file containing the
 * database information, and <code>{commands}</code> is a series of zero or
 * more commands (each of which including command parameters) from the
 * following list:
 * <ul>
 * <li><b>-addGroup {group}</b> - Add a new group with the specified name
 * <li><b>-addMember {group} {user}</b> - Make {user} a member of {group}
 * <li><b>-addRole {group/user} {role}</b> - Add an explicit assignment of
 *     {role} to the specified {group} or {user}.
 * <li><b>-addUser {user} {password}</b> - Add a new user with the specified
 *     username and (cleartext) password.
 * <li><b>-dropGroup {group}</b> - Drop this group and all its associated
 *     memberships and roles
 * <li><b>-dropMember {group} {user}</b> - Make {user} no longer a member
 *     of {group}
 * <li><b>-dropRole {group/user} {role}</b> - Drop this explicitly assigned
 *     {role} from the specified {group} or {user}.
 * <li><b>-dropUser {user}</b> - Drop this user and all its associated
 *     memberships and roles
 * <li><b>-list</b> - List the users, groups, and roles in this database
 * </ul>
 * <p>
 * To initialize a new database file for use by this tool, create a text file
 * (whose name ends in ".xml") with the following contents:
 * <pre>
 *	<tomcat-users>
 *	</tomcat-users>
 * </pre>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 1999/10/23 22:30:18 $
 */

public final class FileRealmTool {


    /**
     * The command line arguments we are processing
     */
    private static String args[] = null;


    /**
     * The subscript of the next argument to be returned by getArg().
     */
    private static int current = 0;


    /**
     * The database we are processing.
     */
    private static FileRealmDatabase database = null;


    /**
     * The name of the database file we are processing.
     */
    private static String filename = null;


    /**
     * Has the cached database information been modified?
     */
    private static boolean modified = false;


    /**
     * The internationalized string constants for this package.
     */
    private static StringManager sm =
    	StringManager.getManager(Constants.Package);


    /**
     * The application main program.
     */
    public static void main(String arguments[]) {

	args = arguments;
	open();
	process();
	close();

    }


    /**
     * Add a new group, if it is not already present.
     */
    private static void addGroup() {

	String name = getArg();
	if (name.length() < 1) {
	    System.err.println(sm.getString("tool.missing.group"));
	    System.exit(2);
	}

	FileRealmGroup group = database.getGroup(name);
	if (group != null)
	    return;
	group = database.createGroup(name);
	modified = true;

    }


    /**
     * Add a new group membership, if not already present.
     */
    private static void addMember() {

	String groupName = getArg();
	if (groupName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.group"));
	    System.exit(2);
	}
	FileRealmGroup group = database.getGroup(groupName);
	if (group == null) {
	    System.err.println(sm.getString("tool.unknown.group", groupName));
	    System.exit(2);
	}

	String userName = getArg();
	if (userName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.user"));
	    System.exit(2);
	}
	FileRealmUser user = database.getUser(userName);
	if (user == null) {
	    System.err.println(sm.getString("tool.unknown.user", userName));
	    System.exit(2);
	}

	if (!user.hasGroup(group)) {
	    user.add(group);
	    modified = true;
	}

    }


    /**
     * Add a new assigned role to a group or user, if not already present.
     */
    private static void addRole() {

	String principalName = getArg();
	if (principalName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.principal"));
	    System.exit(2);
	}
	FileRealmGroup group = database.getGroup(principalName);
	FileRealmUser user = database.getUser(principalName);
	if ((group == null) && (user == null)) {
	    System.err.println(sm.getString("tool.unknown.principal",
					    principalName));
	    System.exit(2);
	}

	String role = getArg();
	if (role.length() < 1) {
	    System.err.println(sm.getString("tool.missing.role"));
	    System.exit(2);
	}

	if (group != null) {
	    if (!group.hasRole(role)) {
		group.add(role);
		modified = true;
	    }
	}

	if (user != null) {
	    if (!user.hasRole(role)) {
		user.add(role);
		modified = true;
	    }
	}

    }


    /**
     * Add a new user, if it is not already present.  If the user is already
     * present, update the password to the specified value.
     */
    private static void addUser() {

	String name = getArg();
	if (name.length() < 1) {
	    System.err.println(sm.getString("tool.missing.user"));
	    System.exit(2);
	}
	String password = getArg();
	if (password.length() < 1) {
	    System.err.println(sm.getString("tool.missing.password"));
	    System.exit(2);
	}

	FileRealmUser user = database.getUser(name);
	if (user != null)
	    user.setPassword(password);
	else
	    user = database.createUser(name, password);
	modified = true;

    }


    /**
     * Close the database, flushing any changes first.
     */
    private static void close() {

	if (!modified)
	    return;

	OutputStream stream = null;
	try {
	    stream = new BufferedOutputStream(new FileOutputStream(filename));
	} catch (IOException e) {
	    System.err.println(sm.getString("tool.close.open", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    database.write(stream);
	} catch (IOException e) {
	    System.err.println(sm.getString("tool.close.write", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    stream.close();
	} catch (IOException e) {
	    ;
	}

    }


    /** [Private] Convert the specified byte array representation of an
     * encrypted password into the corresponding hexadecimal digit
     * representation.
     *
     * @param bytes Byte array representation
     */
    private static String convert(byte bytes[]) {

	StringBuffer sb = new StringBuffer(bytes.length * 2);
	for (int i = 0; i < bytes.length; i++) {
	    sb.append(convertDigit((int) (bytes[i] >> 4)));
	    sb.append(convertDigit((int) (bytes[i] & 0x0f)));
	}
	return (sb.toString());

    }


    /**
     * [Private] Convert the specified value (0 .. 15) to the corresponding
     * hexadecimal digit.
     *
     * @param value Value to be converted
     */
    private static char convertDigit(int value) {

	value &= 0x0f;
	if (value >= 10)
	    return ((char) (value - 10 + 'a'));
	else
	    return ((char) (value + '0'));

    }


    /**
     * Drop an existing group, if it is already present.
     */
    private static void dropGroup() {

	String name = getArg();
	if (name.length() < 1) {
	    System.err.println(sm.getString("tool.missing.group"));
	    System.exit(2);
	}

	FileRealmGroup group = database.getGroup(name);
	if (group == null) {
	    System.err.println(sm.getString("tool.unknown.group"));
	    System.exit(2);
	}
	database.remove(group);
	modified = true;

    }


    /**
     * Drop an existing group membership.
     */
    private static void dropMember() {

	String groupName = getArg();
	if (groupName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.group"));
	    System.exit(2);
	}
	FileRealmGroup group = database.getGroup(groupName);
	if (group == null) {
	    System.err.println(sm.getString("tool.unknown.group", groupName));
	    System.exit(2);
	}

	String userName = getArg();
	if (userName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.user"));
	    System.exit(2);
	}
	FileRealmUser user = database.getUser(userName);
	if (user == null) {
	    System.err.println(sm.getString("tool.unknown.user", userName));
	    System.exit(2);
	}

	if (user.hasGroup(group)) {
	    user.remove(group);
	    modified = true;
	}

    }


    /**
     * Drop an existing assigned role for a group or user.
     */
    private static void dropRole() {

	String principalName = getArg();
	if (principalName.length() < 1) {
	    System.err.println(sm.getString("tool.missing.principal"));
	    System.exit(2);
	}
	FileRealmGroup group = database.getGroup(principalName);
	FileRealmUser user = database.getUser(principalName);
	if ((group == null) && (user == null)) {
	    System.err.println(sm.getString("tool.unknown.principal",
					    principalName));
	    System.exit(2);
	}

	String role = getArg();
	if (role.length() < 1) {
	    System.err.println(sm.getString("tool.missing.role"));
	    System.exit(2);
	}

	if (group != null) {
	    if (group.hasRole(role)) {
		group.remove(role);
		modified = true;
	    }
	}

	if (user != null) {
	    if (user.hasRole(role)) {
		user.remove(role);
		modified = true;
	    }
	}

    }


    /**
     * Drop an existing user, if it is already present.
     */
    private static void dropUser() {

	String name = getArg();
	if (name.length() < 1) {
	    System.err.println(sm.getString("tool.missing.user"));
	    System.exit(2);
	}

	FileRealmUser user = database.getUser(name);
	if (user == null) {
	    System.err.println(sm.getString("tool.unknown.user"));
	    System.exit(2);
	}
	database.remove(user);
	modified = true;

    }


    /**
     * Return the next command line argument, if there is one.  Otherwise,
     * return a zero-length string.
     */
    private static String getArg() {

	if (current >= args.length)
	    return ("");
	else
	    return (args[current++]);

    }


    /**
     * List the users and groups, and their associated roles, in this database.
     */
    private static void list() {

	Enumeration users = database.getUsers();
	while (users.hasMoreElements()) {
	    FileRealmUser user = (FileRealmUser) users.nextElement();
	    System.out.print("User '" + user.getName() +
			     "', password='" + convert(user.getPassword()) +
			     "'");
	    Enumeration groups = user.getGroups();
	    while (groups.hasMoreElements()) {
		FileRealmGroup group = (FileRealmGroup) groups.nextElement();
		System.out.print(", group='" + group.getName() + "'");
	    }
	    Enumeration roles = user.getRoles();
	    while (roles.hasMoreElements()) {
		String role = (String) roles.nextElement();
		System.out.print(", role='" + role + "'");
	    }
	    System.out.println();
	}

	Enumeration groups = database.getGroups();
	while (groups.hasMoreElements()) {
	    FileRealmGroup group = (FileRealmGroup) groups.nextElement();
	    System.out.print("Group '" + group.getName() + "'");
	    Enumeration roles = group.getRoles();
	    while (roles.hasMoreElements()) {
		String role = (String) roles.nextElement();
		System.out.print(", role='" + role + "'");
	    }
	    System.out.println();
	}

	System.out.println("===============================================");

    }


    /**
     * Open the database file specified by the next command line argument.
     */
    private static void open() {

	filename = getArg();
	if (filename.length() < 1) {
	    System.err.println(sm.getString("tool.open.missing"));
	    System.exit(1);
	}

	InputStream stream = null;
	try {
	    stream = new BufferedInputStream(new FileInputStream(filename));
	} catch (FileNotFoundException e) {
	    System.err.println(sm.getString("tool.open.open", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    database = new FileRealmDatabase(stream);
	} catch (IOException e) {
	    System.err.println(sm.getString("tool.open.read", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	} catch (SAXParseException e) {
	    System.err.println(sm.getString("tool.open.parse", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	} catch (SAXException e) {
	    System.err.println(sm.getString("tool.open.process", filename) +
			       ": " + e);
	    e.printStackTrace();
	    System.exit(1);
	}
	try {
	    stream.close();
	} catch (IOException e) {
	    ;
	}

    }


    /**
     * Process all valid command from the command line arguments.
     */
    private static void process() {

	while (true) {
	    String command = getArg();
	    if (command.length() < 1)
		break;
	    else if (command.equals("-addGroup"))
		addGroup();
	    else if (command.equals("-addMember"))
		addMember();
	    else if (command.equals("-addRole"))
		addRole();
	    else if (command.equals("-addUser"))
		addUser();
	    else if (command.equals("-dropGroup"))
		dropGroup();
	    else if (command.equals("-dropMember"))
		dropMember();
	    else if (command.equals("-dropRole"))
		dropRole();
	    else if (command.equals("-dropUser"))
		dropUser();
	    else if (command.equals("-list"))
		list();
	    else {
		System.err.println(sm.getString("tool.process.command",
						command));
	        modified = false;
		System.exit(2);
	    }
	}

    }


}
