/**
 * @(#) BasicObjectRef.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 *
 */
package org.objectweb.carol.jtests.conform.basic.server;

// java import
import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * Class <code>BasicObjectRef</code> is a Basic referenceable (and
 * serializable) class for testing
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class BasicObjectRef implements Referenceable, Serializable {

    /**
     * String object content
     */
    String content = "string";

    /**
     * Constructor
     */
    public BasicObjectRef(String content) throws Exception {
        this.content = content;
    }

    /**
     * get Reference methode
     */
    public Reference getReference() throws NamingException {
        return new Reference(BasicObjectRef.class.getName(), new StringRefAddr("content", "string2"),
                BasicObjectRefFactory.class.getName(), null);
    }

    /**
     * to String method
     * @return String content
     */
    public String toString() {
        return content;
    }

}