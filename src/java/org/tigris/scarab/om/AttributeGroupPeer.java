package org.tigris.scarab.om;


// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;

// Scarab classes
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class AttributeGroupPeer 
    extends org.tigris.scarab.om.BaseAttributeGroupPeer
{
    private static final String ATTRIBUTEGROUP_PEER = 
        "AttributeGroupPeer";
    private static final String RETRIEVE_BY_PK = 
        "retrieveByPK";

    /** 
     * Retrieve a single object by pk
     *
     * @param ObjectKey pk
     */
    public static AttributeGroup retrieveByPK(ObjectKey pk)
        throws TorqueException
    {
        AttributeGroup result = null;
        Object obj = ScarabCache.get(ATTRIBUTEGROUP_PEER, RETRIEVE_BY_PK, pk); 
        if (obj == null) 
        {        
            result = BaseAttributeGroupPeer.retrieveByPK(pk);
            ScarabCache.put(result, ATTRIBUTEGROUP_PEER, RETRIEVE_BY_PK, pk);
        }
        else 
        {
            result = (AttributeGroup)obj;
        }
        return result;
    }
}
