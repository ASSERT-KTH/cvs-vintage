/*
 * @(#)IIOPRessourceWrapper.java	1.0 02/07/15
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
 */
package org.objectweb.carol.jndi.iiop;

// java import
import java.rmi.RemoteException;

import javax.naming.Reference;

/*
 * Class <code>IIOPReferenceWrapper</code> is the CAROL Remote Reference implementation. This implementation make the 
 * iiop reference wrapping to/from a remote object
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class IIOPReferenceWrapper implements IIOPRemoteReference {
    
    /**
     * <code>Reference</code> reference to wrap
     */
    protected Reference reference;	


    /**
     * constructor, export this object
     *
     * @param reference the <code>Reference</code> reference to wrap
     */
    public IIOPReferenceWrapper(Reference reference) throws RemoteException {
	super();
	this.reference = reference;
    }

    /**
     * Get the <code>Reference</code> reference 
     *
     * @return the <code>Reference</code> reference
     */
    public Reference getReference() throws RemoteException {
	return reference;
    }
}
