/**
 * JBoss, the OpenSource EJB server.
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
package org.jboss.jms.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * An abstract implementaion of {@link JMSProviderAdapter}.  Sub-classes must
 * provide connection names via instance initialzation and provide an 
 * implementaion of {@link #getInitialContext}.
 *
 * @version <pre>$id$</pre>
 * @author  Jason Dillon <a href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</a>
 */
public abstract class AbstractJMSProviderAdapter
   implements JMSProviderAdapter, java.io.Serializable
{
   /** The queue factory name to use. */
   protected String queueFactoryName;

   /** The topic factory name to use. */
   protected String topicFactoryName;

   /** The name of the provider. */
   protected String name;

   /** The provider url. */
   protected String providerURL;

   /**
    * Initialize.
    *
    * @param queueFactoryName    The name of the queue factory to use.
    * @param topicFactoryName    The name of the topic factory to use.
    */
   protected AbstractJMSProviderAdapter(final String queueFactoryName,
                                        final String topicFactoryName)
   {
      this.queueFactoryName = queueFactoryName;
      System.out.println("queue factory name: " + this.queueFactoryName);
      this.topicFactoryName = topicFactoryName;
      System.out.println("topic factory name: " + this.topicFactoryName);
   }

   /**
    * Set the name of the provider.
    *
    * @param name    The provider name.
    */
   public void setName(final String name) {
      this.name = name;
   }

   /**
    * Get the name of the provider.
    *
    * @return  The provider name.
    */
   public final String getName() {
      return name;
   }

   /**
    * Set the URL that will be used to connect to the JNDI provider.
    *
    * @param url  The URL that will be used to connect.
    */
   public void setProviderUrl(final String url) {
      this.providerURL = url;
   }

   /**
    * Get the URL that is currently being used to connect to the JNDI 
    * provider.
    *
    * @return     The URL that is currently being used.
    */
   public final String getProviderUrl() {
      return providerURL;
   }

   /**
    * Get the JNDI name of the queue factory connection to use.
    *
    * @return  JNDI name of the queue factory connection to use.
    */
   public final String getQueueFactoryName() {
      return queueFactoryName;
   }

   /**
    * Get the JNDI name of the topic factory connection to use.
    *
    * @return  JNDI name of the topic factory connection to use.
    */
   public final String getTopicFactoryName() {
      return topicFactoryName;
   }
}
