/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.tomcat.facade;

import javax.servlet.http.Cookie;

import org.apache.tomcat.util.http.ServerCookie;

/**
 * Facade for a ServerCookie object. The ServerCookie is a recyclable
 * and efficient Cookie implementation. The Facades makes sure the
 * user "sees" only what's permited by the servlet spec.
 *
 * @author Costin Manolache
 */
final class CookieFacade extends Cookie {
    ServerCookie sC;
    
    CookieFacade( ServerCookie sC ) {
	// we can't reuse super anyway
	super("", "");
	this.sC=sC;
    }
    public void setComment(String purpose) {
	sC.getComment().setString( purpose);
    }
    public String getComment() {
	return sC.getComment().toString();
    }
    public void setDomain(String pattern) {
	sC.getDomain().setString( pattern.toLowerCase());
	// IE allegedly needs this
    }
    public String getDomain() {
	return sC.getDomain().toString();
    }
    public void setMaxAge(int expiry) {
	sC.setMaxAge(expiry);
    }
    public int getMaxAge() {
	return sC.getMaxAge();
    }
    public void setPath(String uri) {
	sC.getPath().setString( uri );
    }
    public String getPath() {
	return sC.getPath().toString();
    }
    public void setSecure(boolean flag) {
	sC.setSecure( flag );
    }
    public boolean getSecure() {
	return sC.getSecure();
    }
    public String getName() {
	return sC.getName().toString();
    }
    public void setValue(String newValue) {
	sC.getValue().setString(newValue);
    }
    public String getValue() {
	return sC.getValue().toString();
    }
    public int getVersion() {
	return sC.getVersion();
    }
    public void setVersion(int v) {
	sC.setVersion(v);
    }
}
