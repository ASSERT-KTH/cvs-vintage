/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.io.Serializable;

/**
 * A helper class for serializing stateful session beans.	
 *
 * Instances of this class are used to replace the non-serializable fields of StatefulSessionBean 
 * during serialization (passivation) and deserialization (activation)
 * Section 6.4.1 of the ejb1.1 specification states when this can happen.
 *      
 *	@see org.jboss.ejb.plugins.SessionObjectOutputStream, org.jboss.ejb.plugins.SessionObjectInputStream
 *	@author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.3 $
 */
class StatefulSessionBeanField implements java.io.Serializable {
   static final byte SESSION_CONTEXT = 0;
   static final byte USER_TRANSACTION = 1;

   byte type;
      
   StatefulSessionBeanField(byte type) {
      this.type = type;
   }
}
