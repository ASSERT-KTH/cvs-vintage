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
import org.apache.turbine.services.uniqueid.*;
// Scarab
import org.tigris.scarab.om.peer.ScarabUserPeer;

/**
    This class is an abstraction that is currently based around
    Turbine's code. We can change this later. It is here so
    that it is easier to change later to work under different
    implementation needs.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabUser.java,v 1.1 2000/12/18 05:03:33 jon Exp $
*/
public class ScarabUser extends TurbineUser
{    
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
        email
        password
        password_confirm
        firstname
        lastname
        companyname
        address1
        address2
        city
        state
        country
        postalcode
        phone
        altphone
        fax
        cell
        pager
        </pre>
    */
    public void doPopulate(RunData data)
        throws Exception
    {
        // form validation routines
        String LOGINID = data.getParameters().getString("email", null);
        String FIRST_NAME = data.getParameters().getString("firstname", null);
        String LAST_NAME = data.getParameters().getString("lastname", null);
        String PASSWORD = data.getParameters().getString("password", null);
        String PASSWORD_CONFIRM = data.getParameters().getString("password_confirm", null);
        
        // FIXME: add better email address checking to catch stupid mistakes up front
        // FIXME: add better form validation all around, make sure we don't have
        //        bad data as well as the right length.
        if (LOGINID == null)
            throw new Exception ("The email address you entered is empty!");
        if (FIRST_NAME == null)
            throw new Exception ("The first name you entered is empty!");
        if (LAST_NAME == null)
            throw new Exception ("The last name you entered is empty!");
        if (PASSWORD == null)
            throw new Exception ("The password you entered is empty!");
        if (PASSWORD_CONFIRM == null)
            throw new Exception ("The password confirm you entered is empty!");
    
        if (!PASSWORD.equals(PASSWORD_CONFIRM))
            throw new Exception ("The password's you entered do not match!");
        
        // in TurbineUser, data for the user is stored within a hashtable
        // this hashtable can be easily serialized to disk by simply providing
        // a visitor_id and calling the TurbineUser.saveToStorage() method.
        // for now, we are just interested in populating ourself.
        
        // these are defined by an Interface so that they can be easily re-used
        setPerm(User.USERNAME, LOGINID);
        setPerm(User.PASSWORD, PASSWORD);
        setPerm(User.FIRST_NAME, FIRST_NAME);
        setPerm(User.LAST_NAME, LAST_NAME);
        
        // these don't have accessor methods in the User interface, so it is ok
        // to define them myself
        setPerm(ScarabUserPeer.getColumnName("ADDRESS1"), data.getParameters().getString("ADDRESS1",""));
        setPerm(ScarabUserPeer.getColumnName("ADDRESS2"), data.getParameters().getString("ADDRESS2",""));
        setPerm(ScarabUserPeer.getColumnName("CITY"), data.getParameters().getString("CITY",""));
        setPerm(ScarabUserPeer.getColumnName("STATE"), data.getParameters().getString("STATE",""));
        setPerm(ScarabUserPeer.getColumnName("POSTALCODE"), data.getParameters().getString("POSTALCODE",""));
        setPerm(ScarabUserPeer.getColumnName("COUNTRY"), data.getParameters().getString("COUNTRY",""));
        setPerm(ScarabUserPeer.getColumnName("CITIZENSHIP"), ""); // empty for now.
        setPerm(ScarabUserPeer.getColumnName("PHONE"), data.getParameters().getString("PHONE",""));
        setPerm(ScarabUserPeer.getColumnName("ALTPHONE"), data.getParameters().getString("ALTPHONE",""));
        setPerm(ScarabUserPeer.getColumnName("FAX"), data.getParameters().getString("FAX",""));
        setPerm(ScarabUserPeer.getColumnName("CELL"), data.getParameters().getString("CELL",""));
        setPerm(ScarabUserPeer.getColumnName("PAGER"), data.getParameters().getString("PAGER",""));
        setPerm(ScarabUserPeer.getColumnName("EMAIL"), LOGINID); // in Scarab, LOGINID == Email
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
        if(ScarabUserPeer.checkExists(this))
            throw new Exception ( "Sorry, a user with that loginid already exists!" );
        
        // get a unique id for validating the user
        String uniqueId = TurbineUniqueId.getPseudorandomId().substring(0,10);
        // add it to the perm table
        setPerm(ScarabUserPeer.getColumnName("CONFIRM_VALUE"), uniqueId);
        // add it to the criteria for insert into the database
        Criteria crit = getCriteria();        
        crit.add (ScarabUserPeer.getColumnName("CONFIRM_VALUE"), uniqueId);

        // insert the user into the database
        ScarabUserPeer.doInsert(crit);
        
        // FIXME: need to define here what roles the new user starts out having.
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
            criteria.add (ScarabUserPeer.getColumnName(ScarabUserPeer.USER_ID), user.getUserId() );
            criteria.add (ScarabUserPeer.getColumnName(User.CONFIRM_VALUE), ScarabUserPeer.CONFIRM_DATA);
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
        criteria.add (ScarabUserPeer.getColumnName("ADDRESS1"), getPerm(ScarabUserPeer.getColumnName("ADDRESS1")));
        criteria.add (ScarabUserPeer.getColumnName("ADDRESS2"), getPerm(ScarabUserPeer.getColumnName("ADDRESS2")));
        criteria.add (ScarabUserPeer.getColumnName("CITY"), getPerm(ScarabUserPeer.getColumnName("CITY")));
        criteria.add (ScarabUserPeer.getColumnName("STATE"), getPerm(ScarabUserPeer.getColumnName("STATE")));
        criteria.add (ScarabUserPeer.getColumnName("POSTALCODE"), getPerm(ScarabUserPeer.getColumnName("POSTALCODE")));
        criteria.add (ScarabUserPeer.getColumnName("COUNTRY"), getPerm(ScarabUserPeer.getColumnName("COUNTRY")));
        criteria.add (ScarabUserPeer.getColumnName("CITIZENSHIP"), getPerm(ScarabUserPeer.getColumnName("CITIZENSHIP")));
        criteria.add (ScarabUserPeer.getColumnName("PHONE"), getPerm(ScarabUserPeer.getColumnName("PHONE")));
        criteria.add (ScarabUserPeer.getColumnName("ALTPHONE"), getPerm(ScarabUserPeer.getColumnName("ALTPHONE")));
        criteria.add (ScarabUserPeer.getColumnName("FAX"), getPerm(ScarabUserPeer.getColumnName("FAX")));
        criteria.add (ScarabUserPeer.getColumnName("CELL"), getPerm(ScarabUserPeer.getColumnName("CELL")));
        criteria.add (ScarabUserPeer.getColumnName("PAGER"), getPerm(ScarabUserPeer.getColumnName("PAGER")));
        criteria.add (ScarabUserPeer.getColumnName("EMAIL"), this.getUserName());
        return criteria;
    }
}    