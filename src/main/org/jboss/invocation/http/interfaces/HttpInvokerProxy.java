/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.invocation.ServerID;

/** The client side Http invoker proxy that posts an invocation to the
 InvokerServlet using the HttpURLConnection created from the proxy
 externalURL.

* @author Scott.Stark@jboss.org
* @version $Revision: 1.5 $
*/
public class HttpInvokerProxy
   implements Invoker, Externalizable
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(HttpInvokerProxy.class);

   /** Serial Version Identifier. */
   static final long serialVersionUID = -8249272784108192267L;

   // Attributes ----------------------------------------------------

   // URL to the remote JMX node invoker
   protected String externalURLValue;
   protected transient URL externalURL;

   protected transient ServerID serverID;

   // Constructors --------------------------------------------------
   public HttpInvokerProxy()
   {
      // For externalization to work
   }

   /**
      @param externalURL, the URL through which clients should contact the
      InvokerServlet.
   */
   public HttpInvokerProxy(String externalURLValue)
   {
      this.externalURLValue = externalURLValue;
   }

   // Public --------------------------------------------------------

   public ServerID getServerID() throws Exception
   {
      if (serverID == null) {

         if( externalURL == null )
         {
            externalURL = Util.resolveURL(externalURLValue);
         }
         serverID = new ServerID(externalURL.getHost(),  externalURL.getPort(), false, 0);
      } // end of if ()
      return serverID;
   }

   public org.jboss.remoting.ident.Identity getIdentity() {return null;}

   /** This method builds a MarshalledInvocation from the invocation passed
       in and then does a post to the target URL.
   */
   public InvocationResponse invoke(Invocation invocation)
      throws Exception
   {
      // We are going to go through a Remote invocation, switch to a Marshalled Invocation
      MarshalledInvocation mi = new MarshalledInvocation(invocation);

      if( externalURL == null )
         externalURL = Util.resolveURL(externalURLValue);
      Object value = Util.invoke(externalURL, mi);
      return (InvocationResponse)value;
   }

   /** Externalize this instance.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(externalURLValue);
   }

   /** Un-externalize this instance.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      externalURLValue = (String) in.readObject();
      externalURL = Util.resolveURL(externalURLValue);
   }

}
