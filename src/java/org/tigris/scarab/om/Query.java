package org.tigris.scarab.om;

import java.util.List;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.Turbine;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter; 

import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.Email;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class Query 
    extends org.tigris.scarab.om.BaseQuery
    implements Persistent
{

    public static final NumberKey USER__PK = new NumberKey("1");
    public static final NumberKey GLOBAL__PK = new NumberKey("2");

    /**
     * A new Query object
     */
    public static Query getInstance() 
    {
        return new Query();
    }

    public void save( ScarabUser user, ModuleEntity module, 
                      TemplateContext context )
        throws Exception
    {
        ScarabSecurity security = SecurityFactory.getInstance();
        // If it's a global query, user must have Query | Approve permission
        // Or its Approved field gets set to false
        if (getQueryType().getQueryTypeId().equals(USER__PK))
        {
            setApproved(true);
        }
        else
        {
            setApproved(security.hasPermission(ScarabSecurity.ITEM__APPROVE, 
                                               user, module));
        } 

        if (!security.hasPermission(ScarabSecurity.ITEM__APPROVE, 
                                    user, module))
        {
            // Send Email
            // add data to context for email template
            context.put("user", user);
            context.put("module", module);

            String subject = "New query requires approval";
            String template = Turbine.getConfiguration().
                getString("scarab.email.requireapproval.template",
                          "email/RequireApproval.vm");
            ScarabUser toUser = (ScarabUser) ScarabUserImplPeer
                              .retrieveByPK((NumberKey)module.getOwnerId());
            Email.sendEmail(new ContextAdapter(context), null, toUser,
                            subject, template);
        }
        super.save();
    }

    /**
     * Generates link to Issue List page, re-running stored query.
     */
    public String getExecuteLink(String link) 
    {
       return link 
          + "/template/IssueList.vm?action=Search&eventSubmit_doSearch=Search" 
          + "&resultsperpage=25&pagenum=1" + getValue();
    }

    /**
     * Generates link to the Query Detail page.
     */
    public String getEditLink(String link) 
    {
        return link + "/template/EditQuery.vm?queryId=" + getQueryId()
                    + getValue();
    }

    /**
     * Returns list of all query types.
     */
    public List getAllQueryTypes() throws Exception
    {
        return QueryTypePeer.doSelect(new Criteria());
    }

    /**
     * Checks if user has permission to approve query. 
     */
    public void setApproved( ScarabUser user, ScarabModule module,
                            boolean approved, RunData data)
         throws Exception
    {                
        boolean hasPerm = false;
        ScarabSecurity security = SecurityFactory.getInstance();

        if (security.hasPermission(ScarabSecurity.ITEM__APPROVE, user,
                                   module))
        {
            super.setApproved(approved);
            super.save();
        } 
        else
        {
            data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
        }
            
    }

    /**
     * Checks if user has permission to reject query.
     */
    public void setDeleted( ScarabUser user, ScarabModule module,
                            boolean deleted, RunData data)
         throws Exception
    {                
        boolean hasPerm = false;
        ScarabSecurity security = SecurityFactory.getInstance();

        if (security.hasPermission(ScarabSecurity.ITEM__APPROVE, user,
                                   module))
        {
            super.setDeleted(deleted);
            super.save();
        } 
        else
        {
            data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
        }
            
    }

}
