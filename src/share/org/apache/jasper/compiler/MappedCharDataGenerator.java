/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/MappedCharDataGenerator.java,v 1.6 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/23 02:45:12 $
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
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

/**
 * CharDataGenerator generates the character data present in the JSP
 * file. Typically this is HTML which lands up as strings in
 * out.println(...).
 * 
 * This generator will print the HTML line-by-line. This is a
 * feature desired by lots of tool vendors.
 *
 * @author Mandar Raje
 */
public class MappedCharDataGenerator extends CharDataGenerator {
    
    public MappedCharDataGenerator(char[] chars) {
	super(chars);
    }

    public void generate(ServletWriter writer, Class phase) {
	writer.indent();
	writer.print("out.write(\"");
	// Generate the char data:
	int limit       = chars.length;
	StringBuffer sb = new StringBuffer();
	for (int i = 0 ; i < limit ; i++) {
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
		writer.print(sb.toString());
		writer.print("\");\n");
		sb = new StringBuffer();
		writer.indent();
		writer.print("out.write(\"");
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
}
