/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import java.security.Principal;

import javax.transaction.Transaction;

/**
 *	MethodInvocation
 *
 *  This Serializable object carries the method to invoke and an identifier for the target ojbect
 *
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:Richard.Monson-Haefel@jGuru.com">Richard Monson-Haefel</a>.
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @author <a href="mailto:docodan@nycap.rr.com">Daniel O'Connor</a>.
 *	@version $Revision: 1.8 $
 */
public final class RemoteMethodInvocation
   implements java.io.Externalizable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Object id;
   String className;
   int hash;
	
   Object[] args;
	
	Transaction tx;
	Principal identity;
   Object credential;
	
	transient Map methodMap;

   // Static --------------------------------------------------------

   /*
   * The use of hashCode is not enough to differenciate methods
   * we override the hashCode
   *
   * This is taken from the RMH code in EJBoss 0.9
   *
   */
   public static int calculateHash(Method method) {

   	int hash =
       	// We use the declaring class
   		method.getDeclaringClass().getName().hashCode() ^ //name of class
            // We use the name of the method
   		method.getName().hashCode(); //name of method

   	Class[] clazz = method.getParameterTypes();

   	for (int i = 0; i < clazz.length; i++) {

   		 // XOR
   		 // We use the constant because
   		 // a^b^b = a (thank you norbert)
   		 // so that methodA() hashes to methodA(String, String)

   		 hash = (hash +20000) ^ clazz[i].getName().hashCode();
   	}

   	return hash;
   }
	
   // Constructors --------------------------------------------------
   public RemoteMethodInvocation()
   {
      // For externalization to work
   }
   
   public RemoteMethodInvocation(Method m, Object[] args)
   {
      this(null, m, args);
   }

   public RemoteMethodInvocation(Object id, Method m, Object[] args)
   {
      this.id = id;
      this.args = args;
		this.hash = calculateHash(m);
	   this.className = m.getDeclaringClass().getName();
   }
	
   // Public --------------------------------------------------------


   public Object getId() { return id; }

   public Method getMethod()
   {
		return (Method)methodMap.get(new Integer(hash));
   }

   public Object[] getArguments()
   {
      return args;
   }
	
	public void setMethodMap(Map methods)
	{
		methodMap = methods;
	}
	
	public void setTransaction(Transaction tx)
	{
		this.tx = tx;
	}
	
	public Transaction getTransaction()
	{
		return tx;
	}

	public void setPrincipal(Principal identity)
	{
		this.identity = identity;
	}

	public Principal getPrincipal()
	{
		return identity;
	}

  public Object getCredential()
  {
    return credential;
  }

  public void setCredential( Object credential )
  {
    this.credential = credential;
  }
	 
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
   	out.writeObject(id);
		out.writeUTF(className);
		out.writeInt(hash);
		out.writeObject(args);
		out.writeObject(tx);
		out.writeObject(identity);
    out.writeObject(credential);
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
   	id = in.readObject();
		className = in.readUTF();
		hash = in.readInt();
		args = (Object[])in.readObject();
		
		tx = (Transaction)in.readObject();
		identity = (Principal)in.readObject();
    credential = in.readObject();
   }

   // Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}

