/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: Constants.java,v 1.4 2004/05/07 14:58:51 tdiesler Exp $
package org.jboss.webservice;

// $Id: Constants.java,v 1.4 2004/05/07 14:58:51 tdiesler Exp $

import javax.xml.namespace.QName;

/**
 * Constants for JBoss ws4ee webservices.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public interface Constants
{
   /** The default WS4EE namespace uri */
   String NAMESPACE = "http://webservice.jboss.com/ws4ee";
   /** The Axis client engine config system property: org.jboss.ws4ee.client.config */
   String CLIENT_CONFIG = "org.jboss.ws4ee.client.config";
   /** The Axis server engine config system property: org.jboss.ws4ee.server.config */
   String SERVER_CONFIG = "org.jboss.ws4ee.server.config";
   
   // The property name of the handler chain in the message context
   String HANDLER_CHAIN = new QName(NAMESPACE, "HANDLER_CHAIN").toString();
   // The property name of the last fault in the message context
   String LAST_FAULT = new QName(NAMESPACE, "LAST_FAULT").toString();
}
