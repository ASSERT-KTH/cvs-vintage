package org.tigris.scarab.om;

import java.util.*;
//import com.workingdogs.village.*;
//import org.apache.torque.map.*;
import org.apache.torque.TorqueException;
import org.apache.torque.pool.DBConnection;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.ObjectKey;

// Local classes
import org.tigris.scarab.om.map.*;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class IssueTypePeer 
    extends org.tigris.scarab.om.BaseIssueTypePeer
{
    private static final String ISSUE_TYPE_PEER = 
        "IssueTypePeer";
    private static final String GET_ALL_ISSUE_TYPES = 
        "getAllIssueTypes";

    private static final String RETRIEVE_BY_PK = 
        "retrieveByPK";

    /** 
     * Retrieve a single object by pk
     *
     * @param ObjectKey pk
     */
    public static IssueType retrieveByPK( ObjectKey pk )
        throws TorqueException
    {
        IssueType result = null;
        Object obj = ScarabCache.get(ISSUE_TYPE_PEER, RETRIEVE_BY_PK, pk); 
        if ( obj == null ) 
        {        
            result = BaseIssueTypePeer.retrieveByPK(pk);
            ScarabCache.put(result, ISSUE_TYPE_PEER, RETRIEVE_BY_PK, pk);
        }
        else 
        {
            result = (IssueType)obj;
        }
        return result;
    }

    /**
     *  Gets a List of all of the Issue types in the database,
     *  That are not template types.
     */
    public static List getAllIssueTypes(boolean includeDeleted, 
                       String sortColumn, String sortPolarity)
        throws Exception
    {
System.out.println(includeDeleted);
        List result = null;
        Boolean b = includeDeleted ? Boolean.TRUE : Boolean.FALSE;
        Object obj = ScarabCache.get(ISSUE_TYPE_PEER, GET_ALL_ISSUE_TYPES, b); 
        if ( obj == null ) 
        {        
            Criteria c = new Criteria();
            c.add(IssueTypePeer.PARENT_ID, 0);
            if (!includeDeleted)
            {
                c.add(IssueTypePeer.DELETED, 0);
            }
            if (sortColumn != null && sortColumn.equals("desc"))
            {
                addSortOrder(c, IssueTypePeer.DESCRIPTION, 
                             sortPolarity);
            }
            else
            {
                // sort on name
                addSortOrder(c, IssueTypePeer.NAME, 
                             sortPolarity);
            }
            result = doSelect(c);
            ScarabCache.put(result, ISSUE_TYPE_PEER, GET_ALL_ISSUE_TYPES, b);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    public static List getAllIssueTypes(boolean includeDeleted)
        throws Exception
    {
        return getAllIssueTypes(includeDeleted, "name", "asc");
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

    /**
     * Checks to see if the name already exists an issue type.  if one
     * does unique will be false unless the given id matches the issue type
     * that already has the given name.
     *
     * @param name a <code>String</code> value
     * @param id an <code>ObjectKey</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public static boolean isUnique(String name, ObjectKey id)
        throws Exception
    {
        boolean unique = false;
        Criteria crit = new Criteria().add(IssueTypePeer.NAME, name);
        crit.setIgnoreCase(true);
        List types = IssueTypePeer.doSelect(crit);
        if ( types.size() == 0 ) 
        {
            unique = true;
        }
        else 
        {
            IssueType it = (IssueType)types.get(0);
            if ( id != null && it.getPrimaryKey().equals(id) ) 
            {
                unique = true;
            }
        }
        return unique;
    }
}
