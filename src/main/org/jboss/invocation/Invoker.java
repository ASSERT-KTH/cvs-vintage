/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.rmi.Remote;

import org.jboss.proxy.Interceptor;

import org.jboss.util.id.GUID;

/**
 * This invoker carries Invocation in the JMX target node.
 * 
 * <p>
 * The interface in the current JBoss can be implemented with Remote/local switches or
 * with clustered invokers, this interface just masks the network details and the topology
 * of the JMX nodes for the client proxies. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.5 $
 * 
 * <p><b>Revisions:</b>
 *
 * <p><b>20011114 marc fleury:</b>
 * <ul>
 *   <li>Initial check-in
 * </ul>
 */
public interface Invoker
   extends Remote
{    
   /**
    * A globaly unique identifier use to determine if an instance is local
    * to the invoker.
    */
   GUID ID = new GUID();
   
   /**
    * The <code>getServerID</code> method returns a ServerID
    * object describing the server location, and enabling construction
    * of ObjectNames specific to clients of that server.
    *
    * @return a <code>ServerID</code> value
    * @exception Exception if an error occurs
    */
   ServerID getServerID() throws Exception;
   
   /**
    * The invoke with an Invocation Object.
    * 
    * <p>
    * the delegate can handle network protocols on behalf of proxies (proxies delegate to these 
    * puppies). We provide default implemenations with JRMP/Local/Clustered invokers.
    * The delegates are not tied to a type of invocation (EJB or generic RMI).
    *
    * @param invocation    A pointer to the invocation object
    * @return              Return value of method invocation.
    * 
    * @throws Exception    Failed to invoke method.
    */
   InvocationResponse invoke(Invocation invocation) throws Exception;
}
