package org.tigris.scarab.baseom;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.BaseObject;
import org.apache.turbine.om.peer.BasePeer;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** This class was autogenerated by GenerateMapBuilder on: Tue Jan 02 21:50:40 PST 2001 */
public class ScarabAttributeClass extends BaseObject
{
    /** the value for the attribute_class_id field */
    private int attribute_class_id;
    /** the value for the attribute_class_name field */
    private String attribute_class_name;
    /** the value for the attribute_class_desc field */
    private String attribute_class_desc;
    /** the value for the java_class_name field */
    private String java_class_name;


    /**
     * Get the AttributeClassId
     * @return int
     */
     public int getAttributeClassId()
     {
          return attribute_class_id;
     }

                            
    /**
     * Set the value of AttributeClassId
     */
     public void setAttributeClassId(int v ) throws Exception
     {
  
       
        
                
          // update associated ScarabAttributeType
          if (collScarabAttributeTypes != null )
          {
              for (int i=0; i<collScarabAttributeTypes.size(); i++)
              {
                  ((ScarabAttributeType)collScarabAttributeTypes.elementAt(i))
                      .setClassId(v);
              }
          }
       

            if (this.attribute_class_id != v)
           {
              this.attribute_class_id = v;
              setModified(true);
          }
     }
    /**
     * Get the Name
     * @return String
     */
     public String getName()
     {
          return attribute_class_name;
     }

        
    /**
     * Set the value of Name
     */
     public void setName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attribute_class_name, v) )

           {
              this.attribute_class_name = v;
              setModified(true);
          }
     }
    /**
     * Get the Desc
     * @return String
     */
     public String getDesc()
     {
          return attribute_class_desc;
     }

        
    /**
     * Set the value of Desc
     */
     public void setDesc(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.attribute_class_desc, v) )

           {
              this.attribute_class_desc = v;
              setModified(true);
          }
     }
    /**
     * Get the JavaClassName
     * @return String
     */
     public String getJavaClassName()
     {
          return java_class_name;
     }

        
    /**
     * Set the value of JavaClassName
     */
     public void setJavaClassName(String v ) 
     {
  
  

           if ( !ObjectUtils.equals(this.java_class_name, v) )

           {
              this.java_class_name = v;
              setModified(true);
          }
     }

 
    
                
      
    /**
     * Collection to store aggregation of collScarabAttributeTypes
     */
    private Vector collScarabAttributeTypes;
    /**
     * Temporary storage of collScarabAttributeTypes to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
//    private Vector tempcollScarabAttributeTypes;

    public void initScarabAttributeTypes()
    {
        if (collScarabAttributeTypes == null)
            collScarabAttributeTypes = new Vector();
    }

    /**
     * Method called to associate a ScarabAttributeType object to this object
     * through the ScarabAttributeType foreign key attribute
     *
     * @param ScarabAttributeType l
     */
    public void addScarabAttributeTypes(ScarabAttributeType l) throws Exception
    {
        /*
        if (collScarabAttributeTypes == null)
        {
            if (tempcollScarabAttributeTypes == null)
            {
                tempcollScarabAttributeTypes = new Vector();
            }
            tempcollScarabAttributeTypes.add(l);
        }
        else
        {
            collScarabAttributeTypes.add(l);
        }
        */
        getScarabAttributeTypes().add(l);
        l.setScarabAttributeClass(this);
    }

    /**
     * The criteria used to select the current contents of collScarabAttributeTypes
     */
    private Criteria lastScarabAttributeTypesCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of 
     * getScarabAttributeTypes(new Criteria())
     */
    public Vector getScarabAttributeTypes() throws Exception
    {
        if (collScarabAttributeTypes == null)
        {
            collScarabAttributeTypes = getScarabAttributeTypes(new Criteria(10));
        }
        return collScarabAttributeTypes;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection. 
     * Otherwise if this ScarabAttributeClass is new, it will return
     * an empty collection; or if this ScarabAttributeClass has previously
     * been saved, it will retrieve related ScarabAttributeTypes from storage.
     */
    public Vector getScarabAttributeTypes(Criteria criteria) throws Exception
    {
        if (collScarabAttributeTypes == null)
        {
            if ( isNew() ) 
            {
               collScarabAttributeTypes = new Vector();       
            } 
            else
            {
                   criteria.add(ScarabAttributeTypePeer.ATTRIBUTE_CLASS_ID, getAttributeClassId() );               
                   collScarabAttributeTypes = ScarabAttributeTypePeer.doSelect(criteria);
            }
/*
            if (tempcollScarabAttributeTypes != null)
            {
                for (int i=0; i<tempcollScarabAttributeTypes.size(); i++)
                {
                    collScarabAttributeTypes.add(tempcollScarabAttributeTypes.get(i));
                }
                tempcollScarabAttributeTypes = null;
            }
*/
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                   criteria.add(ScarabAttributeTypePeer.ATTRIBUTE_CLASS_ID, getAttributeClassId() );               
               if ( !lastScarabAttributeTypesCriteria.equals(criteria)  )
            {
                collScarabAttributeTypes = ScarabAttributeTypePeer.doSelect(criteria);  
            }
        }
        lastScarabAttributeTypesCriteria = criteria; 

        return collScarabAttributeTypes;
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
                ScarabAttributeClassPeer.getMapBuilder()
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
                ScarabAttributeClassPeer.doInsert(this, dbCon);
            }
            else
            {
                ScarabAttributeClassPeer.doUpdate(this, dbCon);
                setNew(false);
            }
        }

                                    
                
          if (collScarabAttributeTypes != null )
          {
              for (int i=0; i<collScarabAttributeTypes.size(); i++)
              {
                  ((ScarabAttributeType)collScarabAttributeTypes.elementAt(i)).save(dbCon);
              }
          }
                  alreadyInSave = false;
      }
      }

                                                
    /** 
     * Set the Id using pk values.
     *
     * @param int attribute_class_id
     */
    public void setId(
                      int attribute_class_id
                                                                 ) throws Exception
    {
                     setAttributeClassId(attribute_class_id);
                                                    }


    /** 
     * Set the Id using a : separated String of pk values.
     */
    public void setId(Object id) throws Exception
    {
        StringTokenizer st = new StringTokenizer(id.toString(), ":");
                           setAttributeClassId( Integer.parseInt(st.nextToken()) );
                                                            }


    /** 
     * returns an id that differentiates this object from others
     * of its class.
     */
    public Object getId() 
    {
        return ""
                      + getAttributeClassId()
                                                                 ;
    } 

    /** 
     * returns an id that can be used to specify this object in
     * a query string.
     */
    public String getQueryOID() 
    {
        return "ScarabAttributeClass[" + getId() + "]";
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
      * It then fills all the association collections and sets the
     * related objects to isNew=true.
      */
    public ScarabAttributeClass copy() throws Exception
    {
        ScarabAttributeClass copyObj = new ScarabAttributeClass();
         copyObj.setAttributeClassId(attribute_class_id);
         copyObj.setName(attribute_class_name);
         copyObj.setDesc(attribute_class_desc);
         copyObj.setJavaClassName(java_class_name);
 
                                  
                
         Vector v = copyObj.getScarabAttributeTypes();
         for (int i=0; i<v.size(); i++)
         {
             ((BaseObject)v.elementAt(i)).setNew(true);
         }
         
                        
        copyObj.setAttributeClassId(NEW_ID);
                                return copyObj;
    }             

}
