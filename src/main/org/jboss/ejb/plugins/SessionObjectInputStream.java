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
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
class SessionObjectInputStream
	extends ObjectInputStream
{
	StatefulSessionEnterpriseContext ctx;

	// Constructors -------------------------------------------------
	public SessionObjectInputStream(StatefulSessionEnterpriseContext ctx, InputStream in)
      throws IOException
   {
      super(in);
      enableResolveObject(true);
		
		this.ctx = ctx;
   }
      
   // ObjectInputStream overrides -----------------------------------
   protected Object resolveObject(Object obj)
      throws IOException
   {
      if (obj instanceof Handle)
         return ((Handle)obj).getEJBObject(); // Resolve handle to EJB
      else if (obj instanceof HomeHandle)
         return ((HomeHandle)obj).getEJBHome(); // Resolve handle to EJB Home
		else if (obj instanceof SessionContext)	
         return ctx.getSessionContext(); // Resolve session context of this instance
      return obj;
   }
}
