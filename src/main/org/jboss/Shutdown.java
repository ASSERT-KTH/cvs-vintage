/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss;

import java.util.Hashtable;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.jmx.connector.rmi.RMIConnectorImpl;
import org.jboss.jmx.connector.RemoteMBeanServer;

import org.jboss.system.server.Server;
import org.jboss.system.server.ServerImpl;

import org.jboss.mx.util.MBeanProxyExt;

/**
 * A JMX client to shutdown a remote JBoss server.
 *
 * <p>
 * This was quick and dirty...
 *
 * @version <tt>$Revision: 1.10 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Shutdown
{
   /////////////////////////////////////////////////////////////////////////
   //                         Command Line Support                        //
   /////////////////////////////////////////////////////////////////////////

   public static final String PROGRAM_NAME = System.getProperty("program.name", "shutdown");

   protected static void displayUsage()
   {
      System.out.println("A JMX client to shutdown (exit or halt) a remote JBoss server.");
      System.out.println();
      System.out.println("usage: " + PROGRAM_NAME + " [options] <operation>");
      System.out.println();
      System.out.println("options:");
      System.out.println("    -h, --help                Show this help message");
      System.out.println("    -D<name>[=<value>]        Set a system property");
      System.out.println("    --                        Stop processing options");
      System.out.println("    -s, --server=<url>        Specify the JNDI URL of the remote server");
      System.out.println("    -a, --adapter=<name>      Specify JNDI name of the RMI adapter to use");
      System.out.println();
      System.out.println("operations:");
      System.out.println("    -S, --shutdown            Shutdown the remove VM (default)");
      System.out.println("    -e, --exit=<code>         Force the VM to exit with a status code");
      System.out.println("    -H, --halt=<code>         Force the VM to halt with a status code");
   }

   public static void main(final String[] args) throws Exception
   {
      if (args.length == 0) {
         displayUsage();
         System.exit(0);
      }
      
      String sopts = "-:hD:s:d:u:i:r:a:Se:H:";
      LongOpt[] lopts =
      {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("server", LongOpt.REQUIRED_ARGUMENT, null, 's'),
         new LongOpt("shutdown", LongOpt.NO_ARGUMENT, null, 'S'),
         new LongOpt("exit", LongOpt.REQUIRED_ARGUMENT, null, 'e'),
         new LongOpt("halt", LongOpt.REQUIRED_ARGUMENT, null, 'H'),
      };

      Getopt getopt = new Getopt(PROGRAM_NAME, args, sopts, lopts);
      int code;
      String arg;

      String serverURL = null;
      String adapterName = null;
      boolean exit = false;
      boolean halt = false;
      int exitcode = -1;
      
      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
            case '?':
               // for now both of these should exit with error status
               System.exit(1);
               break; // for completeness
               
            case 1:
               // this will catch non-option arguments
               // (which we don't currently care about)
               System.err.println(PROGRAM_NAME + ": unused non-option argument: " +
                                  getopt.getOptarg());
               break; // for completeness
               
            case 'h':
               // show command line help
               displayUsage();
               System.exit(0);
               break; // for completeness
               
            case 'D':
            {
               // set a system property
               arg = getopt.getOptarg();
               String name, value;
               int i = arg.indexOf("=");
               if (i == -1)
               {
                  name = arg;
                  value = "true";
               }
               else
               {
                  name = arg.substring(0, i);
                  value = arg.substring(i + 1, arg.length());
               }
               System.setProperty(name, value);
               break;
            }
            
            case 's':
            {
               serverURL = getopt.getOptarg();
               break;
            }

            case 'S':
            {
               // nothing...
               break;
            }
            
            case 'a':
            {
               adapterName = getopt.getOptarg();
               break;
            }
            
            case 'e':
            {
               exitcode = Integer.parseInt(getopt.getOptarg());
               exit = true;
               break;
            }
            
            case 'H':
            {
               exitcode = Integer.parseInt(getopt.getOptarg());
               halt = true;
               break;
            }
         }
      }

      InitialContext ctx;
      
      if (serverURL == null) {
         ctx = new InitialContext();
      }
      else {
         Hashtable env = new Hashtable();
         env.put(Context.PROVIDER_URL, serverURL);
         ctx  = new InitialContext(env);
      }

      // if adapter is null, the use the default
      if (adapterName == null) {
         adapterName = org.jboss.jmx.adaptor.rmi.RMIAdaptorService.LOCAL_NAME;
      }
      
      Object obj = ctx.lookup(adapterName);
      
      if (!(obj instanceof RMIAdaptor)) {
         throw new RuntimeException("Object not of type: RMIAdaptorImpl, but: " +
                                    (obj == null ? "not found" : obj.getClass().getName()));
      }

      RemoteMBeanServer connector = new RMIConnectorImpl((RMIAdaptor)obj);

      Server server = (Server)MBeanProxyExt.create(Server.class,
                                                ServerImpl.OBJECT_NAME,
                                                connector);

      if (exit) {
         server.exit(exitcode);
      }
      else if (halt) {
         server.halt(exitcode);
      }
      else {
         server.shutdown();
      }
   }
}
