package org.tigris.scarab.om;


import org.apache.fulcrum.template.TemplateContext;
import org.apache.turbine.Turbine;

import org.apache.torque.om.UnsecurePersistent;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.Email;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class IssueTemplateInfo 
    extends org.tigris.scarab.om.BaseIssueTemplateInfo
    implements UnsecurePersistent
{

    /**
     * A new IssueTemplateInfo object
     */
    public static IssueTemplateInfo getInstance() 
    {
        return new IssueTemplateInfo();
    }


    public void saveAndSendEmail( ScarabUser user, ModuleEntity module, 
                                  TemplateContext context )
        throws Exception
    {
        Issue issue = (Issue) IssuePeer.retrieveByPK(getIssueId());

        // If it's a global template, user must have Item | Approve 
        //   permission, or its Approved field gets set to false
        if (getScopeId().equals(Scope.PERSONAL__PK))
        {
            setApproved(true);
        }
        else if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
        } 
        else
        {
            setApproved(false);
            setScopeId(Scope.PERSONAL__PK);
            issue.save();

            // Send Email to module owner to approve new template
            if (context != null)
            {
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
        }
        save();
    }

    /*
     * Checks permission and approves or rejects template.
     * If template is approved,template type set to "global", else set to "personal".
     */
    public void approve( ScarabUser user, boolean approved )
         throws Exception
    {                
        ModuleEntity module = getIssue().getModule();

        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            setApproved(true);
            if (approved)
            {
                setScopeId(Scope.GLOBAL__PK);
            }
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

}
