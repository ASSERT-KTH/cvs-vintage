/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/** Management interface for log4j

@author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public interface Log4jServiceMBean
{
    /** The default name of the service */
    public static final String OBJECT_NAME = ":service=Logging,type=Log4J";

    /** Get the log4j.properties format config file path
    */
    public String getConfigurationPath();
    /** Set the log4j.properties format config file path
    */
    public void setConfigurationPath(String path);
    /** Get the refresh flag. This determines if the log4j.properties file
        is reloaded every refreshPeriod seconds or not.
    */
    public boolean getRefreshFlag();
    /** Set the refresh flag. This determines if the log4j.properties file
        is reloaded every refreshPeriod seconds or not.
    */
    public void setRefreshFlag(boolean flag);
    /** Configures the log4j framework using the current service properties
        and sets the service category to the log4j root Category.
    */
    public void start() throws Exception;
    /** Stops the log4j framework by calling the Category.shutdown() method.
    @see org.apache.log4j.Category#shutdown()
    */
    public void stop();
}
