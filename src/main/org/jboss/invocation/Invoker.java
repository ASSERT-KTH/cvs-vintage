/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.rmi.Remote;

/**
*   This invoker carries Invocation in the JMX target node.
*   The interface in the current JBoss can be implemented with Remote/local switches or
*   with clustered invokers, this interface just masks the network details and the topology
*   of the JMX nodes for the client proxies. 
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.1 $
*   Revisions:
*
*   <p><b>Revisions:</b>
*
*   <p><b>20011114 marc fleury:</b>
*   <ul>
*   <li> Initial check-in
*   </ul>
*/


public interface Invoker
extends Remote
{    
   
   // Public --------------------------------------------------------
  
  /**
   * The time when this class was initialized. Used to 
   * determine if this instance lives in the same VM as the container.
   */
   long STARTUP = System.currentTimeMillis();

   /**
   * A free form String identifier for this delegate invoker, can be clustered or target node
   * This should evolve in a more advanced meta-inf object
   */
   public String getServerHostName()
   throws Exception;
   
   /**
   * The invoke with an Invocation Object 
   * the delegate can handle network protocols on behalf of proxies (proxies delegate to these puppies)
   *  We provide default implemenations with JRMP/Local/Clustered invokers.
   *  The delegates are not tied to a type of invocation (EJB or generic RMI).
   *
   * @param invocation  A pointer to the invocation object
   *     
   * @return      Return value of method invocation.
   * 
   * @throws Exception    Failed to invoke method.
   */
   public Object invoke(Invocation invocation)
   throws Exception;
}