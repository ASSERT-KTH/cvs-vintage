/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging.log4j;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/** A subclass of PrintStream that redirects its output to a log4j Category.
This class is used to map PrintStream/PrintWriter oriented logging onto
the log4j Categories. Examples include capturing System.out/System.err writes.

@author Scott_Stark@displayscape.com
@version $Revision: 1.3 $
*/
public class CategoryStream extends PrintStream
{
    private Category category;
    private Priority priority;
    private boolean inWrite;
    private boolean issuedWarning;

    /** Redirect logging to the indicated category using Priority.INFO
    */
    public CategoryStream(Category category)
    {
        this(category, Priority.INFO, System.out);
    }
    /** Redirect logging to the indicated category using the given
        priority. The ps is simply passed to super but is not used.
    */
    public CategoryStream(Category category, Priority priority, PrintStream ps)
    {
        super(ps);
        this.category = category;
        this.priority = priority;
    }
    public void println(String msg)
    {
        if( msg == null )
            msg = "null";
        byte[] bytes = msg.getBytes();
        write(bytes, 0, bytes.length);
    }
    public void println(Object msg)
    {
        if( msg == null )
            msg = "null";
        byte[] bytes = msg.toString().getBytes();
        write(bytes, 0, bytes.length);
    }
    public void write(byte b)
    {
        byte[] bytes = {b};
        write(bytes, 0, 1);
    }
    public synchronized void write(byte[] b, int off, int len)
    {
        if( inWrite == true )
        {
            /* There is a configuration error that is causing looping. Most
             likely there are two console appenders so just return to prevent
             spinning.
            */
            if( issuedWarning == false )
            {
                String msg = "ERROR: invalid console appender config detected, console stream is looping";
                try
                {
                    out.write(msg.getBytes());
                }
                catch(IOException e)
                {
                }
                issuedWarning = true;
            }
            return;
        }
        inWrite = true;
        // Remove the end of line chars
        while( len > 0 && (b[len-1] == '\n' || b[len-1] == '\r') && len > off )
            len --;

        String msg = new String(b, off, len);
        category.log(priority, msg);
        inWrite = false;
    }
}
