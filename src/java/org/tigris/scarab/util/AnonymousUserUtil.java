/*
 * Created on 03.01.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.scarab.util;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.RunData;
import org.apache.turbine.Turbine;
import org.tigris.scarab.om.ScarabUser;

/**
 * @author hdab
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnonymousUserUtil
{

    /**
     * Returns true if the user is the one set in scarab.anonymous.username, and
     * false otherwise.
     * Note: If anonymous access is denied per configuration, this method
     * always returns false!
     * @return
     */
    public static boolean isUserAnonymous(ScarabUser user)
    {
        boolean brdo = false;
        if(anonymousAccessAllowed())
        {
            String anonymous = getAnonymousUserId();
            if (anonymous != null && user.getUserName().equals(anonymous))
            {
                brdo = true;
            }
        }
        return brdo;
    }

    /**
     * Returns true, when anonymous user access is explicitly allowed,.
     * Otherwise returns false.
     * @return
     */
    public static boolean anonymousAccessAllowed()
    {
        boolean allowed = Turbine.getConfiguration().getBoolean("scarab.anonymous.enable", false);
        return allowed;
    }
    
    /**
     * Returns the userid of the anonymous user
     * Note: This method returns the anonymous userid 
     * independent frm wether anonymous access is allowed or not.
     * @return
     */
    public static String getAnonymousUserId()
    {
        String anonymous = Turbine.getConfiguration().getString("scarab.anonymous.username", null);
        return anonymous;
    }
    
    /**
     * Return an instanceof the Anonymous User.
     * If Anonymous user has been switched off, this method
     * returns a Turbine-anonymous user.
     * @return
     * @throws DataBackendException
     * @throws UnknownEntityException
     */
    public static User getAnonymousUser() throws DataBackendException, UnknownEntityException
    {
        User user;
        if(anonymousAccessAllowed())
        {
            String userid = getAnonymousUserId();
            user = TurbineSecurity.getUser(userid);
        }
        else
        {
            user = TurbineSecurity.getAnonymousUser();
        }
        return user;
    }
    
    /**
     * Login the Anonymous user and prepare the run data
     * @param data
     */
    public static void anonymousLogin(RunData data)
    {
        try
        {
            User user = AnonymousUserUtil.getAnonymousUser();
            data.setUser(user);
            user.setHasLoggedIn(Boolean.TRUE);
            user.updateLastLogin();
            data.save();            
        }
        catch (Exception e)
        {
            Log.get().error("anonymousLogin failed to login anonymously: " + e.getMessage());
        }
        
    }

}
