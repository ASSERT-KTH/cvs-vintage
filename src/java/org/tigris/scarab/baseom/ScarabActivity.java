package org.tigris.scarab.baseom;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.BaseObject;
import org.apache.turbine.om.peer.BasePeer;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** This class was autogenerated by GenerateMapBuilder on: Mon Jan 08 11:17:17 PST 2001 */
public class ScarabActivity extends BaseObject
{
    /** the value for the issue_id field */
    private int issue_id;
    /** the value for the attribute_id field */
    private int attribute_id;
    /** the value for the transaction_id field */
    private int transaction_id;
    /** the value for the old_value field */
    private String old_value;
    /** the value for the new_value field */
    private String new_value;


    /**
     * Get the IssueId
     * @return int
     */
     public int getIssueId()
     {
          return issue_id;
     }

            
    /**
     * Set the value of IssueId
     */
     public void setIssueId(int v ) throws Exception
     {
                  if ( aScarabIssue != null && !aScarabIssue.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

            if (this.issue_id != v)
           {
              this.issue_id = v;
              setModified(true);
          }
     }
    /**
     * Get the AttributeId
     * @return int
     */
     public int getAttributeId()
     {
          return attribute_id;
     }

            
    /**
     * Set the value of AttributeId
     */
     public void setAttributeId(int v ) throws Exception
     {
                  if ( aScarabAttribute != null && !aScarabAttribute.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

            if (this.attribute_id != v)
           {
              this.attribute_id = v;
              setModified(true);
          }
     }
    /**
     * Get the TransactionId
     * @return int
     */
     public int getTransactionId()
     {
          return transaction_id;
     }

            
    /**
     * Set the value of TransactionId
     */
     public void setTransactionId(int v ) throws Exception
     {
                  if ( aScarabTransaction != null && !aScarabTransaction.isNew())
        {
            throw new Exception("Can't set a foreign key directly after an "
                + " association is already made based on saved data.");
        }
  
  

            if (this.transaction_id != v)
           {
              this.transaction_id = v;
              setModified(true);
          }
     }
    /**
     * Get the OldValue
     * @return String
     */
     public String getOldValue()
     {
          return old_value;
     }

        
    /**
     * Set the value of OldValue
     */
     public void setOldValue(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.old_value, v) )

           {
              this.old_value = v;
              setModified(true);
          }
     }
    /**
     * Get the NewValue
     * @return String
     */
     public String getNewValue()
     {
          return new_value;
     }

        
    /**
     * Set the value of NewValue
     */
     public void setNewValue(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.new_value, v) )

           {
              this.new_value = v;
              setModified(true);
          }
     }

 
 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabIssue object
     *
     * @param ScarabIssue v
     */
    private ScarabIssue aScarabIssue;
    public void setScarabIssue(ScarabIssue v) throws Exception
    {
        aScarabIssue = null;
           setIssueId(v.getIssueId());
           aScarabIssue = v;
    }

                     
    public ScarabIssue getScarabIssue() throws Exception
    {
        if ( aScarabIssue==null && (this.issue_id>0) )
        {
            aScarabIssue = ScarabIssuePeer.retrieveByPK(this.issue_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabIssue obj = ScarabIssuePeer.retrieveByPK(this.issue_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabIssue;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabAttribute object
     *
     * @param ScarabAttribute v
     */
    private ScarabAttribute aScarabAttribute;
    public void setScarabAttribute(ScarabAttribute v) throws Exception
    {
        aScarabAttribute = null;
           setAttributeId(v.getAttributeId());
           aScarabAttribute = v;
    }

                     
    public ScarabAttribute getScarabAttribute() throws Exception
    {
        if ( aScarabAttribute==null && (this.attribute_id>0) )
        {
            aScarabAttribute = ScarabAttributePeer.retrieveByPK(this.attribute_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabAttribute obj = ScarabAttributePeer.retrieveByPK(this.attribute_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabAttribute;
    }

 
   
             
   
   
    /**
     * Declares an association between this object and a ScarabTransaction object
     *
     * @param ScarabTransaction v
     */
    private ScarabTransaction aScarabTransaction;
    public void setScarabTransaction(ScarabTransaction v) throws Exception
    {
        aScarabTransaction = null;
           setTransactionId(v.getTransactionId());
           aScarabTransaction = v;
    }

                     
    public ScarabTransaction getScarabTransaction() throws Exception
    {
        if ( aScarabTransaction==null && (this.transaction_id>0) )
        {
            aScarabTransaction = ScarabTransactionPeer.retrieveByPK(this.transaction_id);
            // The following can be used instead of the line above to
            // guarantee the related object contains a reference
            // to this object, but this level of coupling 
            // may be undesirable in many circumstances.
            // As it can lead to a db query with many results that may
            // never be used.  
            // ScarabTransaction obj = ScarabTransactionPeer.retrieveByPK(this.transaction_id);
            // obj.addScarabActivitys(this);
        }
        return aScarabTransaction;
    }

    
        
    


    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     */
    public void save() throws Exception
    {
         DBConnection dbCon = null;
        try
        {
            dbCon = BasePeer.beginTransaction(
                ScarabActivityPeer.getMapBuilder()
                .getDatabaseMap().getName());
            save(dbCon);
        }
        catch(Exception e)
        {
            BasePeer.rollBackTransaction(dbCon);
            throw e;
        }
        BasePeer.commitTransaction(dbCon);

     }

      // flag to prevent endless save loop, if this object is referenced
    // by another object which falls in this transaction.
    private boolean alreadyInSave = false;
      /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     */
    public void save(DBConnection dbCon) throws Exception
    {
        if (!alreadyInSave)
      {
        alreadyInSave = true;
          if (isModified())
        {
            if (isNew())
            {
                ScarabActivityPeer.doInsert(this, dbCon);
            }
            else
            {
                ScarabActivityPeer.doUpdate(this, dbCon);
                setNew(false);
            }
        }

              alreadyInSave = false;
      }
      }

                                                                    
    /** 
     * Set the Id using pk values.
     *
     * @param int issue_id
     * @param int attribute_id
     * @param int transaction_id
     */
    public void setPrimaryKey(
                      int issue_id
                                      , int attribute_id
                                      , int transaction_id
                                                     ) throws Exception
    {
                     setIssueId(issue_id);
                             setAttributeId(attribute_id);
                             setTransactionId(transaction_id);
                                        }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setPrimaryKey(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                     setIssueId( Integer.parseInt(st.nextToken()) );
                              setAttributeId( Integer.parseInt(st.nextToken()) );
                              setTransactionId( Integer.parseInt(st.nextToken()) );
                  }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getPrimaryKey() 
    {
        return ""
                      + getIssueId()
                                      + ":"  + getAttributeId()
                                      + ":"  + getTransactionId()
                                                     ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabActivity[" + getPrimaryKey() + "]";
    }

    public static Vector getScarabActivitys(ParameterParser pp)
        throws Exception
    {
        Vector v = new Vector();
        // look for classname in keys
        Enumeration keys = pp.keys();
        while ( keys.hasMoreElements() )
        {
            String ppKey = (String)keys.nextElement();
            String[] parsedString = StringUtils.parseObjectKey(ppKey);
            if ("ScarabActivity".equalsIgnoreCase(
                     parsedString[StringUtils.PPKEY_CLASSNAME]))
            {
                ScarabActivity obj = null;
                if (parsedString[StringUtils.PPKEY_ID].equals("PK") )
                {
                    obj = new ScarabActivity();
                    v.addElement(obj);
                    StringTokenizer st = new StringTokenizer(
                        parsedString[StringUtils.PPKEY_PROPERTY], ":");
  
                          obj.setIssueId(Integer.parseInt(st.nextToken()));
        
                          obj.setAttributeId(Integer.parseInt(st.nextToken()));
        
                          obj.setTransactionId(Integer.parseInt(st.nextToken()));
                        }
                else
                {
                    StringTokenizer st = new StringTokenizer(
                        parsedString[StringUtils.PPKEY_ID], ":");
  
                          int issue_id = Integer.parseInt(st.nextToken());
        
                          int attribute_id = Integer.parseInt(st.nextToken());
        
                          int transaction_id = Integer.parseInt(st.nextToken());
        

                                  
                    for ( int i=0; i<v.size(); i++) 
                    {
                        ScarabActivity tempObj = 
                            (ScarabActivity)v.get(i);
                        if ( issue_id == tempObj.getIssueId() && attribute_id == tempObj.getAttributeId() && transaction_id == tempObj.getTransactionId() ) 
                        {
                            obj = tempObj;
                            break;
                        }
                    }
                    if ( obj == null ) 
                    {
                        obj = new ScarabActivity();
                        obj.setIssueId(issue_id);
                        obj.setAttributeId(attribute_id);
                        obj.setTransactionId(transaction_id);
                        v.addElement(obj);
                    }
                }
                String property = parsedString[StringUtils.PPKEY_PROPERTY];

                                if ( property.equalsIgnoreCase("OldValue") ) 
                {
                    obj.setOldValue(pp.getString(ppKey));
                }
                        if ( property.equalsIgnoreCase("NewValue") ) 
                {
                    obj.setNewValue(pp.getString(ppKey));
                }
                }
        }
        return v;
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabActivity copy() throws Exception
    {
        ScarabActivity copyObj = new ScarabActivity();
         copyObj.setIssueId(issue_id);
         copyObj.setAttributeId(attribute_id);
         copyObj.setTransactionId(transaction_id);
         copyObj.setOldValue(old_value);
         copyObj.setNewValue(new_value);
 
  
                        
        copyObj.setIssueId(NEW_ID);
                              
        copyObj.setAttributeId(NEW_ID);
                              
        copyObj.setTransactionId(NEW_ID);
                         return copyObj;
    }

}
