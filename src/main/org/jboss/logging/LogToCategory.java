/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import org.apache.log4j.Category;

/** An implementation of Log that routes msgs to a
log4j Category. This class is used to replace the Log
instances created via the legacy Log.createLog(Object)

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public class LogToCategory extends Log
{
    private Category category;
    public LogToCategory(Category category)
    {
        super(category.getName());
        this.category = category;
    }

    public synchronized void log(String type, String message)
    {
        logToCategory(type, message, category);
    }

    /** Log a msg of a given Log type to the provided category.
    @param type, one of the Log "Information", "Debug", "Warning", "Error" strings.
    @param msg, the message to log
    @param category, the log4j Category instance to log the msg to
    */
    public static void logToCategory(String type, String msg, Category category)
    {
        char ctype = type.charAt(0);
        switch( ctype )
        {
            case 'W':
                category.warn(msg);
            break;
            case 'D':
                category.debug(msg);
            break;
            case 'E':
                category.error(msg);
            break;
            default:
                category.info(msg);
            break;
        }
    }
}
