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
@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.3 $
*/
public interface JNDIViewMBean extends org.jboss.util.ServiceMBean
{
    // Constants -----------------------------------------------------
    public static final String OBJECT_NAME = ":service=JNDIView";
    
    // Public --------------------------------------------------------

    /** List the JBoss JNDI namespace.

    @param verbose, 
    */
    public String list(boolean verbose);

    /**
     * List the JBoss JNDI namespace in XML Format
     **/
    public String listXML();
}
