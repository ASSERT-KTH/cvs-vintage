package org.tigris.scarab;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import java.util.List;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.test.BaseTestCase;

/**
 * Test to understand better how security works..
 *
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh</a>
 * @version $Id: SecurityTest.java,v 1.3 2004/02/09 08:55:28 dep4b Exp $
 */
public class SecurityTest extends BaseTestCase
{
    public void OFFtestCreateAssignDeleteUser() throws Exception
    {
        ScarabModule sm = (ScarabModule)getModule();
        User user = ScarabUserManager.getInstance();//ScarabUserManager.getInstance(sm.getOwnerId());
        user.setEmail("test@test.com");
        user.setConfirmed("CONFIRMED");
        user.setFirstName("test");
        user.setLastName("test");
        user.setUserName("test");
        TurbineSecurity.addUser(user,"test");
        user = TurbineSecurity.getAuthenticatedUser("test","test");
        assertNotNull(user);
        Role role = TurbineSecurity.getRole("Project Owner");
        
        
        TurbineSecurity.grant(user,sm,role);
        List users =sm.getUsers("test","","","",this.getDefaultIssueType());
        assertEquals(1,users.size());
        assertTrue(users.contains(user));
        TurbineSecurity.revoke(user,sm,role);
        TurbineSecurity.removeUser(user);
        try{
            TurbineSecurity.getAuthenticatedUser("test","test");
            fail("shouldn't find user!");
        }
        catch(UnknownEntityException uee){
            
        }
        
       
        
    }
   
    
}
