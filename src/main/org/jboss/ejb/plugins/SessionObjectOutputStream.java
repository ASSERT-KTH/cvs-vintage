/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.transaction.UserTransaction;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * The SessionObjectOutputStream is used to serialize stateful session beans when they are passivated
 *      
 *	@see org.jboss.ejb.plugins.SessionObjectInputStream
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.6 $
 */
public class SessionObjectOutputStream
	extends ObjectOutputStream
{
	// Constructors -------------------------------------------------
   public SessionObjectOutputStream(OutputStream out)
      throws IOException
   {
      super(out);
      enableReplaceObject(true);
   }
      
   // ObjectOutputStream overrides ----------------------------------
   protected Object replaceObject(Object obj)
      throws IOException
   {
      // section 6.4.1 of the ejb1.1 specification states what must be taken care of 
      
      // ejb reference (remote interface) : store handle
      if (obj instanceof EJBObject)
         return ((EJBObject)obj).getHandle();
      
      // ejb reference (home interface) : store handle
      else if (obj instanceof EJBHome)
	      return ((EJBHome)obj).getHomeHandle();
      
      // session context : store a typed dummy object
      else if (obj instanceof SessionContext)
         return new StatefulSessionBeanField(StatefulSessionBeanField.SESSION_CONTEXT);

      // naming context : the jnp implementation is serializable, do nothing

      // user transaction : store a typed dummy object
      else if (obj instanceof UserTransaction)
         return new StatefulSessionBeanField(StatefulSessionBeanField.USER_TRANSACTION);      
          
      return obj;
   }
}
