package org.tigris.scarab.om;

import java.util.List;

import org.apache.turbine.Turbine;
import org.apache.fulcrum.template.TemplateContext;
//import org.apache.turbine.modules.ContextAdapter; 
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.tools.Email;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class IssueTemplate 
    extends org.tigris.scarab.om.BaseIssueTemplate
    implements Persistent
{
    public static final NumberKey USER__PK = new NumberKey("1");
    public static final NumberKey GLOBAL__PK = new NumberKey("2");

    /**
     * A new IssueTemplate object
     */
    public static IssueTemplate getInstance() 
    {
        return new IssueTemplate();
    }


    public void saveAndSendEmail( ScarabUser user, ModuleEntity module, 
                                  TemplateContext context )
        throws Exception
    {
        ScarabSecurity security = SecurityFactory.getInstance();
        // If it's a global query, user must have Item | Approve 
        //   permission, Or its Approved field gets set to false
        if (getTypeId().equals(USER__PK))
        {
            setApproved(true);
        }
        else if (security.hasPermission(ScarabSecurity.ITEM__APPROVE,
                                               user, module))
        {
            setApproved(true);
        } 
        else
        {
            setApproved(false);
            setTypeId(USER__PK);

            // Send Email to module owner to approve new template
            context.put("user", user);
            context.put("module", module);

            String subject = "New template requires approval";
            String template = Turbine.getConfiguration().
                getString("scarab.email.requireapproval.template",
                          "email/RequireApproval.vm");
            ScarabUser toUser = (ScarabUser) ScarabUserImplPeer
                              .retrieveByPK((NumberKey)module.getOwnerId());
            Email.sendEmail(context, null, toUser, subject, template);
        }
        save();
    }


    /**
     * Returns list of all issue template types.
     */
    public List getAllTemplateTypes() throws Exception
    {
        return IssueTemplateTypePeer.doSelect(new Criteria());
    }

    /**
     * Checks permission and approves or rejects query. 
     * If query is approved, query type set to "global", else set to "personal".
     */
    public void approve( ScarabUser user, boolean approved )
         throws Exception
    {                
        ScarabSecurity security = SecurityFactory.getInstance();
        ScarabModule module = getScarabModule();

        if (security.hasPermission(ScarabSecurity.ITEM__APPROVE, user,
                                   module))
        {
            setApproved(true);
            if (approved)
            {
                setTypeId(GLOBAL__PK);
            }
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }


    /**
     * Checks if user has permission to delete template.
     * Only the creating user can delete a personal template.
     * Only project owner or admin can delete a project-wide template.
     */
    public void delete( ScarabUser user )
         throws Exception
    {                
        ScarabModule module = getScarabModule();
        ScarabSecurity security = SecurityFactory.getInstance();

        if (security.hasPermission(ScarabSecurity.ITEM__APPROVE, user, module)
          || (user.getUserId().equals(getUserId()) 
             && getTypeId().equals(USER__PK)))
        {
            setDeleted(true);
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

}
