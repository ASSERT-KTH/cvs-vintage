/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.interfaces;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* An implementation of the HostnameVerifier that accepts any SSL certificate
hostname as matching the https URL that was used to initiate the SSL connection.
This is useful for testing SSL setup in development environments using self
signed SSL certificates.

 @author Scott.Stark@jboss.org
 @version $Revision: 1.3 $
 */
public class AnyhostVerifier implements HostnameVerifier
{
   public boolean verify(String s, SSLSession sslSession)
   {
      return true;
   }
}
