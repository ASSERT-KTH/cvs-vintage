/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss;

import java.net.URL;
import java.net.URLEncoder;

/**
 * Provides an OS-independent way of shutting down JBoss.  This
 * works by accessing the JMX server and giving it the shutdown
 * command.  The host to the JMX server can be passed in as well
 * as the port number.  If neither is supplied, the defaults of
 * <tt>localhost</tt> and <tt>8082</tt> are used.
 *
 * <h3>Usage:</h3>
 * <pre>
 * java org.jboss.Shutdown [host] [port]
 * </pre>
 *
 * @author <a href="mailto:dewayne@dmsoft.com">Dewayne McNair</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.7 $
 */
public class Shutdown
{
   private static final String COMMAND =
      "/InvokeAction//jboss%2Esystem%3Aservice%3DServer/action=shutdown?action=shutdown";

   /**
    * Parse the command line and shutdown the remote server.
    *
    * @param  argv       Command line arguments.
    * @throws Exception  Invalid port number.
    */
   public static void main(final String argv[]) throws Exception
   {
      String host = "localhost";
      int port = 8082;

      if (argv.length >= 1) {
         host = argv[0];
      }

      if (argv.length >= 2) {
         port = Integer.parseInt(argv[1]);
      }

      shutdown(host, port);
   }

   /**
    * Connect to the JBoss servers HTML JMX adapter and invoke the
    * shutdown service.
    *
    * @param host The hostname of the JMX server. 
    * @param port The port of the JMX server. 
    *
    */
   public static void shutdown(final String host, final int port) {
      try {
         URL url = new URL("http", host, port, COMMAND);
         url.getContent();
      }
      catch (Exception ignore) {
         // we do nothing because even if everything went
         // right, ie JBoss is shutdown, we'd get an exception
      }
   }
}
