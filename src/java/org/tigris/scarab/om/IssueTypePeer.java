package org.tigris.scarab.om;

import java.util.*;
import com.workingdogs.village.*;
import org.apache.torque.map.*;
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

    /**
     *  Gets a List of all of the Issue types in the database,
     *  That are not template types.
     */
    public static List getAllIssueTypes(boolean includeDeleted)
        throws Exception
    {
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
            result = doSelect(c);
            ScarabCache.put(result, ISSUE_TYPE_PEER, GET_ALL_ISSUE_TYPES, b);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
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
