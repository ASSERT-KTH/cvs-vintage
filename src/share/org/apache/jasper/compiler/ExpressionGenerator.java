/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/ExpressionGenerator.java,v 1.3 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.3 $
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
 * Generator to deal with JSP expressions: <%= ... %> stuff. 
 *
 * @author Anil K. Vijendran
 */
public class ExpressionGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    char[] chars;
    
    public ExpressionGenerator(char[] chars) {
	this.chars = chars;
    }
    
    public void generate(ServletWriter writer, Class phase) {
	writer.println("out.print("+new String(JspUtil.removeQuotes(chars))+");");
    }
}


