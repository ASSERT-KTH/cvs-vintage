//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.main;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class MessageOptionParser {

	private static final int KEY = 0;
	private static final int VALUE = 1;
	private static final int QUOTED_VALUE = 2;
	
	
	public static Map parse(String in) {
		Hashtable result = new Hashtable();
		StringBuffer key = new StringBuffer();
		StringBuffer value = new StringBuffer();
		ArrayList values = new ArrayList();
		
		// Remove quotes if present
		if( in.startsWith("\"")) {
			in = in.substring(1, in.length()-1);
		}
		
		int state = 0;
		int pos = 0;
		
		while( pos < in.length() ) {			
			char ch = in.charAt(pos);
			
			switch( ch ) {
				case '\\' : {
					// Allow escaping
					pos++;
					ch = in.charAt(pos);
					
					if( state == KEY ) {
						key.append(ch);
					} else if( state == VALUE | state == QUOTED_VALUE ) {
						value.append(ch);
					}
					
					break;
				}	
			
				case ',' : {
					if( state == VALUE ) {
						result.put(key.toString(), value.toString());
						key.delete(0, key.length());
						value.delete(0,value.length());
						
						state = KEY;
					} else if( state == QUOTED_VALUE) {
						values.add(value.toString());
						value.delete(0,value.length());
					}
					
					break;
				}
			
				case '=' : {
					state = VALUE;
					break;
				}
				
				case '\'' : {
					if( state == VALUE )  {
						state = QUOTED_VALUE;
					} else if( state == QUOTED_VALUE) {
						values.add(value.toString());
						result.put(key.toString(),values.toArray(new String[0]));
						
						key.delete(0, key.length());
						value.delete(0,value.length());
						values.clear();
						
						state = KEY;
					}
					break;
				}
				
				default: {
					if( state == KEY ) {
						key.append(ch);
					} else if( state == VALUE | state == QUOTED_VALUE ) {
						value.append(ch);
					}
					
					break;
				}
			}
			
			pos++;			
		}

		
		// Put the last
		if( state == VALUE ) {
			result.put(key.toString(), value.toString());
		} else if( state == QUOTED_VALUE) {
			values.add(value.toString());
			result.put(key,values.toArray(new String[0]));
		}
		
		return result;
	}
}
