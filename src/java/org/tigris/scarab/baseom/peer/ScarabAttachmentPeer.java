package org.tigris.scarab.baseom.peer;

// JDK classes
import java.util.*;

// Village classes
import com.workingdogs.village.*;

// Turbine classes
import org.apache.turbine.om.peer.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.db.map.*;
import org.apache.turbine.util.db.pool.DBConnection;

import org.tigris.scarab.baseom.ScarabAttachment;

// Local classes
import org.tigris.scarab.baseom.map.*;
import org.tigris.scarab.baseom.*;

/** This class was autogenerated by GenerateMapBuilder on: Tue Jan 02 21:50:40 PST 2001 */
public class ScarabAttachmentPeer extends BasePeer
{
    /** the mapbuilder for this class */
    private static final ScarabAttachmentMapBuilder mapBuilder = 
        (ScarabAttachmentMapBuilder)getMapBuilder(ScarabAttachmentMapBuilder.CLASS_NAME);

    /** the table name for this class */
    public static final String TABLE_NAME = mapBuilder.getTable();

    /** the column name for the ATTACHMENT_ID field */
    public static final String ATTACHMENT_ID = mapBuilder.getScarabAttachment_AttachmentId();
    /** the column name for the ISSUE_ID field */
    public static final String ISSUE_ID = mapBuilder.getScarabAttachment_IssueId();
    /** the column name for the ATTACHMENT_TYPE_ID field */
    public static final String ATTACHMENT_TYPE_ID = mapBuilder.getScarabAttachment_TypeId();
    /** the column name for the ATTACHMENT_NAME field */
    public static final String ATTACHMENT_NAME = mapBuilder.getScarabAttachment_Name();
    /** the column name for the ATTACHMENT_DATA field */
    public static final String ATTACHMENT_DATA = mapBuilder.getScarabAttachment_Data();
    /** the column name for the ATTACHMENT_FILE_PATH field */
    public static final String ATTACHMENT_FILE_PATH = mapBuilder.getScarabAttachment_FilePath();
    /** the column name for the ATTACHMENT_MIME_TYPE field */
    public static final String ATTACHMENT_MIME_TYPE = mapBuilder.getScarabAttachment_MimeType();
    /** the column name for the MODIFIED_BY field */
    public static final String MODIFIED_BY = mapBuilder.getScarabAttachment_ModifiedBy();
    /** the column name for the CREATED_BY field */
    public static final String CREATED_BY = mapBuilder.getScarabAttachment_CreatedBy();
    /** the column name for the MODIFIED_DATE field */
    public static final String MODIFIED_DATE = mapBuilder.getScarabAttachment_ModifiedDate();
    /** the column name for the CREATED_DATE field */
    public static final String CREATED_DATE = mapBuilder.getScarabAttachment_CreatedDate();
    /** the column name for the DELETED field */
    public static final String DELETED = mapBuilder.getScarabAttachment_Deleted();

    /** number of columns for this peer */
    public static final int numColumns =  12;;

    /** Method to do inserts */
    public static Object doInsert( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                                                                                // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                      return BasePeer.doInsert( criteria );
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Object doInsert( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                                                                                // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                      return BasePeer.doInsert( criteria, dbCon );
    }

    /** Add all the columns needed to create a new object */
    public static void addSelectColumns (Criteria criteria) throws Exception
    {
            criteria.addSelectColumn( ATTACHMENT_ID );
            criteria.addSelectColumn( ISSUE_ID );
            criteria.addSelectColumn( ATTACHMENT_TYPE_ID );
            criteria.addSelectColumn( ATTACHMENT_NAME );
            criteria.addSelectColumn( ATTACHMENT_DATA );
            criteria.addSelectColumn( ATTACHMENT_FILE_PATH );
            criteria.addSelectColumn( ATTACHMENT_MIME_TYPE );
            criteria.addSelectColumn( MODIFIED_BY );
            criteria.addSelectColumn( CREATED_BY );
            criteria.addSelectColumn( MODIFIED_DATE );
            criteria.addSelectColumn( CREATED_DATE );
            criteria.addSelectColumn( DELETED );
        }


    /** Create a new object of type cls from a resultset row starting
      * from a specified offset.  This is done so that you can select
      * other rows than just those needed for this object.  You may
      * for example want to create two objects from the same row.
      */
    public static ScarabAttachment row2Object (Record row, int offset, Class cls ) throws Exception
    {
        ScarabAttachment obj = (ScarabAttachment)cls.newInstance();
                                        obj.setAttachmentId(row.getValue(offset+0).asInt());
                                            obj.setIssueId(row.getValue(offset+1).asInt());
                                            obj.setTypeId(row.getValue(offset+2).asInt());
                                            obj.setName(row.getValue(offset+3).asString());
                                            obj.setData(row.getValue(offset+4).asBytes());
                                            obj.setFilePath(row.getValue(offset+5).asString());
                                            obj.setMimeType(row.getValue(offset+6).asString());
                                            obj.setModifiedBy(row.getValue(offset+7).asInt());
                                            obj.setCreatedBy(row.getValue(offset+8).asInt());
                                            obj.setModifiedDate(row.getValue(offset+9).asDate());
                                            obj.setCreatedDate(row.getValue(offset+10).asDate());
                                            obj.setDeleted
                (1 == row.getValue(offset+11).asInt());
                                        obj.setModified(false);
            obj.setNew(false);
                return obj;
    }

    /** Method to do selects */
    public static Vector doSelect( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabAttachment", null);
    }

    /** 
     * Method to do selects.  This method is to be used during a transaction,
     * otherwise use the doSelect(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Vector doSelect( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabAttachment", dbCon);
    }

    /** Method to do selects. The returned vector will have object
      * of className
      */
    public static Vector doSelect( Criteria criteria, String className, DBConnection dbCon) throws Exception
    {
        addSelectColumns ( criteria );

                                                                                                                                                                                // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
              
        // BasePeer returns a Vector of Value (Village) arrays.  The array
        // order follows the order columns were placed in the Select clause.
        Vector rows = null;
        if (dbCon == null)
        {
            rows = BasePeer.doSelect(criteria);
        }
        else
        {
            rows = BasePeer.doSelect(criteria, dbCon);
        }
        Vector results = new Vector();

        // populate the object(s)
        for ( int i=0; i<rows.size(); i++ )
        {
            Record row = (Record)rows.elementAt(i);
            results.add (row2Object (row,1,Class.forName (className)));
         }
         return results;
    }

    /**
     * Method to do updates. 
     *
     * @param Criteria object containing data that is used to create the UPDATE statement.
     */
    public static void doUpdate(Criteria criteria) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        Criteria selectCriteria = new Criteria(2);
                                selectCriteria.put( ATTACHMENT_ID, criteria.remove(ATTACHMENT_ID) );
                                                                                                                                                                                                                                                                     // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) ) 
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                                BasePeer.doUpdate( selectCriteria, criteria );
    }

    /** 
     * Method to do updates.  This method is to be used during a transaction,
     * otherwise use the doUpdate(Criteria) method.  It will take care of 
     * the connection details internally. 
     *
     * @param Criteria object containing data that is used to create the UPDATE statement.
     */
    public static void doUpdate(Criteria criteria, DBConnection dbCon) throws Exception
    {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
         Criteria selectCriteria = new Criteria(2);
                                selectCriteria.put( ATTACHMENT_ID, criteria.remove(ATTACHMENT_ID) );
                                                                                                                                                                                                                                                                     // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                                BasePeer.doUpdate( selectCriteria, criteria, dbCon );
     }

    /** 
     * Method to do deletes.
     *
     * @param Criteria object containing data that is used DELETE from database.
     */
     public static void doDelete(Criteria criteria) throws Exception
     {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                                                                                // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                       BasePeer.doDelete ( criteria );
     }

    /** 
     * Method to do deletes.  This method is to be used during a transaction,
     * otherwise use the doInsert(Criteria) method.  It will take care of 
     * the connection details internally. 
     *
     * @param Criteria object containing data that is used DELETE from database.
     */
     public static void doDelete(Criteria criteria, DBConnection dbCon) throws Exception
     {
         criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
                                                                                                                                                                                // check for conversion from boolean to int
        if ( criteria.containsKey(DELETED) )
        {
            Object possibleBoolean = criteria.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    criteria.add(DELETED, 1);
                }
                else
                {   
                    criteria.add(DELETED, 0);
                }
            }                     
         }
                       BasePeer.doDelete ( criteria, dbCon );
     }

    /** Method to do inserts */
    public static void doInsert( ScarabAttachment obj ) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj)));
    }

    /**
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabAttachment obj) throws Exception
    {
        doUpdate(buildCriteria(obj));
    }
    /**
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabAttachment obj) throws Exception
    {
        doDelete(buildCriteria(obj));
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(ScarabAttachment) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to insert into the database.
     */
    public static void doInsert( ScarabAttachment obj, DBConnection dbCon) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj), dbCon));
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(ScarabAttachment) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabAttachment obj, DBConnection dbCon) throws Exception
    {
        doUpdate(buildCriteria(obj), dbCon);
    }
    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ScarabAttachment) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabAttachment obj, DBConnection dbCon) throws Exception
    {
        doDelete(buildCriteria(obj), dbCon);
    }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( ScarabAttachment obj )
    {
        Criteria criteria = new Criteria();
                            if ( !obj.isNew() )
            criteria.add( ATTACHMENT_ID, obj.getAttachmentId() );
                                        criteria.add( ISSUE_ID, obj.getIssueId() );
                                        criteria.add( ATTACHMENT_TYPE_ID, obj.getTypeId() );
                                        criteria.add( ATTACHMENT_NAME, obj.getName() );
                                        criteria.add( ATTACHMENT_DATA, obj.getData() );
                                        criteria.add( ATTACHMENT_FILE_PATH, obj.getFilePath() );
                                        criteria.add( ATTACHMENT_MIME_TYPE, obj.getMimeType() );
                                        criteria.add( MODIFIED_BY, obj.getModifiedBy() );
                                        criteria.add( CREATED_BY, obj.getCreatedBy() );
                                        criteria.add( MODIFIED_DATE, obj.getModifiedDate() );
                                        criteria.add( CREATED_DATE, obj.getCreatedDate() );
                                        criteria.add( DELETED, obj.getDeleted() );
                            return criteria;
    }

    /** 
     * Retrieve a single object by pk where multiple PK's are separated
     * by colons
     *
     * @param int attachment_id
     */
    public static ScarabAttachment retrieveById(Object id) 
        throws Exception
    {
        StringTokenizer stok = new StringTokenizer((String)id, ":");
        if ( stok.countTokens() < 1 )
        {   
            throw new TurbineException(
                "id tokens did not match number of primary keys" );
        }
           int attachment_id = Integer.parseInt(stok.nextToken());;

       return retrieveByPK(
             attachment_id
              );
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param int attachment_id
     */
    public static ScarabAttachment retrieveByPK(
                      int attachment_id
                                                                                                                                                                 ) throws Exception
    {
        Criteria criteria = new Criteria();
                       if( attachment_id > 0 )
                  criteria.add( ScarabAttachmentPeer.ATTACHMENT_ID, attachment_id );
                                                                                                                    Vector ScarabAttachmentVector = doSelect(criteria);
        if (ScarabAttachmentVector.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabAttachment) ScarabAttachmentVector.firstElement();
        }
    }


      
        
                       
     
          


   /**
    * selects a collection of ScarabAttachment objects pre-filled with their
    * ScarabIssue objects.
    */
    public static Vector doSelectJoinScarabIssue(Criteria c)
        throws Exception
    {
        addSelectColumns(c);
        int offset = numColumns + 1;
        ScarabIssuePeer.addSelectColumns(c);

                                                                                                                                                                                // check for conversion from boolean to int
        if ( c.containsKey(DELETED) )
        {
            Object possibleBoolean = c.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    c.add(DELETED, 1);
                }
                else
                {   
                    c.add(DELETED, 0);
                }
            }                     
         }
                      
        Vector rows = BasePeer.doSelect(c);
        Vector results = new Vector();

        for (int i=0; i<rows.size(); i++)
        {
            ScarabAttachment obj1 = 
                row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            1, Class.forName("org.tigris.scarab.baseom.ScarabAttachment") );
            ScarabIssue obj2 = ScarabIssuePeer
                .row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            offset, Class.forName("org.tigris.scarab.baseom.ScarabIssue") );
            
            boolean newObject = true;
            for (int j=0; j<results.size(); j++)
            {
                ScarabAttachment temp_obj1 = (ScarabAttachment)results.elementAt(j);
                ScarabIssue temp_obj2 = temp_obj1.getScarabIssue();
                if ( temp_obj2.getId().equals(obj2.getId() ) )
                {
                    newObject = false;
                    temp_obj2.addScarabAttachments(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initScarabAttachments();
                obj2.addScarabAttachments(obj1);
            }
            results.add(obj1);

        }

        return results;
    }
         
                       
     
          


   /**
    * selects a collection of ScarabAttachment objects pre-filled with their
    * ScarabAttachmentType objects.
    */
    public static Vector doSelectJoinScarabAttachmentType(Criteria c)
        throws Exception
    {
        addSelectColumns(c);
        int offset = numColumns + 1;
        ScarabAttachmentTypePeer.addSelectColumns(c);

                                                                                                                                                                                // check for conversion from boolean to int
        if ( c.containsKey(DELETED) )
        {
            Object possibleBoolean = c.get(DELETED);
            if ( possibleBoolean instanceof Boolean )
            {
                if ( ((Boolean)possibleBoolean).booleanValue() )
                {
                    c.add(DELETED, 1);
                }
                else
                {   
                    c.add(DELETED, 0);
                }
            }                     
         }
                      
        Vector rows = BasePeer.doSelect(c);
        Vector results = new Vector();

        for (int i=0; i<rows.size(); i++)
        {
            ScarabAttachment obj1 = 
                row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            1, Class.forName("org.tigris.scarab.baseom.ScarabAttachment") );
            ScarabAttachmentType obj2 = ScarabAttachmentTypePeer
                .row2Object((com.workingdogs.village.Record)rows.elementAt(i),
                            offset, Class.forName("org.tigris.scarab.baseom.ScarabAttachmentType") );
            
            boolean newObject = true;
            for (int j=0; j<results.size(); j++)
            {
                ScarabAttachment temp_obj1 = (ScarabAttachment)results.elementAt(j);
                ScarabAttachmentType temp_obj2 = temp_obj1.getScarabAttachmentType();
                if ( temp_obj2.getId().equals(obj2.getId() ) )
                {
                    newObject = false;
                    temp_obj2.addScarabAttachments(obj1);
                    break;
                }
            }
            if (newObject)
            {
                obj2.initScarabAttachments();
                obj2.addScarabAttachments(obj1);
            }
            results.add(obj1);

        }

        return results;
    }
    

  


    
    /** 
     * Retrieve objects by fk
     *
     * @param int issue_id
     */
//    public static Vector retrieveByIssueId(int issue_id)
//    {
        
    
    /** 
     * Retrieve objects by fk
     *
     * @param int attachment_type_id
     */
//    public static Vector retrieveByTypeId(int attachment_type_id)
//    {
        
                    
}








