/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WebServiceClientDeployer.java,v 1.2 2004/05/29 19:22:12 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.mx.util.ObjectNameFactory;

import javax.naming.Context;
import javax.management.ObjectName;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2004
 */
public interface WebServiceClientDeployer
{
   /** The object name of the service that provides the implementation */
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ws4ee:service=ServiceClientDeployer");

   /**
    * This binds a jaxrpc Service into the callers ENC for every service-ref element
    *
    * @param envCtx      ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param di          The client's deployment info
    * @throws org.jboss.deployment.DeploymentException if it goes wrong
    */
   void setupServiceRefEnvironment(Context envCtx, Iterator serviceRefs, DeploymentInfo di) throws DeploymentException;
}
