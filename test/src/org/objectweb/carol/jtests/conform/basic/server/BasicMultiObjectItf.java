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
 * $Id: BasicMultiObjectItf.java,v 1.3 2005/02/08 10:03:48 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jtests.conform.basic.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class <code>BasicMultiObjectItf</code> is a basic remote interface
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 */
public interface BasicMultiObjectItf extends Remote {

    /**
     * Basic multi method
     * @return String "string"
     */
    public String getMultiString() throws RemoteException;

    /**
     * Basic multi method this method get the BasicObjectItf stub
     * @return BasicObjectItf
     */
    public BasicObjectItf getBasicObject() throws RemoteException;

    /**
     * Basic Ref String, method this method get the BasicObjectItf stub
     * @return String of the reference
     */
    public String getBasicRefString() throws RemoteException;
}