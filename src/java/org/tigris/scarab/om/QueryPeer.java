package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.cache.ScarabCache;

// Local classes
import org.tigris.scarab.services.module.ModuleEntity;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class QueryPeer 
    extends org.tigris.scarab.om.BaseQueryPeer
{

    private static final String GET_ALL_QUERIES = 
        "getAllQueries";

    /**
     * List of private queries associated with this module.
     * Created by this user.
     */
    public static List getAllQueries(ModuleEntity me, IssueType issueType,
                                     ScarabUser user, String sortColumn,   
                                     String sortPolarity)
        throws Exception
    {
        List queries = null;
        Object obj = ScarabCache.get("QueryPeer", GET_ALL_QUERIES, 
                                     user, issueType); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(QueryPeer.MODULE_ID, me.getModuleId())
                .add(QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId())
                .add(QueryPeer.DELETED, 0);

            Criteria.Criterion cPriv1 = crit.getNewCriterion(
                QueryPeer.SCOPE_ID, Scope.PERSONAL__PK, 
                Criteria.EQUAL);
            cPriv1.and( crit.getNewCriterion(TransactionPeer.CREATED_BY, 
                user.getUserId(),  Criteria.EQUAL));
            Criteria.Criterion cGlob = crit.getNewCriterion(
                QueryPeer.SCOPE_ID, Scope.GLOBAL__PK,
                Criteria.EQUAL);
            cGlob.or(cPriv1);
            crit.add(cGlob);
            crit.setDistinct();

            // Add sort criteria
            if (sortColumn.equals("desc"))
            {
                addSortOrder(crit, QueryPeer.DESCRIPTION, 
                             sortPolarity);
            }
            else if (sortColumn.equals("avail"))
            {
                crit.addJoin(QueryPeer.SCOPE_ID,
                             ScopePeer.SCOPE_ID);
                addSortOrder(crit, ScopePeer.SCOPE_NAME, sortPolarity);
            }
            else if (sortColumn.equals("user"))
            {
                addSortOrder(crit, QueryPeer.USER_ID, sortPolarity);
            }
            else
            {
                // sort by name
                addSortOrder(crit, QueryPeer.NAME, sortPolarity);
            }
            queries = QueryPeer.doSelect(crit);
            ScarabCache.put(queries, "QueryPeer", 
                            GET_ALL_QUERIES, user, issueType);
        }
        else 
        {
            queries = (List)obj;
        }
        return queries;
    }


    private static Criteria addSortOrder(Criteria crit, 
                       String sortColumn, String sortPolarity)
    {
        if (sortPolarity.equals("desc"))
        {
            crit.addDescendingOrderByColumn(sortColumn);
        }
        else
        {
            crit.addAscendingOrderByColumn(sortColumn);
        }
        return crit;
    }

}
