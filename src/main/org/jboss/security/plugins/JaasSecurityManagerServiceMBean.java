/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security.plugins;

/** The interface for the JaasSecurityManagerService mbean.
*/
public interface JaasSecurityManagerServiceMBean
    extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=JaasSecurityManager";
    
   // Public --------------------------------------------------------
   /** Get the name of the class that provides the security manager implementation.
    */
    public String getSecurityManagerClassName();
   /** Set the name of the class that provides the security manager implementation.
    */
    public void setSecurityManagerClassName(String className) throws ClassNotFoundException;
    /** Get the name of the class that provides the SecurityProxyFactory implementation.
     */
    public String getSecurityProxyFactoryClassName();
    /** Set the name of the class that provides the SecurityProxyFactory implementation.
     */
    public void setSecurityProxyFactoryClassName(String className) throws ClassNotFoundException;
   /** Get the jndi name under which the authentication CachePolicy implenentation
       is found
    */
    public String getAuthenticationCacheJndiName();
   /** Set the jndi name under which the authentication CachePolicy implenentation
       is found
    */
    public void setAuthenticationCacheJndiName(String jndiName);
}
