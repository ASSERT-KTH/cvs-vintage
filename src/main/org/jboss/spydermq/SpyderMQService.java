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
 * JMX MBean implementation for SpyderMQ.
 *      
 *   @see    SpyderMQ subproject
 *
 *   @author Vincent Sheffer (vsheffer@telkel.com)
 *   @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 *
 *   @version $Revision: 1.4 $
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
                log.log("Testing if SpyderMQ is present....");
                try {
                    spyderMQServer = Thread.currentThread().getContextClassLoader().loadClass("org.spydermq.server.StartServer").newInstance(); 
                    log.log("OK");
                }catch(Exception e) {
                    log.log("failed");
                    log.log("SpyderMQ wasn't found:");
                    log.debug(e.getMessage());
                    return;
                } 
                
                Class[]  spyderMQArgsClasses = { MBeanServer.class };
                Object[] spyderMQArgs        = { mBeanServer };
                
                Method startMethod = spyderMQServer.getClass().getMethod("start", 
                                                                  spyderMQArgsClasses);
                
                Logger.log("Starting SpyderMQ...");
                startMethod.invoke(spyderMQServer, spyderMQArgs); 
            }
	        catch (Exception e) {
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
                
                // [FIXME] jpl
                //      This causes some error messages on the console so
                //      disabled for now
                
                //stopMethod.invoke(spyderMQServer, spyderMQArgs);
                this.spyderMQServer = null;
            } catch (Exception e) {
                log.error("SpyderMQ failed");
                log.exception(e);
            }
        }
    }
}
