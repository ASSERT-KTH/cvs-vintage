package org.tigris.scarab.baseom.map;

// JDK classes
import java.util.*;
import java.math.*;

// Turbine classes
import org.apache.turbine.services.db.PoolBrokerService;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.util.db.map.MapBuilder;
import org.apache.turbine.util.db.map.DatabaseMap;
import org.apache.turbine.util.db.map.TableMap;

/** This class was autogenerated by GenerateMapBuilder on: Mon Jan 08 11:17:17 PST 2001 */
public class ScarabTransactionMapBuilder implements MapBuilder
{
    /** the name of this class */
    public static final String CLASS_NAME = "org.tigris.scarab.baseom.map.ScarabTransactionMapBuilder";

    /** item */
    public static String getTable( )
    {
        return "SCARAB_TRANSACTION";
    }


    /** SCARAB_TRANSACTION.TRANSACTION_ID */
    public static String getScarabTransaction_TransactionId()
    {
        return getTable() + ".TRANSACTION_ID";
    }

    /** SCARAB_TRANSACTION.CREATED_BY */
    public static String getScarabTransaction_CreatedBy()
    {
        return getTable() + ".CREATED_BY";
    }

    /** SCARAB_TRANSACTION.CREATED_DATE */
    public static String getScarabTransaction_CreatedDate()
    {
        return getTable() + ".CREATED_DATE";
    }


    /**  the database map  */
    private DatabaseMap dbMap = null;

    /**
        tells us if this DatabaseMapBuilder is built so that we don't have
        to re-build it every time
    */
    public boolean isBuilt()
    {
        if ( dbMap != null )
            return true;
        return false;
    }

    /**  gets the databasemap this map builder built.  */
    public DatabaseMap getDatabaseMap()
    {
        return this.dbMap;
    }
    /** the doBuild() method builds the DatabaseMap */
    public void doBuild ( ) throws Exception
    {
        dbMap = TurbineDB.getDatabaseMap("default");

        dbMap.addTable(getTable());
        TableMap tMap = dbMap.getTable(getTable());

        tMap.setPrimaryKeyMethod(tMap.IDBROKERTABLE);



                  tMap.addPrimaryKey ( getScarabTransaction_TransactionId(), new Integer(0) );
          
                  tMap.addColumn ( getScarabTransaction_CreatedBy(), new Integer(0) );
          
                  tMap.addColumn ( getScarabTransaction_CreatedDate(), new Date() );
          
    }

}
