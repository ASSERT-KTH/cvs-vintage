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

package org.columba.mail.parser;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class MailUrlParser {
	
	private static String[] keys = { "mailto:", "cc=", "bcc=", "subject=" };
	
	private static String delimiter = "?";

	private Hashtable result;
	
	private KeyMatcher[] matchers;	
	
	private int size;
	
	public MailUrlParser( String mailurl ) {		
		result = new Hashtable();		
			
		size = Array.getLength( keys );
			
		matchers = new KeyMatcher[size];
			
		for( int j=0; j<size; j++ ) {
				
			matchers[j] = new KeyMatcher( keys[j] );
					
		}
			
		parse( mailurl );	
		
	}
	
	public static boolean isMailUrl(String url) {
		return url.startsWith("mailto");
	}
	
	public Object get( Object key ) {
		return result.get(key);
	}
	
	protected void parse( String mailurl ) {
		int length = mailurl.length();
		char c;
	
		StringTokenizer tokenizer = new StringTokenizer( mailurl, delimiter );
		String token;
	
		while( tokenizer.hasMoreTokens() ) {
			
			token = tokenizer.nextToken();
			
			for( int i=0; i<token.length(); i++ ) {
					
				c = token.charAt( i );
			
				for( int j=0; j<size; j++ ) {				
					if( matchers[j].next( c ) ) {						
						result.put(keys[j],token.substring(i+1));					
					}																
				}
			}	
		}
			
	}
	

}

class KeyMatcher {
	
	String key;
	char[] keys;
	
	int matchpos;
	int length;
	
	public KeyMatcher( String key ) {		
		this.key = key;
		
		keys = key.toCharArray();			
		matchpos = 0;
		length = key.length();
	}
	
	public boolean next( char c ) {
		
		if( c == keys[matchpos] ) {
			matchpos++;
			if( matchpos == length ) {
				matchpos = 0;
				return true;
			}
		} else matchpos = 0;
		
		return false;	
	}
}