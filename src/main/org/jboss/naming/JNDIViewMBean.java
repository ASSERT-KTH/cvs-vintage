/*
 * JBoss, the OpenSource J2EE webOS
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
@version $Revision: 1.8 $
*/
public interface JNDIViewMBean extends org.jboss.system.ServiceMBean
{
    // Constants -----------------------------------------------------
    public static final String OBJECT_NAME = "jboss:service=JNDIView";
    
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
