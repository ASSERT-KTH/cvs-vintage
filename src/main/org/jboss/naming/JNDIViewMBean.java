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
@version $Revision: 1.4 $
*/
public interface JNDIViewMBean extends org.jboss.util.ServiceMBean
{
    // Constants -----------------------------------------------------
    public static final String OBJECT_NAME = ":service=JNDIView";
    
    // Public --------------------------------------------------------

    /** List the JBoss JNDI namespace.

    @param verbose, flag indicating if the type of object should be shown
    @param maxdepth, the maxdepth to which an given context should be listed.
    */
    public String list(boolean verbose, int maxdepth);
    /** List the JBoss JNDI namespace.

    @param verbose, flag indicating if the type of object should be shown
    @param maxdepth, the maxdepth to which an given context should be listed.
    @param mimeType: text/plain, text/html, text/xml are the currently
     supported types.
    */
    public String list(boolean verbose, int maxdepth, String mimeType);

    /**
     * List the JBoss JNDI namespace in XML Format
     **/
    public String listXML();
}
