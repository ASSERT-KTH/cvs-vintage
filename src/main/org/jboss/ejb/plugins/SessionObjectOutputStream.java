/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
class SessionObjectOutputStream
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
      if (obj instanceof EJBObject)
         return ((EJBObject)obj).getHandle();
      else if (obj instanceof EJBHome)
	      return ((EJBHome)obj).getHomeHandle();
         
      return obj;
   }
}
