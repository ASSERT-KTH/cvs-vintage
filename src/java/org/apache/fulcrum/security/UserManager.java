package org.apache.fulcrum.security;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PasswordMismatchException;

import org.apache.torque.util.Criteria;

/**
 * An UserManager performs {@link org.apache.fulcrum.security.entity.User} objects
 * related tasks on behalf of the {@link org.apache.fulcrum.security.BaseSecurityService}.
 *
 * The responsibilities of this class include loading data of an user from the
 * storage and putting them into the {@link org.apache.fulcrum.security.entity.User} objects,
 * saving those data to the permanent storage, and authenticating users.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @version $Id: UserManager.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public interface UserManager
{
    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param user The user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data backend.
     */
    boolean accountExists(User user)
        throws DataBackendException;

    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param userName The name of the user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data backend.
     */
    boolean accountExists(String userName)
        throws DataBackendException;

   /**
     * Retrieve a user from persistent storage using username as the
     * key.
     *
     * @param username the name of the user.
     * @return an User object.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    User retrieve(String username)
        throws UnknownEntityException, DataBackendException;

    /**
     * Retrieve a set of users that meet the specified criteria.
     *
     * As the keys for the criteria, you should use the constants that
     * are defined in {@link User} interface, plus the names
     * of the custom attributes you added to your user representation
     * in the data storage. Use verbatim names of the attributes -
     * without table name prefix in case of DB implementation.
     *
     * @param criteria The criteria of selection.
     * @return a List of users meeting the criteria.
     * @throws DataBackendException if there is a problem accessing the
     *         storage.
     */
    User[] retrieve(Criteria criteria)
        throws DataBackendException;

    /**
     * Retrieve a user from persistent storage using username as the
     * key, and authenticate the user. The implementation may chose
     * to authenticate to the server as the user whose data is being
     * retrieved.
     *
     * @param username the name of the user.
     * @param password the user supplied password.
     * @return an User object.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    User retrieve(String username, String password)
        throws PasswordMismatchException, UnknownEntityException,
               DataBackendException;

    /**
     * Save an User object to persistent storage. User's record is
     * required to exist in the storage.
     *
     * @param user an User object to store.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    void store(User user)
        throws UnknownEntityException, DataBackendException;

    /**
     * Authenticate an User with the specified password. If authentication
     * is successful the method returns nothing. If there are any problems,
     * exception was thrown.
     *
     * @param user an User object to authenticate.
     * @param password the user supplied password.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    void authenticate(User user, String password)
        throws PasswordMismatchException, UnknownEntityException,
               DataBackendException;

    /**
     * Creates new user account with specified attributes.
     *
     * @param user the object describing account to be created.
     * @param password The password to use for the object creation
     *
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws EntityExistsException if the user account already exists.
     */
    void createAccount(User user, String password)
        throws EntityExistsException, DataBackendException;

    /**
     * Removes an user account from the system.
     *
     * @param user the object describing the account to be removed.
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws UnknownEntityException if the user account is not present.
     */
    void removeAccount(User user)
        throws UnknownEntityException, DataBackendException;
    /**
     * Change the password for an User.
     *
     * @param user an User to change password for.
     * @param oldPassword the current password suplied by the user.
     * @param newPassword the current password requested by the user.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    void changePassword(User user, String oldPassword, String newPassword)
        throws PasswordMismatchException, UnknownEntityException,
               DataBackendException;

    /**
     * Forcibly sets new password for an User.
     *
     * This is supposed by the administrator to change the forgotten or
     * compromised passwords. Certain implementatations of this feature
     * would require administrative level access to the authenticating
     * server / program.
     *
     * @param user an User to change password for.
     * @param password the new password.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    void forcePassword(User user, String password)
        throws UnknownEntityException, DataBackendException;
}
