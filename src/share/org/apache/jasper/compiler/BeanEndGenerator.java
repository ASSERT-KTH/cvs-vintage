/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/BeanEndGenerator.java,v 1.2 2004/02/23 02:45:11 billbarker Exp $
 * $Revision: 1.2 $
 * $Date: 2004/02/23 02:45:11 $
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
 * Deal with </jsp:useBean>.
 *
 * @author Mandar Raje.
 */
public class BeanEndGenerator extends GeneratorBase implements ServiceMethodPhase {
    // Will chage this later.
    public BeanEndGenerator() {
    }
    

    public void generate(ServletWriter writer, Class phase) {
	writer.println("}");
    }
    
}
