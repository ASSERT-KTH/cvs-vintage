
package org.tigris.scarab.om;

import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.manager.CacheListener;
import org.apache.torque.om.Persistent;

/** 
 * This class manages Query objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class QueryManager
    extends BaseQueryManager
    implements CacheListener
{
    /**
     * Creates a new <code>IssueManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public QueryManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        //List listeners = (List)listenersMap
        //    .get(AttributeTypePeer.ATTRIBUTE_TYPE_ID);
        //notifyListeners(listeners, oldOm, om);
        getMethodResult().removeAll(QueryPeer.QUERY_PEER, 
                                    QueryPeer.GET_QUERIES);
        getMethodResult().removeAll(QueryPeer.QUERY_PEER, 
                                    QueryPeer.GET_USER_QUERIES);
        getMethodResult().removeAll(QueryPeer.QUERY_PEER, 
                                    QueryPeer.GET_MODULE_QUERIES);
        return oldOm;
    }


    public Query getInstanceImpl()
    {
        return new Query();
    }
    // -------------------------------------------------------------------
    // CacheListener implementation

    public void addedObject(Persistent om)
    {
        if (om instanceof Query)
        {
            Query castom = (Query)om;
            ObjectKey key = castom.getQueryId();
            Serializable obj = (Serializable)cacheGet(key);
            if (obj != null) 
            {
                getMethodResult().remove(obj, 
                    QueryPeer.GET_QUERIES);
                getMethodResult().remove(obj, 
                    QueryPeer.GET_USER_QUERIES);
                getMethodResult().remove(obj, 
                    QueryPeer.GET_MODULE_QUERIES);
            }
        }
    }

    public void refreshedObject(Persistent om)
    {
        addedObject(om);
    }

    /** fields which interest us with respect to cache events */
    public List getInterestedFields()
    {
        List interestedCacheFields = new LinkedList();
        interestedCacheFields.add(QueryPeer.QUERY_ID);
        return interestedCacheFields;
    }
}





