/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.IOException;
import javax.naming.NamingException;

/**
 *   
 *      
@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public interface ExternalContextMBean extends org.jboss.util.ServiceMBean
{
    // Constants -----------------------------------------------------
    public static final String OBJECT_NAME = ":service=ExternalContext";
    
    // Public --------------------------------------------------------

    /** Set the jndi name under which the external context is bound.
    */
    public String getJndiName();
    /** Set the jndi name under which the external context is bound.
    */
    public void setJndiName(String jndiName);

    /** Set the jndi.properties information for the external InitialContext.
    This is either a URL string or a classpath resource name. Examples:
        file:///config/myldap.properties
        http://config.mycompany.com/myldap.properties
        /conf/myldap.properties
        myldap.properties

    @param contextPropsURL, either a URL string to a jndi.properties type of
        content or a name of a resource to locate via the current thread
        context classpath.
    @throws IOException, thrown if the url/resource cannot be loaded.
    */
    public void setProperties(String contextPropsURL) throws IOException;
}
