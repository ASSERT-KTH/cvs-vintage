/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.interfaces;

import java.net.HttpURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/* An implementation of the HostnameVerifier that accepts any SSL certificate
hostname as matching the https URL that was used to initiate the SSL connection.
This is useful for testing SSL setup in development environments using self
signed SSL certificates.

 @author Scott.Stark@jboss.org
 @version $Revision: 1.6 $
 */
public class AnyhostVerifier implements HostnameVerifier
{
   public static void setHostnameVerifier(HttpURLConnection conn)
   {
      if( conn instanceof HttpsURLConnection )
      {
         HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
         AnyhostVerifier verifier = new AnyhostVerifier();
         httpsConn.setHostnameVerifier(verifier);
      }
   }

   /* Always validates the hostname.
    * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
    */
   public boolean verify(String hostname, SSLSession session)
   {
      return true;
   }

}
