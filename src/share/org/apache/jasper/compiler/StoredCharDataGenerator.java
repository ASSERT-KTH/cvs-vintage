/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/StoredCharDataGenerator.java,v 1.5 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.5 $
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

import java.util.Vector;

/**
 * StoredCharDataGenerator generates HTML and other data present in
 * JSP files to be stored/serialized into a .dat file. 
 *
 * @author Anil K. Vijendran
 */
public class StoredCharDataGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase, InitMethodPhase, ClassDeclarationPhase
{
    int stringId;
    char[] chars;
    Vector vector;
    String fileName;
    
    public StoredCharDataGenerator(Vector vector, String fileName, 
                                   int stringId, char[] chars) {
        this.stringId = stringId;
	this.chars = chars;
        this.vector = vector;
        this.fileName = fileName;
    }

    private final String getStringVar() {
        return "_jspx_html_data["+stringId+"]";
    }

    private final void generateRef(ServletWriter writer) {
        if (stringId == 0)
            writer.println("static char[][] _jspx_html_data = null;");
    }

    private final void generateInit(ServletWriter writer) {
        if (stringId == 0) {
            String name = writer.quoteString(fileName);
            writer.println("java.io.ObjectInputStream oin = null;");
            writer.println("int numStrings = 0;");
            writer.println("try {");
            writer.pushIndent();
            writer.println("java.io.FileInputStream fin = new java.io.FileInputStream("+name+");");
            writer.println("oin = new java.io.ObjectInputStream(fin);");
            writer.println("_jspx_html_data = (char[][]) oin.readObject();");
            writer.popIndent();
            writer.println("} catch (Exception ex) {");
            writer.pushIndent();
            writer.println("throw new org.apache.jasper.JasperException(\"Unable to open data file\");");
            writer.popIndent();
            writer.println("} finally {");
            writer.pushIndent();
            writer.println("if (oin != null)");
            writer.pushIndent();
            writer.println("try { oin.close(); } catch (java.io.IOException ignore) { }");
            writer.popIndent();
            writer.popIndent();
            writer.println("}");
        }
    }

    private final void generatePrint(ServletWriter writer) {
        writer.println("out.print("+getStringVar()+");");
        vector.addElement(chars);
    }

    public void generate(ServletWriter writer, Class phase) {
        if (phase.equals(ClassDeclarationPhase.class))
            generateRef(writer);
        else if (phase.equals(InitMethodPhase.class))
            generateInit(writer);
        else if (phase.equals(ServiceMethodPhase.class))
            generatePrint(writer);
    }

    public boolean generateCoordinates(Class phase) {
        return false;
    }
}
