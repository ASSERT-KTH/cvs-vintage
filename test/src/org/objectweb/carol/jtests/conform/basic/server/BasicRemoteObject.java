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
 * $Id: BasicRemoteObject.java,v 1.1 2005/03/09 18:12:37 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.server;

import java.io.Serializable;
import java.rmi.Remote;


/**
 * Dummy Remote object
 * @author Florent Benoit
 */
public class BasicRemoteObject implements Remote, Serializable {

    /**
     * Name of the object
     */
    private String name = null;

    /**
     * Build a new object
     * @param name the name of this object
     */
    public BasicRemoteObject(String name) {
        this.name = name;
    }

    /**
     * Equals method
     * @param o object to compare
     * @return true if it is the same value
     */
    public boolean equals(Object o) {
        if (!(o instanceof BasicRemoteObject)) {
            return false;
        }
        return getName().equals(((BasicRemoteObject) o).getName());
    }

    /**
     * @return hashcode of this object
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @return Returns the name.
     */
    protected String getName() {
        return name;
    }


}
