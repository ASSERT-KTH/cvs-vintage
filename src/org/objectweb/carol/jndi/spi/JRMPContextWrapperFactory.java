/*
 * Created on Oct 2, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.carol.jndi.ns.JRMPRegistry;

/**
 * @author riviereg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRMPContextWrapperFactory implements InitialContextFactory {
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
		if (JRMPRegistry.isLocal()) {
			return JRMPLocalContext.getSingleInstance(
				JRMPRegistry.registry,
				env);
		} else {
			return JRMPContext.getSingleInstance(env);
		}
	}
}
