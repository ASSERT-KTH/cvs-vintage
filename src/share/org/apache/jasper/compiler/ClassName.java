/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/ClassName.java,v 1.5 2004/02/23 06:22:36 billbarker Exp $
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

import org.apache.jasper.JasperException;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.jasper.Constants;

/**
 * Parse a .class file to figure out the name of the class from which
 * it was generated. 
 *
 * @author Anil Vijendran
 */
public class ClassName {

    static String processClassData(InputStream in) throws JasperException, IOException {
	DataInputStream din = new DataInputStream(in);
	din.readInt(); // magic
	din.readUnsignedShort(); // majorVersion
	din.readUnsignedShort(); // minorVersion
	int count = din.readUnsignedShort(); // #constant pool entries
	ConstantPool[] constantPool = new ConstantPool[count];
	constantPool[0] = new ConstantPool();
	for (int i = 1; i < constantPool.length; i++) {
	    constantPool[i] = new ConstantPool();
	    if (!constantPool[i].read(din))
		throw new JasperException(Constants.getString("jsp.error.classname"));
	    // These two types take up "two" spots in the table
	    if ((constantPool[i].type == ConstantPool.LONG) ||
		(constantPool[i].type == ConstantPool.DOUBLE))
		i++;
	}

	for (int i = 1; i < constantPool.length; i++) {
	    if (constantPool[i] == null)
		continue;
	    if (constantPool[i].index1 > 0)
		constantPool[i].arg1 = constantPool[constantPool[i].index1];
	    if (constantPool[i].index2 > 0)
		constantPool[i].arg2 = constantPool[constantPool[i].index2];
	}
	int accessFlags = din.readUnsignedShort();
	ConstantPool thisClass = constantPool[din.readUnsignedShort()];
        din.close();
	return printClassName(thisClass.arg1.strValue);
    }

    private static String printClassName(String s) {
	StringBuffer x;

	if (s.charAt(0) == '[') {
	    return(typeString(s, ""));
	}

	x = new StringBuffer();
	for (int j = 0; j < s.length(); j++) {
	    if (s.charAt(j) == '/')
		x.append('.');
	    else
		x.append(s.charAt(j));
	}
	return (x.toString());
    }

    private static String typeString(String typeString, String varName) {
	    int isArray = 0;
	    int	ndx = 0;
	    StringBuffer x = new StringBuffer();

	    while (typeString.charAt(ndx) == '[') {
	        isArray++;
	        ndx++;
	    }

	    switch (typeString.charAt(ndx)) {
	        case 'B' :
		        x.append("byte ");
		        break;
	        case 'C' :
		        x.append("char ");
		        break;
	        case 'D' :
		        x.append("double ");
		        break;
	        case 'F' :
		        x.append("float ");
		        break;
	        case 'I' :
		        x.append("int ");
		        break;
	        case 'J' :
		        x.append("long ");
		        break;
	        case 'L' :
		        for (int i = ndx+1; i < typeString.indexOf(';'); i++) {
		            if (typeString.charAt(i) != '/')
			            x.append(typeString.charAt(i));
		            else
			        x.append('.');
		        }
		        x.append(" ");
		        break;
	        case 'V':
		        x.append("void ");
		        break;
	        case 'S' :
		        x.append("short ");
		        break;
	        case 'Z' :
		        x.append("boolean ");
		        break;
	    }
	    x.append(varName);
	    while (isArray > 0) {
	        x.append("[]");
	        isArray--;
	    }
	    return (x.toString());
    }

    public static String getClassName(String classFile) throws JasperException {
	try {
	    //	    System.out.println("Getting class name from class data");
	    FileInputStream fin = new FileInputStream(classFile);
	    return processClassData(fin);
	} catch (IOException ex) {
	    throw new JasperException(Constants.getString("jsp.error.classname"), 
	    					ex);
	}
    }
    
    public static void main(String[] args) {
	try {
	    for(int i = 0; i < args.length; i++)
		System.out.println("Filename: "+ args[i]+" Classname: "+getClassName(args[i]));
	} catch (Exception ex) {
	    ex.printStackTrace();	// OK
	}
    }
}

class ConstantPool {
    int	type;			// type of this item
    String name; 		// String for the type
    ConstantPool  arg1;	// index to first argument
    ConstantPool  arg2;	// index to second argument
    int index1, index2;
    String 	strValue; 		// ASCIZ String value
    int		intValue;
    long	longValue;
    float	floatValue;
    double	doubleValue;

    public static final int CLASS = 7;
    public static final int FIELDREF = 9;
    public static final int METHODREF = 10;
    public static final int STRING = 8;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int LONG = 5;
    public static final int DOUBLE = 6;
    public static final int INTERFACE = 11;
    public static final int NAMEANDTYPE = 12;
    public static final int ASCIZ = 1;
    public static final int UNICODE = 2;


    /**
     * Generic constructor
     */
    public ConstantPool() {
	index1 = -1;
	index2 = -1;
	arg1 = null;
	arg2 = null;
	type = -1;
    }

    public boolean read(DataInputStream din)
	throws IOException {
	int	len;
	char	c;

	type = din.readByte();
	switch (type) {
	    case CLASS:
		name = "Class";
		index1 = din.readUnsignedShort();
		index2 = -1;
		break;
	    case FIELDREF:
		name = "Field Reference";
		index1 = din.readUnsignedShort();
		index2 = din.readUnsignedShort();
		break;
	    case METHODREF:
		name = "Method Reference";
		index1 = din.readUnsignedShort();
		index2 = din.readUnsignedShort();
		break;
	    case INTERFACE:
		name = "Interface Method Reference";
		index1 = din.readUnsignedShort();
		index2 = din.readUnsignedShort();
		break;
	    case NAMEANDTYPE:
		name = "Name and Type";
		index1 = din.readUnsignedShort();
		index2 = din.readUnsignedShort();
		break;
	    case STRING:
		name = "String";
		index1 = din.readUnsignedShort();
		index2 = -1;
		break;
	    case INTEGER:
		name = "Integer";
		intValue = din.readInt();
		break;
	    case FLOAT:
		name = "Float";
		floatValue = din.readFloat();
		break;
	    case LONG:
		name = "Long";
		longValue = din.readLong();
		break;
	    case DOUBLE:
		name = "Double";
		doubleValue = din.readDouble();
		break;
	    case ASCIZ:
	    case UNICODE:
		if (type == ASCIZ)
		    name = "ASCIZ";
		else
		    name = "UNICODE";

		StringBuffer xxBuf = new StringBuffer();

		len = din.readUnsignedShort();
		while (len > 0) {
		    c = (char) (din.readByte());
		    xxBuf.append(c);
		    len--;
		}
		strValue = xxBuf.toString();
		break;
	    default:
		System.err.println(Constants.getString("jsp.warning.bad.type"));
	}
	return (true);
    }
}

