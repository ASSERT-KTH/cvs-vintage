/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tomcat;

import java.net.URL;
import java.net.InetAddress;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import javax.management.*;

import org.jboss.logging.Log;
import org.jboss.logging.Logger;
import org.jboss.util.ServiceMBeanSupport;
import org.jboss.ejb.DeploymentException;


/**
 *   A service to launch tomcat from JMX.
 *      
 *   This uses the class org.apache.tomcat.startup.EmbededTomcat, which means 
 *   that we can add and remove tomcat "contexts" on the fly.
 *   
 *   If you use this service, Tomcat's server.xml file will NOT be processed, so 
 *   you have to add all contexts through JMX.
 *   
 *   @see <related>
 *   @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.1 $
 */
public class EmbededTomcatService extends ServiceMBeanSupport
	implements EmbededTomcatServiceMBean, MBeanRegistration {
	
	// Constants -----------------------------------------------------
	public static final String NAME = "EmbededTomcat";
	
	// Attributes ----------------------------------------------------
	Thread runner;
	
	// the tomcat launcher
	Object embededTomcat;
	
	// the port tomcat must listen to
	int port;
	
	// cache methods used to deploy/undeploy
	Method addCtxMethod, initCtxMethod, removeCtxMethod;
	
	// repository for deployed URLs and the associated servletContexts
	Hashtable deployedURLs = new Hashtable();

	final Log log = new Log(NAME);
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	public EmbededTomcatService(int port) {
		this.port = port;
	}
	
	
	// Public --------------------------------------------------------
	public ObjectName getObjectName(MBeanServer server, ObjectName name)
		throws javax.management.MalformedObjectNameException {
		
		return new ObjectName(OBJECT_NAME);
	}
	
	public String getName() {
		return NAME;
	}
	
	
	public void startService() throws Exception {
		runner = new Thread(new Runnable() {
			public void run() {
		
				Log.setLog(log);
				
				try {
					
					Class tomcatClass;
						
					Logger.log("Testing if Tomcat is present....");
					
					// We need the tomcat home to set tomcat's working dir / ROOT context
					// This is set by using "java -Dtomcat.home=$TOMCAT_HOME ..." in run.sh/bat
					String tomcatHome = System.getProperty("tomcat.home");
					if (tomcatHome == null) {
						Logger.log("failed");
						Logger.log("System property tomcat.home not found. Be sure to set TOMCAT_HOME");
						return;
					}			   
						
					try {
							
						// Using EmbededTomcat instead of org.apache.tomcat.startup.Tomcat
						// allows us to add/remove contexts on the fly
						tomcatClass = Class.forName("org.apache.tomcat.startup.EmbededTomcat");
						Logger.log("OK");
						
					} catch(Exception e) {
						Logger.log("failed");
						Logger.log("Tomcat wasn't found. Be sure to have your CLASSPATH correctly set");
						//Logger.exception(e);
						return;
					} 
						
					// Initialize the EmbededTomcat object.
					// See javadoc in org.apache.tomcat.startup.EmbededTomcat
					embededTomcat = tomcatClass.newInstance();
						
					// init the methods for deploy/undeploy
					addCtxMethod = tomcatClass.getMethod("addContext", new Class[] { String.class, URL.class });
					initCtxMethod = tomcatClass.getMethod("initContext", new Class[] { Class.forName("javax.servlet.ServletContext") });
					removeCtxMethod = tomcatClass.getMethod("removeContext", new Class[] { Class.forName("javax.servlet.ServletContext") });
					
					// set debug
					invokeMethod(embededTomcat, "setDebug",
						new Class[] { Integer.TYPE },
						new Object[] { new Integer(0) });
					
					// set working dir
					invokeMethod(embededTomcat, "setWorkDir", 
						new Class[] { String.class },
						new Object[] { tomcatHome });
						
					// add root context
					deploy("/", "file:" + tomcatHome + "/webapps/ROOT");
					
					// add endpoint (web service)
					invokeMethod(embededTomcat, "addEndpoint", 
						new Class[] { Integer.TYPE, InetAddress.class, String.class },
						new Object[] { new Integer(port), null, null });
					
					// start
					invokeMethod(embededTomcat, "start", null, new Object[] {});
					
				} catch (Exception e) {
					
					Logger.error("Tomcat failed");
					Logger.exception(e);
				}
			}
		});
		
		runner.start();
	}
	
	
	public void stopService()
	{
		if (runner != null)
		{
			runner.stop();
			runner = null;
		}
	}
	
	
	// warURL could be given as a java.net.URL, but the JMX RI's html adaptor can't
	// show inputs for URLs in HTML forms. 
	public void deploy(String ctxPath, String warUrl) throws DeploymentException {
		Log.setLog(log);
		
		try {
			// add the context
			Object servletCtx = addCtxMethod.invoke(embededTomcat, new Object[] { ctxPath, new URL(warUrl) });
			
			// init the context
			initCtxMethod.invoke(embededTomcat, new Object[] { servletCtx });
			
			// keep track of deployed contexts for undeployment
			deployedURLs.put(warUrl, servletCtx);
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeploymentException(e.getMessage());
		} finally {
			Log.unsetLog();
		}
	}
	
	
	public void undeploy(String warUrl) throws DeploymentException {
		Log.setLog(log);
		
		try {
			// find the javax.servlet.ServletContext in the repository
			Object servletCtx = deployedURLs.get(warUrl);
			
			if (servletCtx == null) 
				throw new DeploymentException("URL " + warUrl + " is not deployed");
			
			// remove the context
			removeCtxMethod.invoke(embededTomcat, new Object[] { servletCtx });
		
		} catch (Exception e) {
			throw new DeploymentException(e.getMessage());
		} finally {
			Log.unsetLog();
		}
	
	}
	
	
	public boolean isDeployed(String warUrl) {
		return deployedURLs.containsKey(warUrl);
	}
	
	
	// Protected -----------------------------------------------------
	
	// Private -------------------------------------------------------
	private Object invokeMethod(Object obj, String methodName, Class[] argTypes, Object[] args)
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		
		// get the method
		Method method = obj.getClass().getMethod(methodName, argTypes);
		
		// invoke it, return the result
		return method.invoke(obj, args);
	}

}
