/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public interface TypeConstants {
	char[] JAVA = "java".toCharArray(); //$NON-NLS-1$
	char[] LANG = "lang".toCharArray(); //$NON-NLS-1$
	char[] IO = "io".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION = "annotation".toCharArray(); //$NON-NLS-1$
	char[] REFLECT = "reflect".toCharArray(); //$NON-NLS-1$
	char[] LENGTH = "length".toCharArray(); //$NON-NLS-1$
	char[] CLONE = "clone".toCharArray(); //$NON-NLS-1$
	char[] GETCLASS = "getClass".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	char[] MAIN = "main".toCharArray(); //$NON-NLS-1$
	char[] SERIALVERSIONUID = "serialVersionUID".toCharArray(); //$NON-NLS-1$
	char[] SERIALPERSISTENTFIELDS = "serialPersistentFields".toCharArray(); //$NON-NLS-1$ 
	char[] READRESOLVE = "readResolve".toCharArray(); //$NON-NLS-1$
	char[] WRITEREPLACE = "writeReplace".toCharArray(); //$NON-NLS-1$
	char[] READOBJECT = "readObject".toCharArray(); //$NON-NLS-1$
	char[] WRITEOBJECT = "writeObject".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_LANG_OBJECT = "java.lang.Object".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_LANG_ENUM = "java.lang.Enum".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTINPUTSTREAM = "java.io.ObjectInputStream".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTOUTPUTSTREAM = "java.io.ObjectOutputStream".toCharArray(); //$NON-NLS-1$
	char[] CharArray_JAVA_IO_OBJECTSTREAMFIELD = "java.io.ObjectStreamField".toCharArray(); //$NON-NLS-1$
	char[] ANONYM_PREFIX = "new ".toCharArray(); //$NON-NLS-1$
	char[] ANONYM_SUFFIX = "(){}".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_NAME = { '?' };
    char[] WILDCARD_SUPER = " super ".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_EXTENDS = " extends ".toCharArray(); //$NON-NLS-1$
    char[] WILDCARD_MINUS = { '-' };
    char[] WILDCARD_STAR = { '*' };
    char[] WILDCARD_PLUS = { '+' };
	char[] BYTE = "byte".toCharArray(); //$NON-NLS-1$
	char[] SHORT = "short".toCharArray(); //$NON-NLS-1$
	char[] INT = "int".toCharArray(); //$NON-NLS-1$
	char[] LONG = "long".toCharArray(); //$NON-NLS-1$
	char[] FLOAT = "float".toCharArray(); //$NON-NLS-1$
	char[] DOUBLE = "double".toCharArray(); //$NON-NLS-1$
	char[] CHAR = "char".toCharArray(); //$NON-NLS-1$
	char[] BOOLEAN = "boolean".toCharArray(); //$NON-NLS-1$
	char[] NULL = "null".toCharArray(); //$NON-NLS-1$
	char[] VOID = "void".toCharArray(); //$NON-NLS-1$
    char[] VALUE = "value".toCharArray(); //$NON-NLS-1$
    char[] VALUES = "values".toCharArray(); //$NON-NLS-1$
    char[] VALUEOF = "valueOf".toCharArray(); //$NON-NLS-1$
    char[] SOURCE = "SOURCE".toCharArray(); //$NON-NLS-1$
    char[] CLASS = "CLASS".toCharArray(); //$NON-NLS-1$
    char[] RUNTIME = "RUNTIME".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION_PREFIX = "@".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION_SUFFIX = "()".toCharArray(); //$NON-NLS-1$
    
    
	// Constant compound names
	char[][] JAVA_LANG = {JAVA, LANG};
	char[][] JAVA_IO = {JAVA, IO};
	char[][] JAVA_LANG_ANNOTATION_ANNOTATION = {JAVA, LANG, ANNOTATION, "Annotation".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ASSERTIONERROR = {JAVA, LANG, "AssertionError".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLASS = {JAVA, LANG, "Class".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLASSNOTFOUNDEXCEPTION = {JAVA, LANG, "ClassNotFoundException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CLONEABLE = {JAVA, LANG, "Cloneable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ENUM = {JAVA, LANG, "Enum".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_EXCEPTION = {JAVA, LANG, "Exception".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ERROR = {JAVA, LANG, "Error".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ILLEGALARGUMENTEXCEPTION = {JAVA, LANG, "IllegalArgumentException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ITERABLE = {JAVA, LANG, "Iterable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_NOCLASSDEFERROR = {JAVA, LANG, "NoClassDefError".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_OBJECT = {JAVA, LANG, OBJECT};
	char[][] JAVA_LANG_STRING = {JAVA, LANG, "String".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_STRINGBUFFER = {JAVA, LANG, "StringBuffer".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_STRINGBUILDER = {JAVA, LANG, "StringBuilder".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SYSTEM = {JAVA, LANG, "System".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_RUNTIMEEXCEPTION = {JAVA, LANG, "RuntimeException".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_THROWABLE = {JAVA, LANG, "Throwable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_REFLECT_CONSTRUCTOR = {JAVA, LANG, REFLECT, "Constructor".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_PRINTSTREAM = {JAVA, IO, "PrintStream".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_IO_SERIALIZABLE = {JAVA, IO, "Serializable".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_BYTE = {JAVA, LANG, "Byte".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SHORT = {JAVA, LANG, "Short".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_CHARACTER = {JAVA, LANG, "Character".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_INTEGER = {JAVA, LANG, "Integer".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_LONG = {JAVA, LANG, "Long".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_FLOAT = {JAVA, LANG, "Float".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_DOUBLE = {JAVA, LANG, "Double".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_BOOLEAN = {JAVA, LANG, "Boolean".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_VOID = {JAVA, LANG, "Void".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_UTIL_ITERATOR = {JAVA, "util".toCharArray(), "Iterator".toCharArray()}; //$NON-NLS-1$//$NON-NLS-2$
	char[][] JAVA_LANG_DEPRECATED = {JAVA, LANG, "Deprecated".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_DOCUMENTED = {JAVA, LANG, ANNOTATION, "Documented".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_INHERITED = {JAVA, LANG, ANNOTATION, "Inherited".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_OVERRIDE = {JAVA, LANG, "Override".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_RETENTION = {JAVA, LANG, ANNOTATION, "Retention".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_SUPPRESSWARNINGS = {JAVA, LANG, "SuppressWarnings".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_TARGET = {JAVA, LANG, ANNOTATION, "Target".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_RETENTIONPOLICY = {JAVA, LANG, ANNOTATION, "RetentionPolicy".toCharArray()}; //$NON-NLS-1$
	char[][] JAVA_LANG_ANNOTATION_ELEMENTTYPE = {JAVA, LANG, ANNOTATION, "ElementType".toCharArray()}; //$NON-NLS-1$
	

	// Constants used by the flow analysis
	int EqualOrMoreSpecific = -1;
	int NotRelated = 0;
	int MoreGeneric = 1;

	// Shared binding collections
	TypeBinding[] NoParameters = new TypeBinding[0];
	ReferenceBinding[] NoExceptions = new ReferenceBinding[0];
	ReferenceBinding[] AnyException = new ReferenceBinding[] { null }; // special handler for all exceptions
	FieldBinding[] NoFields = new FieldBinding[0];
	MethodBinding[] NoMethods = new MethodBinding[0];
	ReferenceBinding[] NoSuperInterfaces = new ReferenceBinding[0];
	ReferenceBinding[] NoMemberTypes = new ReferenceBinding[0];
	TypeVariableBinding[] NoTypeVariables = new TypeVariableBinding[0];
	AnnotationBinding[] NoAnnotations = new AnnotationBinding[0];	
	
	// Synthetics
	char[] INIT = "<init>".toCharArray(); //$NON-NLS-1$
	char[] CLINIT = "<clinit>".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ENUM_VALUES = "ENUM$VALUES".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ASSERT_DISABLED = "$assertionsDisabled".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_CLASS = "class$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_OUTER_LOCAL_PREFIX = "val$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ENCLOSING_INSTANCE_PREFIX = "this$".toCharArray(); //$NON-NLS-1$
	char[] SYNTHETIC_ACCESS_METHOD_PREFIX =  "access$".toCharArray(); //$NON-NLS-1$
}
