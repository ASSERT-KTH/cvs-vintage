/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice;

// $Id: WebServiceClientDeployment.java,v 1.1 2005/01/27 18:26:21 tdiesler Exp $

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
public interface WebServiceClientDeployment
{
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
