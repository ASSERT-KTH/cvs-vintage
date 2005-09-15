/**
 * Copyright (C) 2005 - Bull S.A.
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: LocalEnumeration.java,v 1.1 2005/09/15 12:53:23 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.naming;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


/**
 * Local enumeration for local context
 * @author Florent Benoit (Refactoring)
 */
public class LocalEnumeration implements NamingEnumeration {

    /**
     * Initial context
     */
    private Context localContext;

    /**
     * List of names
     */
    private final String[] names;

    /**
     * Index of names
     */
    private int nextName;

    /**
     * Default constructor
     * @param ctx given context
     * @param names names to enumerate
     */
    public LocalEnumeration(Context ctx, String[] names) {
        this.localContext = ctx;
        this.names = names;
        nextName = 0;
    }

    /**
     * Enumeration is finished ?
     * @return true is this is finished
     */
    public boolean hasMore() {
        return (nextName < names.length);
    }

    /**
     * @return Next object of the enumeration
     * @throws NamingException if compositeName object cannot be built
     */
    public Object next() throws NamingException {
        if (!hasMore()) {
            throw (new java.util.NoSuchElementException());
        }
        String name = names[nextName++];
        Name cname = (new CompositeName()).add(name);

        Object obj = localContext.lookup(cname);
        return (new Binding(cname.toString(), obj));
    }

    /**
     * Enumeration is finished ?
     * @return true is this is finished
     */
    public boolean hasMoreElements() {
        return hasMore();
    }

    /**
     * @return Next object of the enumeration
     */
    public Object nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw new java.util.NoSuchElementException(e.toString());
        }
    }

    /**
     * Close the enumeration
     */
    public void close() {
    }

}