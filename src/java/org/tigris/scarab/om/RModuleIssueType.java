package org.tigris.scarab.om;


import org.apache.torque.om.UnsecurePersistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.util.ScarabConstants;

import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.module.ModuleEntity;

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
     * FIXME: Should use ModuleManager.  Use this instead of setScarabModule.
     */
    public void setModule(ModuleEntity me)
        throws Exception
    {
        super.setScarabModule((ScarabModule)me);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>ModuleEntity</code> value
     */
    public ModuleEntity getModule()
        throws Exception
    {
        return getScarabModule();
    }

    /**
     * Checks if user has permission to delete module-issue type mapping.
     */
    public void delete( ScarabUser user )
         throws Exception
    {                
        ModuleEntity module = getModule();
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

    /**
     * Copies object.
     */
    public RModuleIssueType copy()
         throws Exception
    {                
        RModuleIssueType rmit2 = new RModuleIssueType();
        rmit2.setModuleId(getModuleId());
        rmit2.setIssueTypeId(getIssueTypeId());
        rmit2.setActive(getActive());
        rmit2.setDisplay(getDisplay());
        rmit2.setOrder(getOrder());
        rmit2.setHistory(getHistory());
        rmit2.setComments(getComments());
        return rmit2;
    }
}
