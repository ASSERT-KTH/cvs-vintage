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

import org.tigris.scarab.baseom.ScarabModification;

// Local classes
import org.tigris.scarab.baseom.map.*;
import org.tigris.scarab.baseom.*;

/** This class was autogenerated by GenerateMapBuilder on: Tue Jan 02 21:50:40 PST 2001 */
public class ScarabModificationPeer extends BasePeer
{
    /** the mapbuilder for this class */
    private static final ScarabModificationMapBuilder mapBuilder = 
        (ScarabModificationMapBuilder)getMapBuilder(ScarabModificationMapBuilder.CLASS_NAME);

    /** the table name for this class */
    public static final String TABLE_NAME = mapBuilder.getTable();

    /** the column name for the TABLE_ID field */
    public static final String TABLE_ID = mapBuilder.getScarabModification_TableId();
    /** the column name for the COLUMN_ID field */
    public static final String COLUMN_ID = mapBuilder.getScarabModification_ColumnId();
    /** the column name for the MODIFIED_BY field */
    public static final String MODIFIED_BY = mapBuilder.getScarabModification_ModifiedBy();
    /** the column name for the CREATED_BY field */
    public static final String CREATED_BY = mapBuilder.getScarabModification_CreatedBy();
    /** the column name for the MODIFIED_DATE field */
    public static final String MODIFIED_DATE = mapBuilder.getScarabModification_ModifiedDate();
    /** the column name for the CREATED_DATE field */
    public static final String CREATED_DATE = mapBuilder.getScarabModification_CreatedDate();

    /** number of columns for this peer */
    public static final int numColumns =  6;;

    /** Method to do inserts */
    public static Object doInsert( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
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
                                                                                                 return BasePeer.doInsert( criteria, dbCon );
    }

    /** Add all the columns needed to create a new object */
    public static void addSelectColumns (Criteria criteria) throws Exception
    {
            criteria.addSelectColumn( TABLE_ID );
            criteria.addSelectColumn( COLUMN_ID );
            criteria.addSelectColumn( MODIFIED_BY );
            criteria.addSelectColumn( CREATED_BY );
            criteria.addSelectColumn( MODIFIED_DATE );
            criteria.addSelectColumn( CREATED_DATE );
        }


    /** Create a new object of type cls from a resultset row starting
      * from a specified offset.  This is done so that you can select
      * other rows than just those needed for this object.  You may
      * for example want to create two objects from the same row.
      */
    public static ScarabModification row2Object (Record row, int offset, Class cls ) throws Exception
    {
        ScarabModification obj = (ScarabModification)cls.newInstance();
                                        obj.setTableId(row.getValue(offset+0).asInt());
                                            obj.setColumnId(row.getValue(offset+1).asInt());
                                            obj.setModifiedBy(row.getValue(offset+2).asInt());
                                            obj.setCreatedBy(row.getValue(offset+3).asInt());
                                            obj.setModifiedDate(row.getValue(offset+4).asDate());
                                            obj.setCreatedDate(row.getValue(offset+5).asDate());
                                        obj.setModified(false);
            obj.setNew(false);
                return obj;
    }

    /** Method to do selects */
    public static Vector doSelect( Criteria criteria ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabModification", null);
    }

    /** 
     * Method to do selects.  This method is to be used during a transaction,
     * otherwise use the doSelect(Criteria) method.  It will take care of 
     * the connection details internally. 
     */
    public static Vector doSelect( Criteria criteria, DBConnection dbCon ) throws Exception
    {
        criteria.setDbName(getMapBuilder().getDatabaseMap().getName());
        return doSelect (criteria,"org.tigris.scarab.baseom.ScarabModification", dbCon);
    }

    /** Method to do selects. The returned vector will have object
      * of className
      */
    public static Vector doSelect( Criteria criteria, String className, DBConnection dbCon) throws Exception
    {
        addSelectColumns ( criteria );

                                                                                         
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
                                selectCriteria.put( TABLE_ID, criteria.remove(TABLE_ID) );
                                         selectCriteria.put( COLUMN_ID, criteria.remove(COLUMN_ID) );
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
                                selectCriteria.put( TABLE_ID, criteria.remove(TABLE_ID) );
                                         selectCriteria.put( COLUMN_ID, criteria.remove(COLUMN_ID) );
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
                                                                                                  BasePeer.doDelete ( criteria, dbCon );
     }

    /** Method to do inserts */
    public static void doInsert( ScarabModification obj ) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj)));
    }

    /**
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabModification obj) throws Exception
    {
        doUpdate(buildCriteria(obj));
    }
    /**
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabModification obj) throws Exception
    {
        doDelete(buildCriteria(obj));
    }

    /** 
     * Method to do inserts.  This method is to be used during a transaction,
     * otherwise use the doInsert(ScarabModification) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to insert into the database.
     */
    public static void doInsert( ScarabModification obj, DBConnection dbCon) throws Exception
    {
        obj.setId(doInsert(buildCriteria(obj), dbCon));
    }

    /**
     * Method to do update.  This method is to be used during a transaction,
     * otherwise use the doUpdate(ScarabModification) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to update in the database.
     */
    public static void doUpdate(ScarabModification obj, DBConnection dbCon) throws Exception
    {
        doUpdate(buildCriteria(obj), dbCon);
    }
    /**
     * Method to delete.  This method is to be used during a transaction,
     * otherwise use the doDelete(ScarabModification) method.  It will take 
     * care of the connection details internally. 
     *
     * @param obj the data object to delete in the database.
     */
    public static void doDelete(ScarabModification obj, DBConnection dbCon) throws Exception
    {
        doDelete(buildCriteria(obj), dbCon);
    }

    /** Build a Criteria object from the data object for this peer */
    public static Criteria buildCriteria( ScarabModification obj )
    {
        Criteria criteria = new Criteria();
                            if ( !obj.isNew() )
            criteria.add( TABLE_ID, obj.getTableId() );
                                        if ( !obj.isNew() )
            criteria.add( COLUMN_ID, obj.getColumnId() );
                                        criteria.add( MODIFIED_BY, obj.getModifiedBy() );
                                        criteria.add( CREATED_BY, obj.getCreatedBy() );
                                        criteria.add( MODIFIED_DATE, obj.getModifiedDate() );
                                        criteria.add( CREATED_DATE, obj.getCreatedDate() );
                            return criteria;
    }

    /** 
     * Retrieve a single object by pk where multiple PK's are separated
     * by colons
     *
     * @param int table_id
     * @param int column_id
     */
    public static ScarabModification retrieveById(Object id) 
        throws Exception
    {
        StringTokenizer stok = new StringTokenizer((String)id, ":");
        if ( stok.countTokens() < 2 )
        {   
            throw new TurbineException(
                "id tokens did not match number of primary keys" );
        }
           int table_id = Integer.parseInt(stok.nextToken());;
           int column_id = Integer.parseInt(stok.nextToken());;

       return retrieveByPK(
             table_id
              , column_id
              );
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param int table_id
     * @param int column_id
     */
    public static ScarabModification retrieveByPK(
                      int table_id
                                      , int column_id
                                                                             ) throws Exception
    {
        Criteria criteria = new Criteria();
                       if( table_id > 0 )
                  criteria.add( ScarabModificationPeer.TABLE_ID, table_id );
                            if( column_id > 0 )
                  criteria.add( ScarabModificationPeer.COLUMN_ID, column_id );
                                                     Vector ScarabModificationVector = doSelect(criteria);
        if (ScarabModificationVector.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabModification) ScarabModificationVector.firstElement();
        }
    }


    
 

  


            
}








