package org.tigris.scarab.om;


import org.apache.torque.om.UnsecurePersistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.util.ScarabConstants;

import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.ScarabModule;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class RModuleIssueType 
    extends org.tigris.scarab.om.BaseRModuleIssueType
    implements UnsecurePersistent
{

    /**
     * Checks if user has permission to delete module-issue type mapping.
     */
    public void delete( ScarabUser user )
         throws Exception
    {                
        ScarabModule module = getScarabModule();
        ScarabSecurity security = SecurityFactory.getInstance();

            Criteria c = new Criteria()
                .add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, getIssueTypeId());
            RModuleIssueTypePeer.doDelete(c);
            save();
        if (security.hasPermission(ScarabSecurity.MODULE__EDIT, 
                                   user, module))
        {
            c = new Criteria()
                .add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, getIssueTypeId());
            RModuleIssueTypePeer.doDelete(c);
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

}
