package org.tigris.scarab.om;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;
import com.workingdogs.village.DataSetException;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class RModuleIssueTypePeer 
    extends org.tigris.scarab.om.BaseRModuleIssueTypePeer
{
    private static final String COUNT = 
        "count(*)";

    /**
     * Adds count(*) to the select clause and returns the 
     * number of rows resulting from the given Criteria.  If the criteria will
     * lead to duplicate rows they will be counted.  
     *
     * @param crit a <code>Criteria</code> value
     * @return an <code>int</code> value
     * @exception TorqueException if an error occurs
     * @exception DataSetException if an error occurs
     */
    public static int count(Criteria crit)
        throws TorqueException, DataSetException
    {
        crit.addSelectColumn(COUNT);
        return ((Record)doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }
}
