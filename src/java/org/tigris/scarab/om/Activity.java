package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
 import org.apache.turbine.om.*;
// import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
// import org.apache.turbine.util.Log;
// import org.apache.turbine.util.db.pool.DBConnection;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class Activity 
    extends BaseActivity
    implements Persistent
{
    private Attribute aAttribute;                 
    private Transaction aTransaction;                 
    private Attachment aAttachment;                 
    private AttributeOption oldAttributeOption;                 
    private AttributeOption newAttributeOption;                 


    /**
     * Gets the Attribute that was changed for this Activity record.
     */
    public Attribute getAttribute() throws Exception
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = Attribute.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    /**
     * Sets the Attribute that was changed for this Activity record.
     */
    public void setAttribute(Attribute v) throws Exception
    {
        aAttribute = v;
        super.setAttribute(v);
    }

    /**
     * Gets the Transaction object associated with this Activity record
     */
    public Transaction getTransaction() throws Exception
    {
        if ( aTransaction==null && (getTransactionId() != null) )
        {
            aTransaction = TransactionPeer.retrieveByPK(new NumberKey(getTransactionId()));
            
            // make sure the parent attribute is in synch.
            super.setTransaction(aTransaction);            
        }
        return aTransaction;
    }

    /**
     * Sets the Transaction object associated with this Activity record
     */
    public void setTransaction(Transaction v) throws Exception
    {
        aTransaction = v;
        super.setTransaction(v);
    }

    /**
     * Gets the Attachment associated with this Activity record
     */
    public Attachment getAttachment() throws Exception
    {
        if ( aAttachment==null && (getAttachmentId() != null) )
        {
            aAttachment = AttachmentPeer.retrieveByPK(new NumberKey(getAttachmentId()));
            
            // make sure the parent attribute is in synch.
            super.setAttachment(aAttachment);            
        }
        return aAttachment;
    }

    /**
     * Sets the Attachment associated with this Activity record
     */
    public void setAttachment(Attachment v) throws Exception
    {
        aAttachment = v;
        super.setAttachment(v);
    }


    /**
     * Gets the AttributeOption object associated with the Old Value field
     * (i.e., the old value for the attribute before the change.)
     */
    public AttributeOption getOldAttributeOption() throws Exception
    {
        if ( oldAttributeOption==null && (getOldValue() != null) )
        {
            oldAttributeOption = AttributeOptionPeer.retrieveByPK(new NumberKey(getOldValue()));
        }
        return oldAttributeOption;
    }

    /**
     * Sets the Old Attribute Option associated with this Activity record
    public void setOldAttributeOption(AttributeOption v) throws Exception
    {
        oldAttributeOption  = v;
        super.setOldValue(v);
    }
     */

    /**
     * Gets the AttributeOption object associated with the New Value field
     * (i.e., the new value for the attribute after the change.)
     */
    public AttributeOption getNewAttributeOption() throws Exception
    {
        if ( newAttributeOption==null && (getNewValue() != null) )
        {
            newAttributeOption = AttributeOptionPeer.retrieveByPK(new NumberKey(getNewValue()));
        }
        return newAttributeOption;
    }

    /**
     * Sets the New Attribute Option associated with this Activity record
    public void setNewAttributeOption(AttributeOption v) throws Exception
    {
        newAttributeOption  = v;
        super.setNewValue(v);
    }
     */

}



