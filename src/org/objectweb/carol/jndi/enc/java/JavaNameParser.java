/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 1999-2005 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: JavaNameParser.java,v 1.1 2005/03/10 16:50:22 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.enc.java;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

/**
 * Basic name parser used for java:comp naming space
 */
public class JavaNameParser implements NameParser {

    /**
     * New syntax
     */
    private static Properties syntax = new Properties();

    static {
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", "/");
        syntax.put("jndi.syntax.ignorecase", "false");
    }

    /**
     * Parse a name into its components.
     * @param name The non-null string name to parse.
     * @return A non-null parsed form of the name using the naming convention of
     *         this parser.
     * @exception NamingException If a naming exception was encountered.
     */
    public Name parse(String name) throws NamingException {
        return new CompoundName(name, syntax);
    }

}