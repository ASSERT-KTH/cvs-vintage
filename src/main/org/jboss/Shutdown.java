/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss;

import java.io.*;
import java.net.*;
import java.util.*;

/**
  * Provides an OS-independent way of shutting down JBoss.  This
  * works by accessing the JMX server and giving it the shutdown
  * command.  The host to the JMX server can be passed in as well
  * as the port number.  If neither is supplied, the defaults of
  * localhost and 8082 are used.
  * <br>
  * <br>
  * Usage:  java org.jboss.Shutdown [host] [port]
  * <br>
  * <br>
  *
  * @author Dewayne McNair (dewayne@dmsoft.com)
  * @version $Revision: 1.1 $
  */
public class Shutdown
{
    private static final String command =
         "/InvokeAction//DefaultDomain%3Atype%3DShutdown/action=shutdown?action=shutdown";

    public static void main (String argv[])
    {
        String host = "localhost";
        String port = "8082";

        if (argv.length == 1)
            host = argv[0];

        if (argv.length == 2)
            port = argv[1];

        try
        {
            URL url = new URL ("http://" + host + ":" + port + command);
            url.getContent();
        }
        catch (Exception e)
        {
            // we do nothing because even if everything went
            // right, ie JBoss is shutdown, we'd get an exception
        }
    }
}
