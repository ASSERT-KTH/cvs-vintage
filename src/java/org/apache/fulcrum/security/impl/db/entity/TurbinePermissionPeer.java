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

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.SecurityEntity;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.torque.om.BaseObject;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

/**
 * This class handles all the database access for the PERMISSION
 * table.  This table contains all the permissions that are used in
 * the system.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: TurbinePermissionPeer.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class TurbinePermissionPeer
    extends org.apache.fulcrum.security.impl.db.entity.BaseTurbinePermissionPeer
{
    /** The column name for the name field. */
    public static final String NAME = PERMISSION_NAME;

    /**
     * Checks if a Permission is defined in the system. The name
     * is used as query criteria.
     *
     * @param permission The Permission to be checked.
     * @return <code>true</code> if given Permission exists in the system.
     * @throws DataBackendException when more than one Permission with
     *         the same name exists.
     * @throws Exception, a generic exception.
     */
    public static boolean checkExists( Permission permission )
        throws DataBackendException, Exception
    {
        Criteria criteria = new Criteria();
        criteria.addSelectColumn(PERMISSION_ID);
        criteria.add(NAME, ((SecurityEntity)permission).getName());
        List results = BasePeer.doSelect(criteria);
        if (results.size() > 1)
        {
            throw new DataBackendException("Multiple permissions named '" +
                ((SecurityEntity)permission).getName() + "' exist!");
        }
        return (results.size()==1);
    }

    /**
     * Returns the full name of a column.
     *
     * @return A String with the full name of the column.
     */
    public static String getColumnName (String name)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(TurbinePermissionPeer.TABLE_NAME);
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
     * Retrieves/assembles a PermissionSet
     *
     * @param criteria The criteria to use.
     * @return A PermissionSet.
     * @exception Exception, a generic exception.
     */
    public static PermissionSet retrieveSet(Criteria criteria)
        throws Exception
    {
        List results = doSelect(criteria);
        PermissionSet ps = new PermissionSet();
        for (int i=0; i<results.size(); i++)
        {
            ps.add( (Permission)results.get(i) );
        }
        return ps;
    }

    /**
     * Retrieves a set of Permissions associated with a particular Role.
     *
     * @param role The role to query permissions of.
     * @return A set of permissions associated with the Role.
     * @exception Exception, a generic exception.
     */
    public static PermissionSet retrieveSet( Role role )
        throws Exception
    {
        Criteria criteria = new Criteria();
        criteria.add(TurbineRolePermissionPeer.ROLE_ID,
                     ((TurbineRole)role).getPrimaryKey());
        criteria.addJoin(TurbineRolePermissionPeer.PERMISSION_ID,
                         TurbinePermissionPeer.PERMISSION_ID);
        return retrieveSet(criteria);
    }

    /**
     * Pass in two Vector's of Permission Objects.  It will return a
     * new Vector with the difference of the two Vectors: C = (A - B).
     *
     * @param some Vector B in C = (A - B).
     * @param all Vector A in C = (A - B).
     * @return Vector C in C = (A - B).
     */
    public static final Vector getDifference(Vector some, Vector all)
    {
        Vector clone = (Vector)all.clone();
        for (Enumeration e = some.elements() ; e.hasMoreElements() ;)
        {
            Permission tmp = (Permission) e.nextElement();
            for (Enumeration f = clone.elements() ; f.hasMoreElements() ;)
            {
                Permission tmp2 = (Permission) f.nextElement();
                if (((BaseObject)tmp).getPrimaryKey() ==
                    ((BaseObject)tmp2).getPrimaryKey())
                {
                    clone.removeElement(tmp2);
                    break;
                }
            }
        }
        return clone;
    }

}
