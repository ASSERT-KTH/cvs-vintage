/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging.log4j;

import java.io.PrintStream;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/** A subclass of PrintStream that redirects its output to a log4j Category.
This class is used to map PrintStream/PrintWriter oriented logging onto
the log4j Categories. Examples include capturing System.out/System.err writes.

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public class CategoryStream extends PrintStream
{
    private Category category;
    private Priority priority;

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
        category.log(priority, msg);
    }
    public void println(Object msg)
    {
        category.log(priority, msg);
    }
    public void write(byte[] b, int off, int len)
    {
        // Remove the end of line chars
        while( (b[len-1] == '\n' || b[len-1] == '\r') && len > off )
            len --;
        String msg = new String(b, off, len);
        category.log(priority, msg);
    }
}
