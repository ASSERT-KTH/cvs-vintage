package org.apache.fulcrum.security.impl.db.entity;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.session.SessionBindingEvent;

/**
 * A generic implementation of User interface.
 *
 * This basic implementation contains the functionality that is
 * expected to be common among all User implementations.
 *
 * @author <a href="mailto:josh@stonecottage.com">Josh Lucas</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:cberry@gluecode.com">Craig D. Berry</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: TurbineUser.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class TurbineUser
    extends org.apache.fulcrum.security.impl.db.entity.BaseTurbineUser
    implements User
{
    /** The date on which the user last accessed the application. */
    private Date lastAccessDate = null;

    /** This is data that will survive a servlet engine restart. */
    private Hashtable permStorage = null;

    /** This is data that will not survive a servlet engine restart. */
    private Hashtable tempStorage = null;

    /**
     * Constructor.
     * Create a new User and set the createDate.
     */
    public TurbineUser()
    {
        setCreateDate(new Date());
        tempStorage = new Hashtable(10);
        setHasLoggedIn(Boolean.FALSE);
    }

    /**
     * Gets the access counter for a user from perm storage.
     *
     * @return The access counter for the user.
     */
    public int getAccessCounter()
    {
        try
        {
            return ((Integer) getPerm(User.ACCESS_COUNTER)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * Gets the access counter for a user during a session.
     *
     * @return The access counter for the user for the session.
     */
    public int getAccessCounterForSession()
    {
        try
        {
            return ((Integer) getTemp(User.SESSION_ACCESS_COUNTER)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * Increments the permanent hit counter for the user.
     */
    public void incrementAccessCounter()
    {
        setAccessCounter(getAccessCounter() + 1);
    }

    /**
     * Increments the session hit counter for the user.
     */
    public void incrementAccessCounterForSession()
    {
        setAccessCounterForSession(getAccessCounterForSession() + 1);
    }

    /**
     * Sets the access counter for a user, saved in perm storage.
     *
     * @param cnt The new count.
     */
    public void setAccessCounter(int cnt)
    {
        setPerm(User.ACCESS_COUNTER, new Integer(cnt));
    }

    /**
     * Sets the session access counter for a user, saved in temp
     * storage.
     *
     * @param cnt The new count.
     */
    public void setAccessCounterForSession(int cnt)
    {
        setTemp(User.SESSION_ACCESS_COUNTER, new Integer(cnt));
    }

    /**
     * This method reports whether or not the user has been confirmed
     * in the system by checking the User.CONFIRM_VALUE
     * column in the users record to see if it is equal to
     * User.CONFIRM_DATA.
     *
     * @return True if the user has been confirmed.
     */
    public boolean isConfirmed()
    {
        String value = getConfirmed();
        return (value != null && value.equals(User.CONFIRM_DATA));
    }

    /**
     * The user is considered logged in if they have not timed out.
     *
     * @return Whether the user has logged in.
     */
    public boolean hasLoggedIn()
    {
        Boolean loggedIn = getHasLoggedIn();
        return (loggedIn != null && loggedIn.booleanValue());
    }

    /**
     * This sets whether or not someone has logged in.  hasLoggedIn()
     * returns this value.
     *
     * @param value Whether someone has logged in or not.
     */
    public void setHasLoggedIn(Boolean value)
    {
        setTemp(User.HAS_LOGGED_IN, value);
    }

    /**
     * This gets whether or not someone has logged in.  hasLoggedIn()
     * returns this value as a boolean.  This is private because you
     * should use hasLoggedIn() instead.
     *
     * @return True if someone has logged in.
     */
    private Boolean getHasLoggedIn()
    {
        return (Boolean) getTemp(User.HAS_LOGGED_IN);
    }

    /**
     * Gets the last access date for this User.  This is the last time
     * that the user object was referenced.
     *
     * @return A Java Date with the last access date for the user.
     */
    public java.util.Date getLastAccessDate()
    {
        if (lastAccessDate == null)
        {
            setLastAccessDate();
        }
        return lastAccessDate;
    }

    /**
     * Sets the last access date for this User. This is the last time
     * that the user object was referenced.
     */
    public void setLastAccessDate()
    {
        lastAccessDate = new java.util.Date();
    }

    /**
     * Get an object from permanent storage.
     *
     * @param name The object's name.
     * @return An Object with the given name.
     */
    public Object getPerm(String name)
    {
        return permStorage.get(name);
    }

    /**
     * Get an object from permanent storage; return default if value
     * is null.
     *
     * @param name The object's name.
     * @param def A default value to return.
     * @return An Object with the given name.
     */
    public Object getPerm(String name, Object def)
    {
        try
        {
            Object val = permStorage.get(name);
            return (val == null ? def : val);
        }
        catch (Exception e)
        {
            return def;
        }
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @return A Hashtable.
     */
    public Hashtable getPermStorage()
    {
        if (this.permStorage == null)
        {
            this.permStorage = new Hashtable();
        }

        return this.permStorage;
    }

    /**
     * Put an object into permanent storage. If the value is null,
     * it will convert that to a "" because the underlying storage
     * mechanism within TurbineUser is currently a Hashtable and
     * null is not a valid value.
     *
     * @param name The object's name.
     * @param value The object.
     */
    public void setPerm(String name, Object value)
    {
        safeAddToHashtable(getPermStorage(), name, value);
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @param stuff A Hashtable.
     */
    public void setPermStorage(Hashtable stuff)
    {
        this.permStorage = stuff;
    }

    /**
     * Get an object from temporary storage.
     *
     * @param name The object's name.
     * @return An Object with the given name.
     */
    public Object getTemp(String name)
    {
        return tempStorage.get(name);
    }

    /**
     * Get an object from temporary storage; return default if value
     * is null.
     *
     * @param name The object's name.
     * @param def A default value to return.
     * @return An Object with the given name.
     */
    public Object getTemp(String name, Object def)
    {
        Object val;
        try
        {
            val = tempStorage.get(name);
            if (val == null)
            {
                val = def;
            }
        }
        catch (Exception e)
        {
            val = def;
        }
        return val;
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @return A Hashtable.
     */
    public Hashtable getTempStorage()
    {
        if (this.tempStorage == null)
        {
            this.tempStorage = new Hashtable();
        }
        return this.tempStorage;
    }

    /**
     * Remove an object from temporary storage and return the object.
     *
     * @param name The name of the object to remove.
     * @return An Object.
     */
    public Object removeTemp(String name)
    {
        return tempStorage.remove(name);
    }

    /**
     * Put an object into temporary storage. If the value is null,
     * it will convert that to a "" because the underlying storage
     * mechanism within TurbineUser is currently a Hashtable and
     * null is not a valid value.
     *
     * @param name The object's name.
     * @param value The object.
     */
    public void setTemp(String name, Object value)
    {
        safeAddToHashtable(tempStorage, name, value);
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @param storage A Hashtable.
     */
    public void setTempStorage(Hashtable storage)
    {
        this.tempStorage = storage;
    }

    /**
     * Updates the last login date in the database.
     *
     * @exception Exception, a generic exception.
     */
    public void updateLastLogin()
        throws Exception
    {
        setPerm( User.LAST_LOGIN, new java.util.Date() );
    }

    /**
     * Implement this method if you wish to be notified when the User
     * has been Bound to the session.
     *
     * @param event Inidication of value/session binding.
     */
    public void valueBound(SessionBindingEvent event)
    {
        // Currently we have no need for this method.
    }

    /**
     * Implement this method if you wish to be notified when the User
     * has been Unbound from the session.
     *
     * @param event Inidication of value/session unbinding.
     */
    public void valueUnbound(SessionBindingEvent event)
    {
        try
        {
            if (hasLoggedIn())
            {
                TurbineSecurity.saveUser(this);
            }
        }
        catch (Exception e)
        {
            //Log.error("TurbineUser.valueUnbobund(): " + e.getMessage(), e);

            // To prevent messages being lost in case the logging system
            // goes away before sessions get unbound on servlet container
            // shutdown, print the stcktrace to the container's console.
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(ostr,true));
            String stackTrace = ostr.toString();
            System.out.println(stackTrace);
        }
    }

    /**
     * Set the value of GroupName
     */
    public void setName(String v)
    {
        setUserName(v);
    }

    /**
     * Set the value of GroupName
     */
    public String getName()
    {
        return getUserName();
    }

    /**
     * Nice method for adding data to a Hashtable in such a way
     * as to not get NPE's. The point being that if the
     * value is null, Hashtable.put() will throw an exception.
     * That blows in the case of this class cause you may want to
     * essentially treat put("Not Null", null ) == put("Not Null", "")
     * We will still throw a NPE if the key is null cause that should
     * never happen.
     *
     * !! Maybe a hashtable isn't the best option here and we
     * should use a Map. This was taken from ObjectUtils.
     */
    public static final void safeAddToHashtable(Hashtable hash, Object key, Object value)
        throws NullPointerException
    {
        if (value == null)
        {
            hash.put ( key, "" );
        }
        else
        {
           hash.put ( key, value );
        }
    }
}
