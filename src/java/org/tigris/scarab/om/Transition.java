package org.tigris.scarab.om;

import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.impl.db.entity.TurbineRolePeer;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

/**
 * Every transition declare the ability of a role to change the value of an
 * attribute from one of its options to another.
 * 
 * @see org.tigris.scarab.workflow.CheapWorkflow
 *
 * @author Diego Martinez Velasco
 * @author Jorge Uriarte Aretxaga  
 */
public class Transition extends org.tigris.scarab.om.BaseTransition
        implements
            Persistent
{
    public Role getRole()
    {
        Role role = null;
        try
        {
            role = TurbineRolePeer.retrieveByPK(this.getRoleId());
        }
        catch (NoRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TooManyRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TorqueException e)
        {
            e.printStackTrace();
        }
        return role;
    }

    public AttributeOption getFrom()
    {
        AttributeOption from = null;
        if (null != this.getFromOptionId())
        {
            try
            {
                from = AttributeOptionPeer.retrieveByPK(this.getFromOptionId());
            }
            catch (NoRowsException e)
            {
                //Nothing to do, just ignore it
            }
            catch (TooManyRowsException e)
            {
                //Nothing to do, just ignore it
            }
            catch (TorqueException e)
            {
                e.printStackTrace();
            }
        }
        return from;
    }

    public AttributeOption getTo()
    {
        AttributeOption to = null;
        try
        {
            to = AttributeOptionPeer.retrieveByPK(this.getToOptionId());
        }
        catch (NoRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TooManyRowsException e)
        {
            //Nothing to do, just ignore it
        }
        catch (TorqueException e)
        {
            e.printStackTrace();
        }
        return to;
    }

    public String toString()
    {
        return this.getFromOptionId() + " -> " + this.getToOptionId()
                + " (role: " + this.getRoleId() + ")";
    }

}

