package org.tigris.scarab.om;

// Turbine classes
import org.apache.turbine.om.security.TurbineUser;
import org.apache.turbine.services.db.om.NumberKey;

/** 
 * This class serves to provide any expected methods by torque that are
 * not implemented in TurbineUser
 */
public abstract class TurbineTorqueUser 
    extends org.apache.turbine.om.security.TurbineUser
{
}
