package org.apache.fulcrum.security.impl.db.entity;

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

import java.util.List;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.DataBackendException;

/**
 * This class handles all the database access for the User/User
 * table.  This table contains all the information for a given user.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: TurbineUserPeer.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class TurbineUserPeer
    extends org.apache.fulcrum.security.impl.db.entity.BaseTurbineUserPeer
    implements org.apache.fulcrum.security.impl.db.entity.UserPeer
{
    /** The key name for the username field. */
    public static final String USERNAME = LOGIN_NAME; //"TURBINE_USER.LOGIN_NAME";

    public static Class userClass = null;

    /**
     * The class that the Peer will make instances of.
     * If the BO is abstract then you must implement this method
     * in the BO.
     *
     * !! This is duplicated. This method is available in
     * the DBSecurityService???
     */
    public static Class getOMClass()
        throws TorqueException
    {
        if ( userClass == null )
        {
            String className = TurbineSecurity.getService()
                .getConfiguration().getString("user.class",
                    "org.apache.fulcrum.security.impl.db.entity.TurbineUser");

            try
            {
                userClass = Class.forName(className);
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
        }

        return userClass;
    }

    /**
     * Returns the full name of a column.
     *
     * @return A String with the full name of the column.
     */
    public static String getColumnName(String name)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(TABLE_NAME);
        sb.append(".");
        sb.append(name);
        return sb.toString();
    }

    /**
     * Returns the full name of a column.
     *
     * @return A String with the full name of the column.
     * @deprecated use getColumnName(String name)
     */
    public String getFullColumnName(String name)
    {
        return getColumnName(name);
    }

    /**
     * Get the name of this table.
     *
     * @return A String with the name of the table.
     */
    public static String getTableName()
    {
        return TABLE_NAME;
    }

    /**
     * Checks if a User is defined in the system. The name
     * is used as query criteria.
     *
     * @param permission The User to be checked.
     * @return <code>true</code> if given User exists in the system.
     * @throws DataBackendException when more than one User with
     *         the same name exists.
     * @throws Exception, a generic exception.
     */
    public static boolean checkExists( User user )
        throws DataBackendException, Exception
    {
        Criteria criteria = new Criteria();
        criteria.addSelectColumn(USER_ID);
        criteria.add(TurbineUserPeer.USERNAME, user.getUserName());
        List results = BasePeer.doSelect(criteria);
        if (results.size() > 1)
        {
            throw new DataBackendException("Multiple users named '" +
                user.getUserName() + "' exist!");
        }
        return (results.size() == 1);
    }

    /**
     * Returns a List of all User objects.
     *
     * @return A List with all users in the system.
     * @exception Exception, a generic exception.
     */
    public static List selectAllUsers()
        throws Exception
    {
        Criteria criteria = new Criteria();
        criteria.addAscendingOrderByColumn(TurbineUserPeer.LAST_NAME);
        criteria.addAscendingOrderByColumn(TurbineUserPeer.FIRST_NAME);
        criteria.setIgnoreCase(true);
        return TurbineUserPeer.doSelect(criteria);
    }

    /**
     * Returns a List of all confirmed User objects.
     *
     * @return A List with all confirmed users in the system.
     * @exception Exception, a generic exception.
     */
    public static List selectAllConfirmedUsers()
        throws Exception
    {
        Criteria criteria = new Criteria();
        criteria.add ( User.CONFIRM_VALUE, User.CONFIRM_DATA );
        criteria.addAscendingOrderByColumn(TurbineUserPeer.LAST_NAME);
        criteria.addAscendingOrderByColumn(TurbineUserPeer.FIRST_NAME);
        criteria.setIgnoreCase(true);
        return TurbineUserPeer.doSelect(criteria);
    }
}
