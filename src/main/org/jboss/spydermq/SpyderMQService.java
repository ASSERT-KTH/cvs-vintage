/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.spydermq;

import java.io.File;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.lang.reflect.Method;

import javax.management.*;

import org.jboss.logging.Logger;
import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 * JMX MBean implementation for SpyderMQ.
 *      
 *   @see <related>
 *   @author Vincent Sheffer (vsheffer@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class SpyderMQService
   extends ServiceMBeanSupport
   implements SpyderMQServiceMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

    MBeanServer mBeanServer = null;
    Object spyderMQServer = null;

    public SpyderMQService() {
    }
    
    // Public --------------------------------------------------------
    public ObjectName getObjectName(MBeanServer server, ObjectName name)
        throws javax.management.MalformedObjectNameException {
        this.mBeanServer = server;
        return new ObjectName(OBJECT_NAME);
    }
    
    public String getName() {
        return "SpyderMQ";
    }
    
    public void startService()
        throws Exception {
        if (spyderMQServer == null) {
            final Log log = this.log;
            try {
                Class [] spyderMQArgsClasses = null;
                Method startMethod = null;
                Object [] spyderMQArgs = null;
                
                log.log("Testing if SpyderMQ is present....");
                try {
                    spyderMQServer = Class.forName("org.spydermq.server.StartServer").newInstance(); 
                    log.log("OK");
                }catch(Exception e) {
                    log.log("failed");
                    log.log("SpyderMQ wasn't found. Be sure to have your CLASSPATH correctly set");
                    log.exception(e);
                    return;
                } 
                
                spyderMQArgsClasses = new Class[1];
                spyderMQArgsClasses[0] = mBeanServer.getClass();
                startMethod = spyderMQServer.getClass().getMethod("start", 
                                                                  spyderMQArgsClasses);
                
                spyderMQArgs = new Object[1];
                spyderMQArgs[0] = mBeanServer;
                
                Logger.log("Starting SpyderMQ...");
                startMethod.invoke(spyderMQServer, spyderMQArgs); 
            } catch (Exception e) {
                log.error("SpyderMQ failed");
                log.exception(e);
            }
        }
    }
    
    public void stopService() {
        Class [] spyderMQArgsClasses = null;
        Method stopMethod = null;
        Object [] spyderMQArgs = null;
        
        if (this.spyderMQServer != null) {
            try {
                spyderMQArgsClasses = new Class[0];
                stopMethod = this.spyderMQServer.getClass().getMethod("stop", 
                                                                      spyderMQArgsClasses);
                spyderMQArgs = new Object[0];
                stopMethod.invoke(spyderMQServer, spyderMQArgs);
                this.spyderMQServer = null;
            } catch (Exception e) {
                log.error("SpyderMQ failed");
                log.exception(e);
            }
        }
    }
}
