/**
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: RegistryShell.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

// Jakarta CLI
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.InitialContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class <code>RemoteShell</code> Provide a RMI shell access to a RJVM daemon
 * For the moment it's a basic (but full) rjvm shell
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class RegistryShell {

    // command line options (thanks jef)
    private static Options cmdLineOptions = null;

    /**
     * proc host location
     */
    public static String HOST_LOCATION = "localhost";

    /**
     * proc rmi registry port (default 1099)
     */
    public static int PORT_NUMBER = 1099;

    /**
     * static print help method (thaks to Jef and the JOTM team)
     */
    public static void printHelp(Options cmdLineOptions) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("Remot Proc Daemon Shell [options...]", cmdLineOptions);
    }

    /**
     * Main method, starting the Registry Shell
     * @param args [] the arguments : l [host location] rmi host location
     *        (Optional, default localhost) p [port number] rmi port number for
     *        this daemon (Optional, default 1099)
     */
    public static void main(String args[]) {

        // get the arguments
        cmdLineOptions = new Options();
        // option parameters are: short description (char), long description
        // (String), has arguments (boolean),
        //                        description (String), required (boolean), has multiple arguments
        // (boolean)
        cmdLineOptions.addOption('l', "host location", true, "Host location", false, false);
        cmdLineOptions.addOption('p', "port", true, "daemon rmi port number", false, false);
        cmdLineOptions.addOption('c', "command", true, "start a command and exit", false, false);
        cmdLineOptions.addOption('h', "help", false, "print this message and exit", false, false);

        CommandLine cmd = null;
        try {
            cmd = cmdLineOptions.parse(args, true);
        } catch (ParseException e) {
            System.err.println("\n" + e.getMessage());
            printHelp(cmdLineOptions);
            System.err.println();
            System.exit(1);
        }

        if (cmd.hasOption('h')) {
            printHelp(cmdLineOptions);
            System.exit(1);
        }

        if (cmd.hasOption('p')) {
            PORT_NUMBER = (new Integer(cmd.getOptionValue('p'))).intValue();
        }
        if (cmd.hasOption('l')) {
            HOST_LOCATION = cmd.getOptionValue('l');
        }

        if (cmd.hasOption('c')) {
            String procCmd = cmd.getOptionValue('c');
            startCommandLine(procCmd);
            System.exit(0);
        }

        try {
            System.out.println("Connecting to Registry Manager: rmi://" + HOST_LOCATION + ":" + PORT_NUMBER);
            System.out.println("Start entering lines to manage remote processes");
            System.out.println("(use \"exit\" to exit)...");
            //Basic shell with no completion or history
            BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));
            //Open for reading from the keyboard
            String line;
            boolean cont = true;
            boolean fire = false;
            boolean write = false;
            System.out.print("reg> ");
            line = infile.readLine();
            while (cont) {
                if ((!line.equalsIgnoreCase("quit")) && (!line.equalsIgnoreCase("exit"))) {
                    if (!fire) {
                        if ((!line.equalsIgnoreCase("fw")) && (!line.equalsIgnoreCase("fr"))) {
                            startCommandLine(line);
                            System.out.print("reg> ");
                            line = infile.readLine();
                        } else {
                            fire = true;
                            if (line.equalsIgnoreCase("fw")) {
                                write = true;
                            } else {
                                write = false;
                            }
                        }
                    } else {
                        startFireWallLine(line, write);
                        if (write) {
                            System.out.print("firewall w>");
                        } else {
                            System.out.print("firewall r>");
                        }
                        line = infile.readLine();
                    }
                } else {
                    if (fire) {
                        fire = false;
                        System.out.print("\nreg> ");
                        line = infile.readLine();
                    } else {
                        cont = false;
                    }
                }
            }
            System.out.println("ciao !");
            infile.close();

        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    public static void startCommandLine(String line) {
        try {
            if (line.trim().equals("")) {
                // nothing ...
            } else if ((line.trim().startsWith("help")) || (line.trim().startsWith("?"))) {

                //get help
                printHelpProcess();

            } else if (line.trim().startsWith("ping")) {

                printPing(line);

            } else if (line.trim().startsWith("v")) {

                printVerbose(line);

            } else if (line.trim().startsWith("purge")) {

                printPurge(line);

            } else if (line.trim().startsWith("list")) {

                printList(line);

            } else if (line.trim().startsWith("resume")) {

                printResumeRegistry();

            } else if (line.trim().startsWith("stop")) {

                try {
                    getRegistryManager().stop();
                } catch (Exception e) {
                }
                // process stopped
                System.out.println("registry stopped, get out of the shell");
                System.exit(0);
            } else {
                System.out.println("Unreconized function:" + line);
                printHelpProcess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startFireWallLine(String line, boolean write) {
        try {
            if (line.trim().equals("") || (line.equalsIgnoreCase("fw")) || (line.equalsIgnoreCase("fr"))) {
                // nothing ...
            } else if ((line.trim().startsWith("help")) || (line.trim().startsWith("?"))) {

                //get help
                printFireWallHelpProcess();

            } else if (line.trim().startsWith("lista")) {

                printListAllowed(line, write);

            } else if (line.trim().startsWith("resume")) {

                printResumeFireWall();

            } else if (line.trim().startsWith("listf")) {

                printListForbiden(line, write);

            } else if (line.trim().startsWith("aaa")) {

                printAddAllow(line, write);

            } else if (line.trim().startsWith("afa")) {

                printAddForbid(line, write);

            } else if (line.trim().startsWith("aall")) {

                printAllowAll(line, write);

            } else if (line.trim().startsWith("fall")) {

                printForbidAll(line, write);

            } else if (line.trim().startsWith("is")) {
                printIsAllowed(line, write);
            } else if (line.trim().startsWith("isall")) {
                printIsAll(line, write);
            } else {
                System.out.println("Unreconized function:" + line);
                printHelpProcess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line
     * @param write
     */
    private static void printIsAllowed(String line, boolean write) {
        try {
            //function
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() < 2) {
                System.out.println("the isa function take a process name parametter:");
                System.out.println("isa <IP>");
                System.out.println("where <IP> is x.x.x.x");
            } else {
                st.nextToken(); // isa flag
                String hostName = st.nextToken();
                byte[] ip = getIP(hostName);
                if (write) {
                    System.out.println("Allow address for writing is: "
                            + getRegistryManager().isWriteAllow(InetAddress.getByAddress(ip)));
                } else {
                    System.out.println("Allow address for reading is: "
                            + getRegistryManager().isReadAllow(InetAddress.getByAddress(ip)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param line
     * @param write
     */
    private static void printAddAllow(String line, boolean write) {
        try {
            //function
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() < 2) {
                System.out.println("the aaa function take a process name parametter:");
                System.out.println("aaa <IP>");
                System.out.println("where <IP> is x.x.x.x");
            } else {
                st.nextToken(); // aaa flag
                String hostName = st.nextToken();
                byte[] ip = getIP(hostName);
                if (write) {
                    getRegistryManager().addWriteAllowAddress(InetAddress.getByAddress(ip));
                } else {
                    getRegistryManager().addReadAllowAddress(InetAddress.getByAddress(ip));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line
     * @param write
     */
    private static void printAddForbid(String line, boolean write) {
        try {
            //function
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() < 2) {
                System.out.println("the afa function take a process name parametter:");
                System.out.println("afa <IP>");
                System.out.println("where <IP> is x.x.x.x");
            } else {
                st.nextToken(); // aaa flag
                String hostName = st.nextToken();
                byte[] ip = getIP(hostName);
                if (write) {
                    getRegistryManager().addWriteForbidenAddress(InetAddress.getByAddress(ip));
                } else {
                    getRegistryManager().addReadForbidenAddress(InetAddress.getByAddress(ip));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param write
     */
    private static void printIsAll(String line, boolean write) {
        try {
            if (write) {
                System.out.println("Allow address for writing is: " + getRegistryManager().isWriteAllowAll());
            } else {
                System.out.println("Allow address for reading is: " + getRegistryManager().isReadAllowAll());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line
     * @param write
     */
    private static void printForbidAll(String line, boolean write) {
        try {
            if (write) {
                getRegistryManager().forbidWriteAll();
            } else {
                getRegistryManager().forbidReadAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line
     * @param write
     */
    private static void printAllowAll(String line, boolean write) {
        try {
            if (write) {
                getRegistryManager().allowWriteAll();
            } else {
                getRegistryManager().allowReadAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line
     * @param write
     */
    private static void printListForbiden(String line, boolean write) {
        try {
            if (write) {
                System.out.println("Forbiden Adresses for writing are");
                InetAddress[] fa = getRegistryManager().listWriteForbidenAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            } else {
                System.out.println("Forbiden Adresses for reading are");
                InetAddress[] fa = getRegistryManager().listReadForbidenAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param line
     * @param write
     */
    private static void printListAllowed(String line, boolean write) {
        try {
            if (write) {
                System.out.println("Allowed Adresses for writing are");
                InetAddress[] fa = getRegistryManager().listWriteAllowedAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            } else {
                System.out.println("Allowed Adresses for reading are");
                InetAddress[] fa = getRegistryManager().listReadAllowedAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Verbose flag
     * @param command line
     */
    private static void printVerbose(String line) throws Exception {
        try {
            //conf function
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() < 2) {
                System.out.println("the verbose function take a process name parametter:");
                System.out.println("v <true|false>");
            } else {
                st.nextToken(); // verbose flag
                String verboseFlag = st.nextToken();
                getRegistryManager().setVerbose(new Boolean(verboseFlag).booleanValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Print true if the process is alive and false if not
     */
    private static void printPing(String line) throws Exception {
        try {
            getRegistryManager().ping();
            System.out.println("ping registry Ok");
        } catch (Exception e) {
            System.out.println("can not ping the registry");
        }
    }

    /**
     * Purge the registry
     */
    private static void printPurge(String line) throws Exception {
        try {
            getRegistryManager().purge();
            System.out.println("registry purged");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * List the registry
     */
    private static void printList(String line) throws Exception {
        try {
            String[] list = getRegistryManager().list();
            System.out.println("registry list:");
            for (int i = 0; i < list.length; i++) {
                System.out.println(list[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param hostName
     * @return
     */
    private static byte[] getIP(String hostName) {
        if (hostName.length() == 0) {
            return null;
        }

        int octets;
        char ch;
        byte[] dst = new byte[4];
        char[] srcb = hostName.toCharArray();
        boolean saw_digit = false;

        octets = 0;
        int i = 0;
        int cur = 0;
        while (i < srcb.length) {
            ch = srcb[i++];
            if (Character.isDigit(ch)) {
                int sum = dst[cur] * 10 + (Character.digit(ch, 10) & 0xff);

                if (sum > 255) return null;

                dst[cur] = (byte) (sum & 0xff);
                if (!saw_digit) {
                    if (++octets > 4) return null;
                    saw_digit = true;
                }
            } else if (ch == '.' && saw_digit) {
                if (octets == 4) return null;
                cur++;
                dst[cur] = 0;
                saw_digit = false;
            } else
                return null;
        }
        if (octets < 4) return null;
        return dst;
    }

    /**
     * resume Registry Informations
     */
    private static void printResumeRegistry() {
        try {
            System.out.println("Registry Resume:");
            System.out.println("Started on host: " + HOST_LOCATION + " and on port: " + PORT_NUMBER);
            System.out.println("Objects binded:");
            String[] list = getRegistryManager().list();
            for (int i = 0; i < list.length; i++) {
                System.out.println(list[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * resume FireWall Informations
     */
    private static void printResumeFireWall() {
        try {
            System.out.println("FireWall Resume:");
            if (getRegistryManager().isReadAllowAll()) {
                System.out.println("Read access is allow for all IP except:");
                InetAddress[] fa = getRegistryManager().listReadForbidenAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            } else {
                System.out.println("Read access is forbiden for all IP except:");
                InetAddress[] fa = getRegistryManager().listReadAllowedAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            }

            if (getRegistryManager().isWriteAllowAll()) {
                System.out.println("Write access is allow for all IP except:");
                InetAddress[] fa = getRegistryManager().listWriteForbidenAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            } else {
                System.out.println("Write access is forbiden for all IP except:");
                InetAddress[] fa = getRegistryManager().listWriteAllowedAddress();
                for (int i = 0; i < fa.length; i++) {
                    System.out.println(fa[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RegistryManager getRegistryManager() {
        Properties iprop = new Properties();
        iprop.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
        iprop.put("java.naming.provider.url", "rmi://" + HOST_LOCATION + ":" + PORT_NUMBER);
        try {
            InitialContext in = new InitialContext(iprop);
            return (RegistryManager) in.lookup(ManageableRegistry.REGISTRY_MANAGER_NAME);
        } catch (Exception e) {
            System.out.println("Can not start shell, can not contact Registry Manager" + e);
            return null;
        }
    }

    public static void printHelpProcess() {
        System.out.println("Registry Manager shell commands (reg>):");
        System.out.println("help or ?					-> print this help");
        System.out.println("exit						-> exit of this shell");
        System.out.println("ping						-> ping the registry");
        System.out.println("stop						-> stop the registry");
        System.out.println("resume					-> resume the registry");
        System.out.println("v	<true|false>		-> set the verbose flag");
        System.out.println("purge						-> purge the registry");
        System.out.println("list							-> list the registry");
        System.out.println("fw <command>		-> write firewal shell");
        System.out.println("fr <command>		-> write firewal shell");
    }

    public static void printFireWallHelpProcess() {

        System.out.println("The firewalls shell commands are: (firewall r/w>");
        System.out.println("IP address are IPV4 address: x.x.x.x");
        System.out.println("exit	 					-> return to root shell");
        System.out.println("resume					-> resume the firewall");
        System.out.println("lista						-> list allowed ip address");
        System.out.println("listf						-> list forbiden ip address");
        System.out.println("aaa <IP>			    -> add allowed ip address");
        System.out.println("afa	<IP>		    	-> add forbiden ip address");
        System.out.println("aall			    			-> allowed all ip address");
        System.out.println("fall <IP>			    	-> forbid all ip address");
        System.out.println("is <IP>					-> is allow for this ip address");
        System.out.println("isall			    		-> is allowed all ip address");

    }
}