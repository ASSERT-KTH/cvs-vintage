/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/CharDataGenerator.java,v 1.8 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.8 $
 * $Date: 2004/02/23 06:22:36 $
 *
 *
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.compiler;

/**
 * CharDataGenerator generates the character data present in the JSP
 * file. Typically this is HTML which lands up as strings in
 * out.println(...). 
 *
 * @author Anil K. Vijendran
 */
public class CharDataGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase
{
    char[] chars;

    // process in 32k chunks
    private static final int MAXSIZE = 32 * 1024;
    
    public CharDataGenerator(char[] chars) {
	this.chars = chars;
    }

    public void generate(ServletWriter writer, Class phase) {
	writer.indent();
	int current	= 0;
	int limit       = chars.length;
	while (current < limit) {
	    int from = current;
	    int to = Math.min(current + MAXSIZE, limit);
	    generateChunk(writer, from, to);
	    current = to;
	    writer.println();
	}
    }

    private void generateChunk(ServletWriter writer, int from, int to) {
	writer.print("out.write(\"");
	// Generate the char data:
	StringBuffer sb = new StringBuffer();
	for (int i = from ; i < to ; i++) {
	    int ch = chars[i];
	    switch(ch) {
	    case '"':
		sb.append("\\\"");
		break;
	    case '\\':
		sb.append("\\\\");
		break;
	    case '\r':
		sb.append("\\r");
		break;
		/*
		  case '\'':
		  sb.append('\\');
		  sb.append('\'');
		  break;
		*/
	    case '\n':
		sb.append("\\n");
		break;
	    case '\t':
		sb.append("\\t");
		break;
	    default:
		this.writeChar((char) ch, sb);
	    }
	}
	writer.print(sb.toString());
	writer.print("\");");
	writer.println();
    }

    protected void writeChar( char c, StringBuffer buf ) {
        if ( c < 128 )
    	// if char is pure ASCII -> write it
            buf.append( c );
        else {
        // if char isn't pure ASCII -> write it's unicode
            buf.append( "\\u" );
            String hexa = Integer.toHexString( c );
            for( int i = hexa.length() ; i < 4 ; i++ )
                buf.append( '0' );
            buf.append( hexa );
        }
    }

}
