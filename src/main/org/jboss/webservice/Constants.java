/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: Constants.java,v 1.1 2004/04/28 14:34:52 tdiesler Exp $
package org.jboss.webservice;

// $Id: Constants.java,v 1.1 2004/04/28 14:34:52 tdiesler Exp $

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
   /** The Axis client engine config */
   public static final String AXIS_CLIENT_CONFIG = "META-INF/axis-client-config.xml";
   /** The Axis server engine config */
   public static final String AXIS_SERVER_CONFIG = "META-INF/axis-server-config.xml";
}
