/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/** 
 * This utility class allows for command-line interaction with
 * the user during Tomcat startup. This is particularly useful in capturing
 * sensitive information, such as resource passwords, that may not be suitable
 * for inclusion in the various configuration files due to security concerns.
 * <p>
 * It is designed primarily as a "building block" utility.
 * Its purpose is to provide a common API for any Tomcat listeners/interceptors
 * that may wish to communicate via the command-line during container startup.
 * <p>
 * All public methods in this class should be synchronized, and they should
 * follow the general layout described below (see
 * <a href="#promptForInput(java.lang.String)">promptForInput</a> for an
 * example implementation):
 *
 * <ol>
 *    <li>Get a copy of the current <code>stdout</code> stream by calling
 *        <a href="#cloneStdOut()">cloneStdOut</a>
 *    <li>Temporarily seize control of the <code>stdout</code> stream by
 *        calling <a href="#setStdOut()">setStdOut</a>
 *    <li>(Process the required interaction)
 *    <li>Restore <code>stdout</code> to its original value by calling
 *        <a href="#restoreStdOut(java.io.PrintStream)">restoreStdOut</a> with
 *        the clone
 * </ol>
 *
 * <i>Threading Considerations</i>
 * <p>
 * Note that sychronizing the interaction methods prevents multiple threads
 * from attempting command-line interaction at the same time. Modules calling
 * this utility will therefore be guaranteed a single atmoic
 * challenge/response, but not necessarily a single atomic "session" with the
 * command line. For example, if a module prompts for a password, determines
 * that the password is invalid, and then reprompts, there is no guarantee that
 * another thread has not prompted for something else in the meantime. In
 * practice, this would have to involve multiple threads from a single
 * command-line/JVM attempting to start multiple instances for Tomcat with a
 * single command. Nevertheless, modules should send very specific prompt
 * messages to avoid any potential user confusion.
 *
 * @author    Christopher Cain
 * @version   $Revision: 1.3 $ $Date: 2004/02/26 06:37:28 $
 */
public class Prompter {

    // ----------------------------------------------------- Fields (Constants)

    /** The maximum allowed input size */
    public static final int MAX_INPUT_LENGTH = 1024;

    // ------------------------------------------------------------ Constructor

    /**
     * The default constructor is <code>private</code>, as instantiation of
     * this class is not permitted.
     */
    private Prompter() {
    }

    /**
     * StringBuffer containing EOL character sequence
     */
    private static final StringBuffer eol =
            new StringBuffer(System.getProperty("line.separator"));

    // --------------------------------------------------------- Public Methods

    /**
     * Request a value from the user, using the given text as a prompt.
     * <p>
     * @param promptMessage   the prompt text to display to the user
     * @return   This method returns a non-null string containing the user
     *           input. If the user didn't enter a value, an empty string is
     *           returned. If the value exceeds the arbitrary size limit
     *           (currently 1k), or if it fails the
     *           <a href="#isMalformed(java.lang.String)">isMalformed</a>
     *           check, a <code>PrompterException</code> is thrown.
     * @exception IOException        any IO problems communicating with the terminal
     * @exception PrompterException  any validator exceptions with the given input
     */
    public static synchronized String promptForInput( String promptMessage, int scroll )
    throws IOException, PrompterException {

        /* Setup */

        String userInput = "";
        BufferedReader br = null;
        IOException ioeThrow = null;

        // Aim stdout at the terminal
        PrintStream initialStdOut = cloneStdOut();
        setStdOut();

        /* Execution */

        try {

            // Display the prompt

            System.out.println();
            System.out.println( promptMessage );

            // Read the input

            br = new BufferedReader(new InputStreamReader(System.in));
            userInput = br.readLine();

            // Hide the input by scrolling the display
            for ( int i = 0; i < scroll; i++ )
                System.out.println();

            // Basic validation

            if ( userInput == null )
                throw new PrompterException("Unable to read password from System.in");
            else if ( userInput.length() > MAX_INPUT_LENGTH )
                throw new PrompterException("Input limit exceeded");
            else if ( isMalformed(userInput) )
                throw new PrompterException("Input contains illegal character(s)");

            // Return

            return userInput;

        } catch ( IOException ioe ) {
            throw ioe;
        } finally {
            restoreStdOut(initialStdOut);
        }
    }

    // ----------------------------------------- Private Implementation Methods

    /**
     * Return a cloned copy of the current <code>stdout</code>. This should
     * only be called from synchronized methods.
     * <p>
     * @return   a clone of <code>stdout</code>
     */
    protected static PrintStream cloneStdOut() {

        // Note: Deep cloning doesn't appear to be necessary

        return new PrintStream(System.out);

    }

    /**
     * Temporarily redirect <code>stdout</code> to the command line (in case
     * it isn't already). This should only be called from synchronized methods.
     */
    protected static void setStdOut() {

        System.setOut(
            new PrintStream(
                new BufferedOutputStream(
                    new FileOutputStream(FileDescriptor.out),
                    128
                ),
                true
            )
        );

    }

    /**
     * Restore <code>stdout</code> to its original value. This should only be
     * called from synchronized methods.
     */
    protected static void restoreStdOut( PrintStream origStream ) {

        System.setOut(origStream);

    }

    /**
     * Perform a basic integrity check against the command-line user input.
     * Arbitrary command-line input should <b>always</b> be scanned for
     * unusual character sequences, primarily as a precaution against
     * malicious code being embeded into the reply value.
     * <p>
     * This check is currently implemented as an search for non-printing
     * characters, which usually indicates either an attempted attack or an
     * accidental key-combination (CTRL + letter). It will also disallow tabs.
     * <p>
     * This check will help prevent problems such as sending a questionable
     * string as a parameter to another program. It will <b>NOT</b> save you if
     * you are using the arbitrary user input to directly construct a command
     * string (a system-level command, a SQL statement through string
     * concatenation, etc.). In general, you <i>really</i> don't want to do
     * that.
     * <p>
     * @param inputValue   the value supplied by the user
     * @return             <code>true</code> if the input contains
     *                     immediately-suspicious characters, otherwise
     *                     <code>false</code>
     */
    protected static boolean isMalformed( String inputValue ) {

        // Note: We don't care about line separators, as BufferedReader sees
        // them as the input terminator. It is therefore impossible for the
        // input value to contain them.

        char nextChar;
        int charType;

        for ( int x = 0; x < inputValue.length(); x++ ) {

            nextChar = inputValue.charAt(x);

            if ( Character.isISOControl(nextChar) )
                return true;
            else {

                charType = Character.getType(nextChar);

                if ( charType == Character.CONTROL ||
                     charType == Character.FORMAT ||
                     charType == Character.UNASSIGNED ||
                     charType == Character.PRIVATE_USE ||
                     charType == Character.SURROGATE
                )
                    return true;

            }

        }

        return false;
    }
}
