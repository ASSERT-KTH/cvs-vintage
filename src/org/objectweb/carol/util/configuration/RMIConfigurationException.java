/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: RMIConfigurationException.java,v 1.3 2005/03/11 13:57:53 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.configuration;

/**
 * Class <code>RmiConfigurationException</code> throw when there is a problem
 * in the carol configuration
 * @author Guillaume Riviere
 */
public class RMIConfigurationException extends Exception {

    /**
     * constructor with a the detail message
     * @param s message of exception
     */
    public RMIConfigurationException(String s) {
        super(s);
    }

    /**
     * constructor with a the detail message
     * @param s message of exception
     * @param e original exception
     */
    public RMIConfigurationException(String s, Exception e) {
        super(s, e);
    }

}