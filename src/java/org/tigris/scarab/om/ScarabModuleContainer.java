package org.tigris.scarab.om;

import java.util.*;
import org.apache.torque.om.BaseObject;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;


/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @author <a href="mailto:jmcnally@collab.new">John McNally</a>
 * @version $Id: ScarabModuleContainer.java,v 1.2 2002/04/12 22:59:42 jmcnally Exp $
 */
public class ScarabModuleContainer extends org.tigris.scarab.om.ScarabModule
{
    public ScarabModuleContainer()
    {
        setClassKey(ScarabModulePeer.CLASSKEY_1);
    }
        
    /**
     * Determines whether this module is accepting new issues.
     * Containers never accept new issues, so this will return false;
     */
    public boolean allowsNewIssues() 
    {
        return false;
    }
    
    /**
     * Determines whether this module accepts issues.  This default
     * implementation does NOT allow issues.
     */
    public boolean allowsIssues()
    {
        return false;
    }
}
