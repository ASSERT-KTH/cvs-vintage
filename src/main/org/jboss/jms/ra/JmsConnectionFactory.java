/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.jms.ra;

import javax.jms.TopicConnectionFactory;
import javax.jms.QueueConnectionFactory;

/**
 * JmsConnectionFactory.java
 *
 * <p>Created: Thu Apr 26 17:01:35 2001
 *
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version <pre>$Revision: 1.1 $</pre>
 */
public interface JmsConnectionFactory 
   extends TopicConnectionFactory, QueueConnectionFactory
{
   // empty
}
