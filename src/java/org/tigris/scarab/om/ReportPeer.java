package org.tigris.scarab.om;

import java.util.*;
import com.workingdogs.village.*;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

// Local classes
import org.tigris.scarab.om.map.*;

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
        Criteria crit = new Criteria()
            .add(NAME, name);
        List reports = doSelect(crit);
        return reports.size() > 0;
    }
}
