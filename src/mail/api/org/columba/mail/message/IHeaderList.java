// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.message;

import java.util.Enumeration;
import java.util.Set;

import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;

/**
 * @author fdietz
 *
 */
public interface IHeaderList {
	void add(IColumbaHeader header, Object uid);

	int count();

	boolean exists(Object uid);

	boolean containsValue(Object value);

	IColumbaHeader get(Object uid);

	IColumbaHeader remove(Object uid);

	Enumeration keys();
	
	Set keySet();

	Object[] getUids();
	
	public void setAttribute(Object uid, String key, Object value);
	
	public Object getAttribute(Object uid, String key);
	
	public Flags getFlags(Object uid);
	
	public Attributes getAttributes(Object uid);
	
	public Header getHeaderFields(Object uid, String[] keys);
	
	Enumeration elements();
	
	void clear();
}