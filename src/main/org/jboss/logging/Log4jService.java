/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

/** This is a JMX MBean that provides two features:
1., It initalizes the log4j framework from the log4j properties format file
    specified by the ConfigurationPath attribute to that the log4j may be
    used by JBoss components.
2., It collects JMX notification events fired by the "service=Log" mbean
    and logs the msgs to the log4j root Category. This allows the Log4jService
    to replace all other JBoss logging services like ConsoleLogging and
    FileLogging.

@author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
@author Scott_Stark@displayscape.com
@version $Revision: 1.2 $
*/
public class Log4jService implements Log4jServiceMBean, NotificationListener,
    MBeanRegistration
{

// Attributes ----------------------------------------------------
    private Category cat;
    private String configurationPath;
    private int refreshPeriod;
    private boolean refreshFlag;

// Constructors --------------------------------------------------
    public Log4jService()
    {
        this("log4j.properties", 60);
    }
    public Log4jService(String path)
    {
        this(path, 60);
    }
    /**
    @param path, the path to the log4j.properties format file
    @param refreshPeriod, the refreshPeriod in seconds to wait between each check.
    */
    public Log4jService(String path, int refreshPeriod)
    {
        this.configurationPath = path;
        this.refreshPeriod = refreshPeriod;
        this.refreshFlag = true;
    }

    /** Get the log4j.properties format config file path
    */
    public String getConfigurationPath()
    {
        return configurationPath;
    }
    /** Set the log4j.properties format config file path
    */
    public void setConfigurationPath(String path)
    {
        this.configurationPath = path;
    }
    /** Get the refresh flag. This determines if the log4j.properties file
        is reloaded every refreshPeriod seconds or not.
    */
    public boolean getRefreshFlag()
    {
        return refreshFlag;
    }
    /** Set the refresh flag. This determines if the log4j.properties file
        is reloaded every refreshPeriod seconds or not.
    */
    public void setRefreshFlag(boolean flag)
    {
        this.refreshFlag = flag;
    }
    /** Configures the log4j framework using the current service properties
        and sets the service category to the log4j root Category. This method
        throws a FileNotFoundException exception if the current
        configurationPath cannot be located to avoid interaction problems
        between the log4j framework and the JBoss ConsoleLogging service.
    */
    public void start() throws Exception
    {
        // Make sure the config file can be found
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(configurationPath);
        if( url == null )
            throw new FileNotFoundException("Failed to find logj4 props: "+configurationPath);
        if( refreshFlag )
        {
            // configurationPath is a file path
            String path = url.getFile();
            PropertyConfigurator.configureAndWatch(path, 1000*refreshPeriod);
        }
        else
        {
            PropertyConfigurator.configure(url);
        }
        cat = Category.getRoot();
        cat.info("Started Log4jService, config="+url);
    }
    /** Stops the log4j framework by calling the Category.shutdown() method.
    @see org.apache.log4j.Category#shutdown()
    */
    public void stop()
    {
        Category.shutdown();
        if( cat != null )
            cat.info("Stopped Log4jService");
    }

// Public --------------------------------------------------------
    /** This method recevies JMX notification events posted via a Logger
        instances fireNotification method and logs the msg through the
        log4j root Category.

    @param n, the log event. This provides the log source as n.getUserData(),
        the log msg as n.getMessage(), and the type of message from n.getType().
    @see org.jboss.logging.Logger#fireNotification(String, Object, String)
    */
    public void handleNotification(Notification n, Object handback)
    {
        String msg = n.getMessage();
        char type = n.getType().charAt(0);
        String source = (String) n.getUserData();
        if( source == null || source.length() == 0 )
            source = "Default";
        NDC.push(source);
        switch( type )
        {
            case 'W':
                cat.warn(msg);
            break;
            case 'D':
                cat.debug(msg);
            break;
            case 'E':
                cat.error(msg);
            break;
            default:
                cat.info(msg);
            break;
        }
        NDC.pop();
    }

// --- Begin MBeanRegistration interface methods
    /** Initializes the MBean by registering as a addNotificationListener of the
        Log service and then invokes start() to configure the log4j framework.
    @return the name of this mbean.
    */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
    {
        start();
        // Receive notification events sent by the Logger mbean
        ObjectName logger = new ObjectName(server.getDefaultDomain(),"service","Log");
        server.addNotificationListener(logger,this,null,null);
        return name == null ? new ObjectName(OBJECT_NAME) : name;
    }
    public void postRegister(Boolean b)
    {
    }
    public void preDeregister()
    {
    }
    public void postDeregister()
    {
    }
// --- End MBeanRegistration interface methods
}
