package org.jboss.proxy;



import java.io.*;

import javax.ejb.EJBHome;

import javax.ejb.Handle;

import javax.ejb.EJBObject;



public class ProxyProxy implements Serializable, EJBObject

{

  InvocationHandler handler;

  String[] targetNames;



  public ProxyProxy( InvocationHandler handler, Class[] targetTypes )

  {
    this.handler = handler;

    targetNames = new String[ targetTypes.length ];

    for (int iter=0; iter<targetTypes.length; iter++)

    {

      targetNames[iter] = targetTypes[iter].getName();

    }

  }



  private Class[] getClasses()

  {

    try

    {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class[] classes = new Class[targetNames.length];

      for (int iter=0; iter<targetNames.length; iter++)

      {

        classes[iter] = cl.loadClass( targetNames[iter] );

      }

      return classes;

    }

    catch (Exception e)

    {

      e.printStackTrace();

      return null;

    }

  }



  public Object readResolve() throws ObjectStreamException

  {
    return Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
									        getClasses(), handler );

  }



	public EJBHome getEJBHome() throws java.rmi.RemoteException

	{

		throw new UnsupportedOperationException();

	}



	public Handle getHandle() throws java.rmi.RemoteException

	{

		throw new UnsupportedOperationException();

	}



	public Object getPrimaryKey() throws java.rmi.RemoteException

	{

		throw new UnsupportedOperationException();

	}



	public boolean isIdentical(EJBObject parm1) throws java.rmi.RemoteException

	{

		throw new UnsupportedOperationException();

	}



	public void remove() throws java.rmi.RemoteException, javax.ejb.RemoveException

	{

		throw new UnsupportedOperationException();

	}

}



