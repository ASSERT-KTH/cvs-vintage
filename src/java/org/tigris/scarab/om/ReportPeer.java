package org.tigris.scarab.om;

import java.util.*;
import com.workingdogs.village.*;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

// Local classes
import org.tigris.scarab.om.map.*;
import org.tigris.scarab.util.ScarabException;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class ReportPeer 
    extends org.tigris.scarab.om.BaseReportPeer
{

    /**
     * Does a saved report exist under the given name.
     *
     * @param name a <code>String</code> report name
     * @return true if a report by the given name exists
     */
    public static boolean exists(String name)
        throws Exception
    {
        return retrieveByName(name) != null;
    }

    /**
     * gets the active report saved under the given name
     *
     * @param name a <code>String</code> value
     * @return a <code>Report</code> value
     * @exception Exception if an error occurs
     */
    public static Report retrieveByName(String name)
        throws Exception
    {
        Report report = null;
        Criteria crit = new Criteria()
            .add(NAME, name)
            .add(DELETED, false);
        List reports = doSelect(crit);
        if ( reports.size() == 1 ) 
        {
            report = (Report)reports.get(0);
        }
        else if ( reports.size() > 1 ) 
        {
            throw new ScarabException(
                "Multiple reports are active under the name, " + name + 
                ".  Application error, please notify the developers.");
        }
        
        return report;
    }
}
