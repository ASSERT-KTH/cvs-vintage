package org.tigris.scarab.test.mocks;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */ 

import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.ServiceBroker;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.security.SecurityService;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.log4j.Category;
import org.apache.torque.util.Criteria;

/**
 * @author Eric Pugh
 *
 */
public class MockSecurityService implements SecurityService {

    private Configuration configuration;
    public MockSecurityService() {
        configuration = new BaseConfiguration();
        configuration.addProperty("user.class","org.tigris.scarab.om.ScarabUserImpl");
    }

    public Class getUserClass() throws UnknownEntityException {
        
        return null;
    }

    public User getUserInstance() throws UnknownEntityException {
        
        return null;
    }

    public User getUserInstance(String userName) throws UnknownEntityException {
        
        return null;
    }

    public Class getGroupClass() throws UnknownEntityException {
        
        return null;
    }

    public Group getGroupInstance() throws UnknownEntityException {
        
        return null;
    }

    public Group getGroupInstance(String groupName) throws UnknownEntityException {
        
        return null;
    }

    public Class getPermissionClass() throws UnknownEntityException {
        
        return null;
    }

    public Permission getPermissionInstance() throws UnknownEntityException {
        
        return null;
    }

    public Permission getPermissionInstance(String permName) throws UnknownEntityException {
        
        return null;
    }

    public Class getRoleClass() throws UnknownEntityException {
        
        return null;
    }

    public Role getRoleInstance() throws UnknownEntityException {
        
        return null;
    }

    public Role getRoleInstance(String roleName) throws UnknownEntityException {
        
        return null;
    }

    public Class getAclClass() throws UnknownEntityException {
        
        return null;
    }

    public AccessControlList getAclInstance(Map roles, Map permissions) throws UnknownEntityException {
        
        return null;
    }

    public boolean accountExists(String userName) throws DataBackendException {
        
        return false;
    }

    public boolean accountExists(User user) throws DataBackendException {
        
        return false;
    }

    public User getAuthenticatedUser(String username, String password) throws DataBackendException,
            UnknownEntityException, PasswordMismatchException {
        
        return null;
    }

    public User getUser(String username) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public User[] getUsers(Criteria criteria) throws DataBackendException {
        
        return null;
    }

    public User getAnonymousUser() throws UnknownEntityException {
        
        return null;
    }

    public void saveUser(User user) throws UnknownEntityException, DataBackendException {
        

    }

    public void addUser(User user, String password) throws DataBackendException, EntityExistsException {
        

    }

    public void removeUser(User user) throws DataBackendException, UnknownEntityException {
        

    }

    public String encryptPassword(String password) {
        
        return null;
    }

    public void changePassword(User user, String oldPassword, String newPassword) throws PasswordMismatchException,
            UnknownEntityException, DataBackendException {
        

    }

    public void forcePassword(User user, String password) throws UnknownEntityException, DataBackendException {
        

    }

    public AccessControlList getACL(User user) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public PermissionSet getPermissions(Role role) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public void grant(User user, Group group, Role role) throws DataBackendException, UnknownEntityException {
        

    }

    public void revoke(User user, Group group, Role role) throws DataBackendException, UnknownEntityException {
        

    }

    public void revokeAll(User user) throws DataBackendException, UnknownEntityException {
        

    }

    public void grant(Role role, Permission permission) throws DataBackendException, UnknownEntityException {
        

    }

    public void revoke(Role role, Permission permission) throws DataBackendException, UnknownEntityException {
        

    }

    public void revokeAll(Role role) throws DataBackendException, UnknownEntityException {
        

    }

    public Group getGlobalGroup() {
        
        return null;
    }

    public Group getNewGroup(String groupName) {
        
        return null;
    }

    public Role getNewRole(String roleName) {
        
        return null;
    }

    public Permission getNewPermission(String permissionName) {
        
        return null;
    }

    public Group getGroup(String name) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public Role getRole(String name) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public Permission getPermission(String name) throws DataBackendException, UnknownEntityException {
        
        return null;
    }

    public GroupSet getGroups(Criteria criteria) throws DataBackendException {
        
        return null;
    }

    public RoleSet getRoles(Criteria criteria) throws DataBackendException {
        
        return null;
    }

    public PermissionSet getPermissions(Criteria criteria) throws DataBackendException {
        
        return null;
    }

    public GroupSet getAllGroups() throws DataBackendException {
        
        return null;
    }

    public RoleSet getAllRoles() throws DataBackendException {
        
        return null;
    }

    public PermissionSet getAllPermissions() throws DataBackendException {
        
        return null;
    }

    public void saveGroup(Group group) throws DataBackendException, UnknownEntityException {
        

    }

    public void saveRole(Role role) throws DataBackendException, UnknownEntityException {
        

    }

    public void savePermission(Permission permission) throws DataBackendException, UnknownEntityException {
        

    }

    public Group addGroup(Group group) throws DataBackendException, EntityExistsException {
        
        return null;
    }

    public Role addRole(Role role) throws DataBackendException, EntityExistsException {
        
        return null;
    }

    public Permission addPermission(Permission permission) throws DataBackendException, EntityExistsException {
        
        return null;
    }

    public void removeGroup(Group group) throws DataBackendException, UnknownEntityException {
        

    }

    public void removeRole(Role role) throws DataBackendException, UnknownEntityException {
        

    }

    public void removePermission(Permission permission) throws DataBackendException, UnknownEntityException {
        

    }

    public void renameGroup(Group group, String name) throws DataBackendException, UnknownEntityException {
        

    }

    public void renameRole(Role role, String name) throws DataBackendException, UnknownEntityException {
        

    }

    public void renamePermission(Permission permission, String name) throws DataBackendException,
            UnknownEntityException {
        

    }

    public void init() throws InitializationException {
        

    }

    public void shutdown() {
        

    }

    public boolean isInitialized() {
        
        return false;
    }

    public void setServiceBroker(ServiceBroker broker) {
        

    }

    public void setName(String name) {
        

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getRealPath(String path) {
        
        return null;
    }

    public Category getCategory() {
        
        return null;
    }

    public String getStatus() throws ServiceException {
        
        return null;
    }

}
