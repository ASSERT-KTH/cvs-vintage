/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
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
 * Initial developer: Florent BENOIT
 * --------------------------------------------------------------------------
 * $Id: SunFixedDelegate.java,v 1.1 2004/12/13 16:24:14 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.delegate;

import java.rmi.RemoteException;

import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.Delegate;

import com.sun.corba.se.internal.iiop.ShutdownUtilDelegate;


/**
 * Allow to fix a bug of Sun JDK class.
 * @author Florent BENOIT
 */
public class SunFixedDelegate extends ShutdownUtilDelegate {

    /**
     * The <tt>isLocal</tt> method has the same semantics as the ObjectImpl._is_local
     * method, except that it can throw a RemoteException.
     * (no it doesn't but the spec says it should.)
     *
     * @param stub the stub to test.
     *
     * @return The <tt>_is_local()</tt> method returns true if
     * the servant incarnating the object is located in the same process as
     * the stub and they both share the same ORB instance.  The <tt>_is_local()</tt>
     * method returns false otherwise. The default behavior of <tt>_is_local()</tt> is
     * to return false.
     *
     * @throws RemoteException The Java to IDL specification does to
     * specify the conditions that cause a RemoteException to be thrown.
     */
    public boolean isLocal(Stub stub) throws RemoteException {
        try {
            Delegate delegate = stub._get_delegate();
            return delegate.is_local(stub);
        } catch (SystemException e) {
            throw Util.mapSystemException(e);
        }
    }


}
