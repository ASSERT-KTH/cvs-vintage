package org.apache.fulcrum.security.impl.db.entity;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.impl.db.DBSecurityService;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

/**
 * This class handles all the database access for the ROLE table.
 * This table contains all the roles that a given member can play.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: TurbineRolePeer.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class TurbineRolePeer
    extends org.apache.fulcrum.security.impl.db.entity.BaseTurbineRolePeer
{
    /** The column name for the name field. */
    public static final String NAME = ROLE_NAME;

    /**
     * Checks if a Role is defined in the system. The name
     * is used as query criteria.
     *
     * @param permission The Role to be checked.
     * @return <code>true</code> if given Role exists in the system.
     * @throws DataBackendException when more than one Role with
     *         the same name exists.
     * @throws Exception, a generic exception.
     */
    public static boolean checkExists(Role role)
        throws DataBackendException, Exception
    {
        Criteria criteria = new Criteria();
        criteria.addSelectColumn(ROLE_ID);
        criteria.add(NAME, role.getName());
        List results = BasePeer.doSelect(criteria);
        if (results.size() > 1)
        {
            throw new DataBackendException("Multiple roles named '" +
                role.getName() + "' exist!");
        }
        return (results.size() == 1);
    }

    /**
     * Returns the full name of a column.
     *
     * @return A String with the full name of the column.
     */
    public static String getColumnName(String name)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(TurbineRolePeer.TABLE_NAME);
        sb.append(".");
        sb.append(name);
        return sb.toString();
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
     * Retrieves/assembles a RoleSet based on the Criteria passed in
     */
    public static RoleSet retrieveSet(Criteria criteria) throws Exception
    {
        List results = doSelect(criteria);
        RoleSet rs = new RoleSet();
        for (int i=0; i<results.size(); i++)
        {
            rs.add( (Role)results.get(i) );
        }
        return rs;
    }

    /**
     * Retrieves a set of Roles that an User was assigned in a Group
     *
     * @param user An user.
     * @param group A group
     * @return A Set of Roles of this User in the Group
     * @exception Exception, a generic exception.
     */
    public static RoleSet retrieveSet(User user, Group group)
        throws Exception
    {
        Criteria criteria = new Criteria();

        /*
         * Peer specific methods should absolutely NOT be part
         * of any of the generic interfaces in the security system.
         * this is not good.
         *
         * UserPeer up = TurbineSecurity.getUserPeerInstance();
         */
        UserPeer up = ((DBSecurityService)TurbineSecurity.getService())
            .getUserPeerInstance();

        criteria.add(TurbineUserPeer.USERNAME, user.getUserName());
        criteria.add(TurbineUserGroupRolePeer.GROUP_ID,
                     ((Persistent)group).getPrimaryKey());

        criteria.addJoin(TurbineUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
        criteria.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePeer.ROLE_ID);
        return retrieveSet(criteria);
    }

}
