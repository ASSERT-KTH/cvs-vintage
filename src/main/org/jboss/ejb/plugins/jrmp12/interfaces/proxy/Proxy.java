package org.jboss.ejb.jrmp12.interfaces.proxy;



public class Proxy

{



  public static Object newProxyInstance(ClassLoader loader, Class[] interfaces,

  	InvocationHandler h)

  {

  	Class[] interfaces2 = new Class[ interfaces.length+2 ];

    interfaces2[interfaces2.length-2] = java.io.Serializable.class;

    interfaces2[interfaces2.length-1] = Replaceable.class;

    for (int iter=0; iter<interfaces.length; iter++)

    {

    	interfaces2[iter] = interfaces[iter];

    }



 		return Proxies.newTarget( loader, h, interfaces2 );

  }

}

