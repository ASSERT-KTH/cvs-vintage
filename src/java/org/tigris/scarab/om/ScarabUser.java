package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

//JDK
import java.util.*;

// Turbine
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.om.security.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.services.security.*;
import org.apache.turbine.services.uniqueid.*;
// Scarab
import org.tigris.scarab.om.peer.ScarabUserPeer;
import org.tigris.scarab.baseom.*;
import org.tigris.scarab.baseom.peer.*;

/**
    This class is an abstraction that is currently based around
    Turbine's code. We can change this later. It is here so
    that it is easier to change later to work under different
    implementation needs.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabUser.java,v 1.7 2001/01/16 08:31:39 jon Exp $
*/
public class ScarabUser extends org.apache.turbine.om.security.TurbineUser
{    
    private static final String CURRENT_MODULE = "CURRENT_MODULE";
    /**
        Call the superclass constructor to initialize this object.
    */
    public ScarabUser()
    {
        super();
    }
    
    /**
        Attempt to populate the following form fields into the
        superclass. If there is an error with any of the 
        data that needs to be checked/present, throw an exception.

        <pre>
        firstname
        lastname
        email
        password
        password_confirm
        </pre>
    */
    public void doPopulate(RunData data)
        throws Exception
    {
        // form validation routines
        data.getParameters().setProperties(this);

        String password_confirm = data.getParameters().getString("password_confirm", null);
        setUserName(data.getParameters().getString("Email"));

        // FIXME: add better email address checking to catch stupid mistakes up front
        // FIXME: add better form validation all around, make sure we don't have
        //        bad data as well as the right length.
        if (getFirstName() == null || getFirstName().length() == 0)
            throw new Exception ("The first name you entered is empty!");
        if (getLastName() == null || getLastName().length() == 0)
            throw new Exception ("The last name you entered is empty!");
        if (getUserName() == null || getUserName().length() == 0)
            throw new Exception ("The email address you entered is empty!");
        if (getPassword() == null || getPassword().length() == 0)
            throw new Exception ("The password you entered is empty!");
        if (password_confirm == null)
            throw new Exception ("The password confirm you entered is empty!");
        if (!getPassword().equals(password_confirm))
            throw new Exception ("The password's you entered do not match!");
    }
    
    /**
        This method is responsible for creating a new user. It will throw an 
        exception if there is any sort of error (such as a duplicate login id) 
        and place the error message into e.getMessage(). This also creates a 
        uniqueid and places it into this object in the perm table under the
        Visitor.CONFIRM_VALUE key.
    */
    public void createNewUser()
        throws Exception
    {
        // get a unique id for validating the user
        String uniqueId = TurbineUniqueId.getPseudorandomId().substring(0,10);
        // add it to the perm table
        setConfirmed(false, uniqueId);
        TurbineSecurity.addUser (this, getPassword());
    }
    
    /**
        Utility method that takes a username and a confirmation code
        and will return true if there is a match and false if no match.
        <p>
        If there is an Exception, it will also return false.
    */
    public static boolean checkConfirmationCode (String username, String confirm)
    {
        // security check. :-)
        if (confirm.equalsIgnoreCase(ScarabUserPeer.CONFIRM_DATA))
        {
            return false;
        }
    
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserPeer.getColumnName(User.USERNAME), username);
            criteria.add (ScarabUserPeer.getColumnName(User.CONFIRM_VALUE), confirm);
            criteria.setSingleRecord(true);
            Vector result = ScarabUserPeer.doSelect(criteria);
            if (result.size() > 0)
                return true;

            // FIXME: once i figure out how to build an OR in a Criteria i won't need this.
            criteria = new Criteria();
            criteria.add (ScarabUserPeer.getColumnName(User.USERNAME), username);
            criteria.add (ScarabUserPeer.getColumnName(User.CONFIRM_VALUE), ScarabUserPeer.CONFIRM_DATA);
            criteria.setSingleRecord(true);
            result = ScarabUserPeer.doSelect(criteria);
            if (result.size() > 0)
                return true;

            return false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    /**
        This method will mark username as confirmed.
        returns true on success and false on any error
    */
    public static boolean confirmUser (String username)
    {
        try
        {
            User user = getOneUser(username);
            if (user == null)
                throw new Exception ("Username does not exist!");
                
            Criteria criteria = new Criteria();            
            criteria.add (ScarabUserPeer.getColumnName(ScarabUserPeer.USER_ID),
                          ((org.apache.turbine.om.security.TurbineUser)user)
                          .getPrimaryKeyAsLong() );
            criteria.add (ScarabUserPeer.getColumnName(User.CONFIRM_VALUE), 
                          ScarabUserPeer.CONFIRM_DATA);
            ScarabUserPeer.doUpdate(criteria);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
        get a user based on username
        returns null if there is an error or if the user doesn't exist
    */
    public static User getOneUser(String username)
    {
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserPeer.getColumnName(User.USERNAME), username);
            return (User)(ScarabUserPeer.doSelect(criteria).elementAt(0));
        }
        catch (Exception e)
        {
            return null;
        }
    }
    /**
        This will return the username for a given userid.
        return null if there is an error or if the user doesn't exist
    */
    public static String getUserName(int userid)
    {
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserPeer.USER_ID, userid);
            User user = (User)ScarabUserPeer.doSelect(criteria).elementAt(0);
            return user.getUserName();
        }
        catch (Exception e)
        {
            return null;
        }        
    }

    /**
        This method will build up a criteria object out of the information 
        currently stored in this object and then return it.
    */
    private Criteria getCriteria()
    {
        // FIXME: clean up ugly code duplication below. this will be done
        //        by taking advantage of an autogenerated UserPeer instead
        //        of this ugly hack!!
        Criteria criteria = new Criteria();
        criteria.add (ScarabUserPeer.getColumnName(User.USERNAME), this.getUserName());
        criteria.add (ScarabUserPeer.getColumnName(User.PASSWORD), getPerm(User.PASSWORD));
        criteria.add (ScarabUserPeer.getColumnName(User.FIRST_NAME), getPerm(User.FIRST_NAME));
        criteria.add (ScarabUserPeer.getColumnName(User.LAST_NAME), getPerm(User.LAST_NAME));
        criteria.add (ScarabUserPeer.getColumnName(User.EMAIL), this.getUserName());
        return criteria;
    }


    /**
     * 
     */
    public Vector getModules() throws Exception
    {
        Criteria crit = new Criteria(3)
            .add(ScarabRModuleUserPeer.USER_ID, getPrimaryKeyAsLong())
            .add(ScarabRModuleUserPeer.DELETED, false);
        Vector srmvs = ScarabRModuleUserPeer.doSelectJoinScarabModule(crit);
        // each srmvs represents a unique ScarabModule, so we do not 
        // need to check for duplicates.  Just stuff into the new Vector.
        Vector modules = new Vector(srmvs.size());
        for ( int i=0; i<srmvs.size(); i++ ) 
        {
            ScarabRModuleUser srmv = (ScarabRModuleUser)srmvs.get(i);
            modules.add( new Module(srmv.getScarabModule()) );
        }
        
        return modules;
    }

    public Module getCurrentModule()
    {
        return (Module) getTemp(CURRENT_MODULE);
    }

    public void setCurrentModule(Module m)
    {
        setTemp(CURRENT_MODULE, m);
    }

}    
