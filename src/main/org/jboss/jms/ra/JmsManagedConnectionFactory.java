/*
 * Copyright (c) 2001 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.ra;

import java.util.Set;
import java.util.Iterator;

import java.io.PrintWriter;

import javax.security.auth.Subject;

import javax.resource.ResourceException;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.SecurityException;
import javax.resource.spi.IllegalStateException;

import javax.resource.spi.security.PasswordCredential;

import org.jboss.jms.jndi.JMSProviderAdapter;

/**
 * JmsManagedConnectionFactory.java
 *
 *
 * Created: Sat Mar 31 03:08:35 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.3 $
 */
public class JmsManagedConnectionFactory
   implements ManagedConnectionFactory
{
   private PrintWriter logWriter = null;
   private String providerJNDI;
   private JmsLogger logger = new JmsLogger();

   //For local access
   private JMSProviderAdapter adapter;
   
   public JmsManagedConnectionFactory() {
   }
   
   /**
    * Create a "non managed" connection factory. No appserver involved
    */
   public Object createConnectionFactory() throws ResourceException 
   {
      return new JmsConnectionFactoryImpl(this, null);
   }

   /**
    * Create a ConnectionFactory with appserver hook
    */ 
   public Object createConnectionFactory(ConnectionManager cxManager)
      throws ResourceException 
   {
      return new JmsConnectionFactoryImpl(this, cxManager);
   }
    
   /**
    * Create a new connection to manage in pool
    */
   public ManagedConnection createManagedConnection(Subject subject, 
                                                    ConnectionRequestInfo info) 
      throws ResourceException 
   {
      JmsCred cred = JmsCred.getJmsCred(this,subject, info);
      // OK we got autentication stuff
      JmsManagedConnection mc = new JmsManagedConnection
         (this, info,cred.name, cred.pwd);
      // Set default logwriter according to spec
      mc.setLogWriter(logWriter);
      return mc;

   }

   /**
    * Match a set of connections from the pool
    */
   public ManagedConnection matchManagedConnections(Set connectionSet,
                                                    Subject subject,
                                                    ConnectionRequestInfo info) 
      throws ResourceException
   {
      // Get cred
      JmsCred cred = JmsCred.getJmsCred(this,subject, info);

      // Traverse the pooled connections and look for a match, return
      // first found
      Iterator connections = connectionSet.iterator();
      while (connections.hasNext()) {
         Object obj = connections.next();
	    
         // We only care for connections of our own type
         if (obj instanceof JmsManagedConnection) {
            // This is one from the pool
            JmsManagedConnection mc = (JmsManagedConnection) obj;
		
            // Check if we even created this on
            ManagedConnectionFactory mcf =
               mc.getManagedConnectionFactory();
		
            // Only admit a connection if it has the same username as our
            // asked for creds
            // FIXME, Here we have a problem, jms connection 
            // may be anonymous, have a user name
		
            if (
                (mc.getUserName() == null || 
                 (mc.getUserName() != null && 
                  mc.getUserName().equals(cred.name))
                 ) &&
                mcf.equals(this)) {
               // Now check if ConnectionInfo equals
               if (info.equals( mc.getInfo() )) {

                  return mc;
               }
            }
         }
      }
      return null;
   }

   /**
    * 
    */
   public void setLogWriter(PrintWriter out)
      throws ResourceException {
      this.logWriter = out;
      logger.setLogWriter(out);
   }
   
   /**
    * 
    */
   public PrintWriter getLogWriter() throws ResourceException {
      return logWriter;    
   }

   // FIXME, what should be the unique identity of a ConnectionFactory for
   // JMS, hard do say actually

   public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj instanceof JmsManagedConnectionFactory) {
         String you = ((JmsManagedConnectionFactory) obj).
            getJmsProviderAdapterJNDI();
         String me = this.providerJNDI;
         return (you == null) ? (me == null) : (you.equals(me));
      } else {
         return false;
      }
   }

   public int hashCode() {
      if (providerJNDI == null) {
         return (new String("")).hashCode();
      } else {
         return providerJNDI.hashCode();
      }
   }

   // --- Connfiguration API ---
   public void setJmsProviderAdapterJNDI(String jndi) {
      providerJNDI = jndi;
   }
    
   public String getJmsProviderAdapterJNDI() {
      return providerJNDI;
   }

   /**
    * For local access
    */
   public void setJmsProviderAdapter(JMSProviderAdapter adapter) {
      this.adapter = adapter;
   }
    
   public JMSProviderAdapter getJmsProviderAdapter() {
      return adapter;
   }
   
} // JmsManagedConnectionFactory
