/*
 * Created on Oct 2, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.carol.jndi.jrmp;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * @author riviereg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRMPResourceContextWrapperFactory
	implements InitialContextFactory {
		/**
		 * Get/Build the JRMP Wrapper InitialContext
		 *
		 * @param env the inital JRMP environement
		 * @return a <code>Context</code> coresponding to the inital JRMP environement with 
		 *         JRMP Serializable ressource wrapping
		 *
		 * @throws NamingException if a naming exception is encountered
		 */   
		public Context getInitialContext(Hashtable env) throws NamingException {
		return JRMPResourceContextWrapper.getSingleInstance(env);
		}
}
