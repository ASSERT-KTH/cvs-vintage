/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.util;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

/**
 * AutoNumber stores autonumbers for items in a collection.
 *
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.2 $
 */
public interface AutoNumber extends EJBObject {

	/**
	 * Gets the current value of the autonumber.
	 */
	public Integer getValue() throws RemoteException;
	
	/**
	 * Sets the current value of the autonumber.
	 */
	public void setValue(Integer value) throws RemoteException;
}