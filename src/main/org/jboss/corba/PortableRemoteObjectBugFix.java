/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.corba;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/**
 * This is a hack to associate objects with the singleton orb created by ORBFactory.
 * Otherwise Sun's Utility class throws BAD_INV_ORDER when trying to pass a non active Servant as a parameter.
 */
public class PortableRemoteObjectBugFix extends com.sun.corba.se.internal.javax.rmi.PortableRemoteObject 
{
   public void exportObject(Remote obj) throws RemoteException 
   {
	   super.exportObject(obj);
	   Tie tie = Util.getTie(obj);
      if (tie != null)
	      tie.orb(ORBFactory.getORB());
   }
}


