/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.logging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

import org.jboss.system.ServiceMBeanSupport;

/**
 * The log analysis service.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 *
 */
public class LogAnalysis
  extends ServiceMBeanSupport
  implements LogAnalysisMBean
{
  // Constants -----------------------------------------------------

  // Attributes ----------------------------------------------------
  
  private HashMap analysis;

  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  /**
   * Default constructor.
   */
  public LogAnalysis()
  {
    analysis = new HashMap();
  }

  // Public --------------------------------------------------------

  // LogAnalysisMBean implementation -------------------------------

  public synchronized String analyse(boolean showAll)
  {
    StringBuffer buffer = new StringBuffer();

    if (analysis.size() > 0)
    {
      buffer.append("<h2><center>Messages by category and priority</center></h2>");

      // Add the table header
      buffer.append("<table border='1' align='center' cellpadding='3'><tr>");
      buffer.append("<th>Category</th>");
      buffer.append("<th>Total</th>");
      buffer.append("<th>Trace</th>");
      buffer.append("<th>Debug</th>");
      buffer.append("<th>Info</th>");
      buffer.append("<th>Warn</th>");
      buffer.append("<th>Error</th>");
      buffer.append("<th>Fatal</th>");
      buffer.append("<th>Other</th>");
      buffer.append("</tr>");

      // Add each map entry as a table row
      Iterator i = analysis.entrySet().iterator();
      while(i.hasNext())
      {
        Map.Entry e = (Map.Entry) i.next();
        Category category = (Category) e.getKey();
        Analysis anal = (Analysis) e.getValue();

        // Skip correct loggings unless showing everything
        if (!showAll && getColor(category, TracePriority.TRACE, anal.debug).equals("black")
            && getColor(category, Priority.DEBUG, anal.debug).equals("black")
            && getColor(category, Priority.INFO, anal.info).equals("black")
            && getColor(category, Priority.WARN, anal.warn).equals("black")
            && getColor(category, Priority.ERROR, anal.error).equals("black")
            && getColor(category, Priority.FATAL, anal.fatal).equals("black"))
          continue;

        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append(category.getName());
        buffer.append("</td>");

        buffer.append("<td align='center'>");
        buffer.append(anal.trace + anal.debug + anal.info + anal.warn + anal.error + anal.fatal + anal.other);
        buffer.append("</td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, TracePriority.TRACE, anal.trace));
        buffer.append("'>");
        buffer.append(anal.trace);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, Priority.DEBUG, anal.debug));
        buffer.append("'>");
        buffer.append(anal.debug);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, Priority.INFO, anal.info));
        buffer.append("'>");
        buffer.append(anal.info);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, Priority.WARN, anal.warn));
        buffer.append("'>");
        buffer.append(anal.warn);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, Priority.ERROR, anal.error));
        buffer.append("'>");
        buffer.append(anal.error);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='");
        buffer.append(getColor(category, Priority.FATAL, anal.fatal));
        buffer.append("'>");
        buffer.append(anal.fatal);
        buffer.append("</font></td>");

        buffer.append("<td align='center'><font color='black'>");
        buffer.append(anal.other);
        buffer.append("</font></td>");

        buffer.append("</tr>");
      }
      buffer.append("</table>");
    }
    return buffer.toString();
  }

  // ServiceMBeanSupport overrides ---------------------------------

  public void startService()
    throws Exception
  {
    Logger.setAnalysis(this);
  }

  public void stopService()
  {
    Logger.setAnalysis(null);
    analysis.clear();
  }

  public String getName()
  {
    return "Log analysis";
  }

  // Package protected ---------------------------------------------

  /**
   * Add one to the count for this category and priority.
   *
   * @param log the logging category.
   * @param type the priority.
   */
  public synchronized void add(Category log, Priority type)
  {
     if (!analysis.containsKey(log))
       analysis.put(log, new Analysis());
     Analysis anal = (Analysis) analysis.get(log);
     if (type == TracePriority.TRACE)
       anal.trace++;
     else if (type == Priority.DEBUG)
       anal.debug++;
     else if (type == Priority.INFO)
       anal.info++;
     else if (type == Priority.WARN)
       anal.warn++;
     else if (type == Priority.ERROR)
       anal.error++;
     else if (type == Priority.FATAL)
       anal.fatal++;
     else
       anal.other++;
  }

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  /**
   * Gets the color to display.
   *
   * @param Category the logging category
   * @param Priority the logging priority
   * @param count the number of logs
   * @return red for incorrect logs, black otherwise
   */
  private String getColor(Category category, Priority priority, long count)
  {
    if (count == 0)
      return "black";
    if (!category.isEnabledFor(priority))
      return "red";
    if (priority.isGreaterOrEqual(category.getChainedPriority()))
      return "black";
    return "red";
  }

  // Inner classes -------------------------------------------------

  /**
   * The analysis data for each log.
   */
  private class Analysis
  {
    // Attributes ----------------------------------------------------

    // The number of trace logs
    public long trace;

    // The number of debug logs
    public long debug;

    // The number of info logs
    public long info;

    // The number of warn logs
    public long warn;

    // The number of error logs
    public long error;

    // The number of fatal logs
    public long fatal;

    // The number of other logs
    public long other;
  }
}
