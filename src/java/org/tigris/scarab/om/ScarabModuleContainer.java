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
 * @version $Id: ScarabModuleContainer.java,v 1.1 2001/12/27 22:36:44 dr Exp $
 */
public class ScarabModuleContainer extends org.tigris.scarab.om.ScarabModule
{
    public ScarabModuleContainer()
    {
        setClassKey(ScarabModulePeer.CLASSKEY_1);
    }
    
    
    public boolean allowsIssues() {
        return (false);
    }
    
}
