package org.tigris.scarab.om;

import java.util.Vector;
import org.apache.turbine.RunData;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.turbine.TemplateContext;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.security.ScarabSecurityPull;
import org.tigris.scarab.tools.ScarabRequestTool;
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

    public void save(  TemplateContext context, ScarabUser user )
        throws Exception
    {
        ScarabSecurityPull security = (ScarabSecurityPull)context
            .get(ScarabConstants.SECURITY_TOOL);
        ScarabRequestTool scarabR = (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
        if (security.hasPermission(ScarabSecurityPull.QUERY__APPROVE, 
                                   scarabR.getCurrentModule()))
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
