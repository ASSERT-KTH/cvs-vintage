// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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