/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/ClassDeclarationPhase.java,v 1.2 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.2 $
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
 * When a generator implements ClassDeclarationPhase, its generate
 * method will only be invoked while generating the servlet's class
 * body and not during the service method phase. 
 *
 * @author Anil K. Vijendran
 */
public interface ClassDeclarationPhase {
}
