/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/FileDeclarationPhase.java,v 1.3 2004/02/23 06:22:36 billbarker Exp $
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
 * If a generator wants to output stuff at "file scope" in the
 * generated servlet class, it should implement this interface. The
 * code would go outside the class body. (For example non-public
 * support classes can be generated this way)
 *
 * @author Anil K. Vijendran
 */
public interface FileDeclarationPhase {
}
