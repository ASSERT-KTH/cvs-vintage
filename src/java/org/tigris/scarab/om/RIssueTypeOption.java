
package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class RIssueTypeOption 
    extends org.tigris.scarab.om.BaseRIssueTypeOption
    implements Persistent
{

    public void delete( ScarabUser user )
         throws Exception
    {                
            Criteria c = new Criteria()
                .add(RIssueTypeOptionPeer.ISSUE_TYPE_ID, getIssueTypeId())
                .add(RIssueTypeOptionPeer.OPTION_ID, getOptionId());
            RIssueTypeOptionPeer.doDelete(c);

            // Correct the ordering of the remaining options
            ArrayList optIds = new ArrayList();
            List rios = getIssueType().getRIssueTypeOptions(getAttributeOption().getAttribute(), false);
            for (int i=0; i<rios.size();i++)
            {
                RIssueTypeOption rio = (RIssueTypeOption)rios.get(i);
                optIds.add(rio.getOptionId());
            }
            Criteria c2 = new Criteria()
                .addIn(RIssueTypeOptionPeer.OPTION_ID, optIds)
                .add(RIssueTypeOptionPeer.PREFERRED_ORDER, getOrder(), Criteria.GREATER_THAN);
            List adjustRios = RIssueTypeOptionPeer.doSelect(c2);
            for (int j=0; j<adjustRios.size();j++)
            {
                RIssueTypeOption rio = (RIssueTypeOption)adjustRios.get(j);
                //rmos.remove(rmo);
                rio.setOrder(rio.getOrder() -1);
                rio.save();
                //rmos.add(rmo);
            }
    }
}
