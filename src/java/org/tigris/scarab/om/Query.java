package org.tigris.scarab.om;

import java.util.Vector;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class Query 
    extends org.tigris.scarab.om.BaseQuery
    implements Persistent
{

    public static final NumberKey GLOBAL__PK = new NumberKey("2");

    /**
     * A new Query object
     */
    public static Query getInstance() 
    {
        return new Query();
    }

    public void save(  ScarabUser user, ModuleEntity module )
        throws Exception
    {
        ScarabSecurity security = SecurityFactory.getInstance();
        if (security.hasPermission(ScarabSecurity.QUERY__APPROVE, 
                                   user, module))
        {
            setApproved(user.getUserId());
        } 
        else
        {
            setApproved(new NumberKey(0));
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
    public Vector getAllQueryTypes() throws Exception
    {
        return QueryTypePeer.doSelect(new Criteria());
    }

}
