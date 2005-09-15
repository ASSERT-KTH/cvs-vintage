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
 * $Id: SimpleNameParser.java,v 1.1 2005/09/15 12:53:23 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.naming;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

/**
 * A very simple Compound Name parser
 * @author Florent Benoit (Refactoring)
 */
public class SimpleNameParser implements NameParser {

    /**
     * Default syntax
     */
    private static final Properties SYNTAX = new Properties();

    /**
     * Parses a name into its components.
     * @param name The non-null string name to parse.
     * @return A non-null parsed form of the name using the naming convention of
     *         this parser.
     * @exception NamingException If a naming exception was encountered.
     */
    public Name parse(String name) throws NamingException {
        return (new CompoundName(name, SYNTAX));
    }
}