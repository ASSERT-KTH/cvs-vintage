/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/Mangler.java,v 1.3 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.3 $
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
 * You can control attributes like classname, packagename etc by
 * plugging in your own mangler. 
 *
 * @author Anil K. Vijendran
 */
public interface Mangler {
    /** The class name ( without package ) of the
     *  generated servlet, including the version number
     */
    String getClassName();

    /** The package name. It is based on the .jsp path, with
     *  all unsafe components escaped.
     */
    String getPackageName();

    /** The full name of the .java file, including
     *  version number ( based on className and outputDir )
     */
    String getJavaFileName();

    /** The full name of the .class file ( without version number)
     */
    String getClassFileName();
}
