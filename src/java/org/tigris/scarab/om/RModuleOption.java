package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.*;
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Fri Apr 13 13:24:10 PDT 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class RModuleOption 
    extends org.tigris.scarab.om.BaseRModuleOption
    implements Persistent
{

    private static final Comparator comparator = new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                int result = 1;
                RModuleOption opt1 = (RModuleOption)obj1; 
                RModuleOption opt2 = (RModuleOption)obj2;
                if (opt1.getOrder() < opt2.getOrder()) 
                {
                    result = -1;
                }
                else if (opt1.getOrder() == opt2.getOrder()) 
                {
                    result = opt1.getDisplayValue()
                        .compareTo(opt2.getDisplayValue()); 
                }
                return result;
            }            
        };

    private int level;


    /**
     * Compares numeric value and in cases where the numeric value
     * is the same it compares the display values.
     */
    public static Comparator getComparator()
    {
        return comparator;
    }


    /**
     * A convenience method for getting the option name.  It is 
     * preferred over using getAttributeOption().getName() as it
     * leaves open the possibility of per module display values.
     */
    public String getDisplayValue()
    {
        String dispVal = super.getDisplayValue();
        if ( dispVal == null ) 
        {
            try
            {
                dispVal = getAttributeOption().getName();
            }
            catch (Exception e)
            {
                Log.error(e);
                dispVal = "!Error-Check Logs!";
            }
        }
        return dispVal;
        
        // return getAttributeOption().getName();
    }
    
    /**
     * Get the level in the option parent-child tree.
     * @return value of level.
     */
    public int getLevel() 
    {
        return level;
    }
    
    /**
     * Get the level in the option parent-child tree.
     * @param v  Value to assign to level.
     */
    public void setLevel(int  v) 
    {
        this.level = v;
    }
    

    /* *
     * The AttributeOption that is the parent of this moduleOption.
     * Is different that for a moduleOption to have a parent, it
     * must have siblings or nephews/nieces that fall in the same module.
     * /
    public AttributeOption getParent()
    {
    }
    */
}



