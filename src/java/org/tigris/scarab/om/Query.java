package org.tigris.scarab.om;

import java.util.List;

import org.apache.fulcrum.template.TemplateContext;
import org.apache.turbine.Turbine;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;

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

    public void saveAndSendEmail( ScarabUser user, ModuleEntity module, 
                                  TemplateContext context )
        throws Exception
    {
        ScarabSecurity security = SecurityFactory.getInstance();
        // If it's a global query, user must have Item | Approve 
        //   permission, Or its Approved field gets set to false
        if (getQueryType().getQueryTypeId().equals(USER__PK))
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

            // Send Email to module owner to approve new query
            if (context != null) {
                context.put("user", user);
                context.put("module", module);

                String subject = "New query requires approval";
                String template = Turbine.getConfiguration().
                    getString("scarab.email.requireapproval.template",
                              "email/RequireApproval.vm");
                ScarabUser toUser = (ScarabUser) ScarabUserImplPeer
                                  .retrieveByPK((NumberKey)module.getOwnerId());
                Email.sendEmail(context, null, toUser,
                                subject, template);
            }
        }
        save();
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
     * Returns list of all frequency values.
     */
    public List getFrequencies() throws Exception
    {
        return FrequencyPeer.doSelect(new Criteria());
    }

    /**
     * Returns list of subscribed users.
     */
    public List getSubscribedUsers() throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RQueryUserPeer.QUERY_ID, getQueryId());
        return RQueryUserPeer.doSelect(crit);
    }

    /**
     * Returns true if passed-in user is subscribed to query.
     */
    public boolean isUserSubscribed(NumberKey userId) throws Exception
    {
        boolean isSubscribed = false;
        List queryUsers = getSubscribedUsers(); 
        for (int i=0;i<queryUsers.size(); i++)
        {
            if (((RQueryUser)queryUsers.get(i))
                             .getScarabUserImpl().getUserId().equals(userId))
            {
                 isSubscribed = true;
                 break;
            }
        }
        return isSubscribed;
    }

    /**
     * Gets RQueryUser object for this query and user.
     */
    public RQueryUser getSubscription(NumberKey userId) throws Exception
    {
        RQueryUser rqu = new RQueryUser();
        if (isUserSubscribed(userId))
        {
            Criteria crit = new Criteria();
            crit.add(RQueryUserPeer.QUERY_ID, getQueryId());
            crit.add(RQueryUserPeer.USER_ID, userId);
            rqu = (RQueryUser)RQueryUserPeer.doSelect(crit).get(0);
        }
        return rqu;
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
     * Checks if user has permission to delete query.
     * Only the creating user can delete a personal query.
     * Only project owner or admin can delete a project-wide query.
     */
    public void delete( ScarabUser user )
         throws Exception
    {                
        ScarabModule module = getScarabModule();
        ScarabSecurity security = SecurityFactory.getInstance();

        if (security.hasPermission(ScarabSecurity.ITEM__APPROVE, user, module)
          || (user.getUserId().equals(getUserId()) && getQueryType().equals(USER__PK)))
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
