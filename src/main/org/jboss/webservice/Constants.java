/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: Constants.java,v 1.2 2004/04/30 16:24:46 tdiesler Exp $
package org.jboss.webservice;

// $Id: Constants.java,v 1.2 2004/04/30 16:24:46 tdiesler Exp $

/**
 * Static constants for JBoss webservices.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public interface Constants
{
   /** The default WS4EE namespace uri */
   public static final String WS4EE_NAMESPACE_URI = "http://webservice.jboss.com/ws4ee";
   /** The Axis client engine config system property: org.jboss.webservice.client.config */
   public static final String WS4EE_CLIENT_CONFIG = "org.jboss.webservice.client.config";
   /** The Axis server engine config system property: org.jboss.webservice.client.server */
   public static final String WS4EE_SERVER_CONFIG = "org.jboss.webservice.client.server";
}
