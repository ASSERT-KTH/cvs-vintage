package org.eclipse.jdt.internal.compiler.codegen;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.jdt.internal.compiler.ClassFile;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * This type is used to store all the constant pool entries.
 */
public class ConstantPool implements ClassFileConstants, TypeIds {
	public static final int DOUBLE_INITIAL_SIZE = 5;
	public static final int FLOAT_INITIAL_SIZE = 3;
	public static final int INT_INITIAL_SIZE = 248;
	public static final int LONG_INITIAL_SIZE = 5;
	public static final int UTF8_INITIAL_SIZE = 778;
	public static final int STRING_INITIAL_SIZE = 761;
	public static final int FIELD_INITIAL_SIZE = 156;
	public static final int METHOD_INITIAL_SIZE = 236;
	public static final int INTERFACE_INITIAL_SIZE = 50;
	public static final int CLASS_INITIAL_SIZE = 86;
	public static final int NAMEANDTYPE_INITIAL_SIZE = 272;
	public static final int CONSTANTPOOL_INITIAL_SIZE = 2000;
	public static final int CONSTANTPOOL_GROW_SIZE = 6000;
	protected DoubleCache doubleCache;
	protected FloatCache floatCache;
	protected IntegerCache intCache;
	protected LongCache longCache;
	public CharArrayCache UTF8Cache;
	protected CharArrayCache stringCache;
	protected ObjectCache fieldCache;
	protected ObjectCache methodCache;
	protected ObjectCache interfaceMethodCache;
	protected ObjectCache classCache;
	protected FieldNameAndTypeCache nameAndTypeCacheForFields;
	protected MethodNameAndTypeCache nameAndTypeCacheForMethods;
	int[] wellKnownTypes = new int[20];
	int[] wellKnownMethods = new int[26];
	int[] wellKnownFields = new int[10];
	int[] wellKnownFieldNameAndTypes = new int[2];
	int[] wellKnownMethodNameAndTypes = new int[24];
	public byte[] poolContent;
	public int currentIndex = 1;
	public int currentOffset;
	// predefined constant index for well known types
	final static int JAVA_LANG_BOOLEAN_TYPE = 0;
	final static int JAVA_LANG_BYTE_TYPE = 1;
	final static int JAVA_LANG_CHARACTER_TYPE = 2;
	final static int JAVA_LANG_DOUBLE_TYPE = 3;
	final static int JAVA_LANG_FLOAT_TYPE = 4;
	final static int JAVA_LANG_INTEGER_TYPE = 5;
	final static int JAVA_LANG_LONG_TYPE = 6;
	final static int JAVA_LANG_SHORT_TYPE = 7;
	final static int JAVA_LANG_VOID_TYPE = 8;
	final static int JAVA_LANG_CLASS_TYPE = 9;
	final static int JAVA_LANG_CLASSNOTFOUNDEXCEPTION_TYPE = 10;
	final static int JAVA_LANG_NOCLASSDEFFOUNDERROR_TYPE = 11;
	final static int JAVA_LANG_OBJECT_TYPE = 12;
	final static int JAVA_LANG_STRING_TYPE = 13;
	final static int JAVA_LANG_STRINGBUFFER_TYPE = 14;
	final static int JAVA_LANG_SYSTEM_TYPE = 15;
	final static int JAVA_LANG_THROWABLE_TYPE = 16;
	final static int JAVA_LANG_ERROR_TYPE = 17;
	final static int JAVA_LANG_EXCEPTION_TYPE = 18;
	final static int JAVA_LANG_REFLECT_CONSTRUCTOR_TYPE = 19;
	// predefined constant index for well known fields	
	final static int TYPE_BYTE_FIELD = 0;
	final static int TYPE_SHORT_FIELD = 1;
	final static int TYPE_CHARACTER_FIELD = 2;
	final static int TYPE_INTEGER_FIELD = 3;
	final static int TYPE_LONG_FIELD = 4;
	final static int TYPE_FLOAT_FIELD = 5;
	final static int TYPE_DOUBLE_FIELD = 6;
	final static int TYPE_BOOLEAN_FIELD = 7;
	final static int TYPE_VOID_FIELD = 8;
	final static int OUT_SYSTEM_FIELD = 9;
	// predefined constant index for well known methods	
	final static int FORNAME_CLASS_METHOD = 0;
	final static int NOCLASSDEFFOUNDERROR_CONSTR_METHOD = 1;
	final static int APPEND_INT_METHOD = 2;
	final static int APPEND_FLOAT_METHOD = 3;
	final static int APPEND_LONG_METHOD = 4;
	final static int APPEND_OBJECT_METHOD = 5;
	final static int APPEND_CHAR_METHOD = 6;
	final static int APPEND_STRING_METHOD = 7;
	final static int APPEND_BOOLEAN_METHOD = 8;
	final static int APPEND_DOUBLE_METHOD = 9;
	final static int STRINGBUFFER_STRING_CONSTR_METHOD = 10;
	final static int STRINGBUFFER_DEFAULT_CONSTR_METHOD = 11;
	final static int STRINGBUFFER_TOSTRING_METHOD = 12;
	final static int SYSTEM_EXIT_METHOD = 13;
	final static int THROWABLE_GETMESSAGE_METHOD = 14;
	final static int JAVALANGERROR_CONSTR_METHOD = 15;
	final static int GETCONSTRUCTOR_CLASS_METHOD = 16;
	final static int NEWINSTANCE_CONSTRUCTOR_METHOD = 17;
	final static int STRING_INTERN_METHOD = 18;
	final static int VALUEOF_INT_METHOD = 19;
	final static int VALUEOF_FLOAT_METHOD = 20;
	final static int VALUEOF_LONG_METHOD = 21;
	final static int VALUEOF_OBJECT_METHOD = 22;
	final static int VALUEOF_CHAR_METHOD = 23;
	final static int VALUEOF_BOOLEAN_METHOD = 24;
	final static int VALUEOF_DOUBLE_METHOD = 25;
	// predefined constant index for well known name and type for fields
	final static int TYPE_JAVALANGCLASS_NAME_AND_TYPE = 0;
	final static int OUT_SYSTEM_NAME_AND_TYPE = 1;
	// predefined constant index for well known name and type for methods
	final static int FORNAME_CLASS_METHOD_NAME_AND_TYPE = 0;
	final static int STRING_CONSTR_METHOD_NAME_AND_TYPE = 1;
	final static int DEFAULT_CONSTR_METHOD_NAME_AND_TYPE = 2;
	final static int APPEND_INT_METHOD_NAME_AND_TYPE = 3;
	final static int APPEND_FLOAT_METHOD_NAME_AND_TYPE = 4;
	final static int APPEND_LONG_METHOD_NAME_AND_TYPE = 5;
	final static int APPEND_OBJECT_METHOD_NAME_AND_TYPE = 6;
	final static int APPEND_CHAR_METHOD_NAME_AND_TYPE = 7;
	final static int APPEND_STRING_METHOD_NAME_AND_TYPE = 8;
	final static int APPEND_BOOLEAN_METHOD_NAME_AND_TYPE = 9;
	final static int APPEND_DOUBLE_METHOD_NAME_AND_TYPE = 10;
	final static int TOSTRING_METHOD_NAME_AND_TYPE = 11;
	final static int EXIT_METHOD_NAME_AND_TYPE = 12;
	final static int GETMESSAGE_METHOD_NAME_AND_TYPE = 13;
	final static int GETCONSTRUCTOR_METHOD_NAME_AND_TYPE = 14;
	final static int NEWINSTANCE_METHOD_NAME_AND_TYPE = 15;
	final static int INTERN_METHOD_NAME_AND_TYPE = 16;
	final static int VALUEOF_INT_METHOD_NAME_AND_TYPE = 17;
	final static int VALUEOF_FLOAT_METHOD_NAME_AND_TYPE = 18;
	final static int VALUEOF_LONG_METHOD_NAME_AND_TYPE = 19;
	final static int VALUEOF_OBJECT_METHOD_NAME_AND_TYPE = 20;
	final static int VALUEOF_CHAR_METHOD_NAME_AND_TYPE = 21;
	final static int VALUEOF_BOOLEAN_METHOD_NAME_AND_TYPE = 22;
	final static int VALUEOF_DOUBLE_METHOD_NAME_AND_TYPE = 23;
/**
 * ConstantPool constructor comment.
 */
public ConstantPool(ClassFile classFile) {
	UTF8Cache = new CharArrayCache(UTF8_INITIAL_SIZE);
	stringCache = new CharArrayCache(STRING_INITIAL_SIZE);
	fieldCache = new ObjectCache(FIELD_INITIAL_SIZE);
	methodCache = new ObjectCache(METHOD_INITIAL_SIZE);
	interfaceMethodCache = new ObjectCache(INTERFACE_INITIAL_SIZE);
	classCache = new ObjectCache(CLASS_INITIAL_SIZE);
	nameAndTypeCacheForMethods = new MethodNameAndTypeCache(NAMEANDTYPE_INITIAL_SIZE);
	nameAndTypeCacheForFields = new FieldNameAndTypeCache(NAMEANDTYPE_INITIAL_SIZE);	
	poolContent = classFile.header;
	currentOffset = classFile.headerOffset;
	// currentOffset is initialized to 0 by default
	currentIndex = 1;
}
/**
 * Return the content of the receiver
 */
public byte[] dumpBytes() {
	System.arraycopy(poolContent, 0, (poolContent = new byte[currentOffset]), 0, currentOffset);
	return poolContent;
}
/**
 * Return the index of the @fieldBinding.
 *
 * Returns -1 if the @fieldBinding is not a predefined fieldBinding, 
 * the right index otherwise.
 *
 * @param fieldBinding com.ibm.compiler.namelookup.FieldBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownFieldNameAndType(FieldBinding fieldBinding) {
	if ((fieldBinding.type.id == T_JavaLangClass) && (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE)))
		return TYPE_JAVALANGCLASS_NAME_AND_TYPE;
	if ((fieldBinding.type.id == T_JavaIoPrintStream) && (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.Out)))
		return OUT_SYSTEM_NAME_AND_TYPE;
	return -1;
}
/**
 * Return the index of the @fieldBinding.
 *
 * Returns -1 if the @fieldBinding is not a predefined fieldBinding, 
 * the right index otherwise.
 *
 * @param fieldBinding com.ibm.compiler.namelookup.FieldBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownFields(FieldBinding fieldBinding) {
	switch (fieldBinding.declaringClass.id) {
		case T_JavaLangByte :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_BYTE_FIELD;
			break;
		case T_JavaLangShort :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_SHORT_FIELD;
			break;
		case T_JavaLangCharacter :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_CHARACTER_FIELD;
			break;
		case T_JavaLangInteger :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_INTEGER_FIELD;
			break;
		case T_JavaLangLong :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_LONG_FIELD;
			break;
		case T_JavaLangFloat :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_FLOAT_FIELD;
			break;
		case T_JavaLangDouble :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_DOUBLE_FIELD;
			break;
		case T_JavaLangBoolean :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_BOOLEAN_FIELD;
			break;
		case T_JavaLangVoid :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.TYPE))
				return TYPE_VOID_FIELD;
			break;
		case T_JavaLangSystem :
			if (CharOperation.equals(fieldBinding.name, QualifiedNamesConstants.Out))
				return OUT_SYSTEM_FIELD;
	}
	return -1;
}
/**
 * Return the index of the @methodBinding.
 *
 * Returns -1 if the @methodBinding is not a predefined methodBinding, 
 * the right index otherwise.
 *
 * @param methodBinding com.ibm.compiler.namelookup.MethodBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownMethodNameAndType(MethodBinding methodBinding) {
	char firstChar = methodBinding.selector[0];
	switch (firstChar) {
		case 'f' :
			if ((methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_JavaLangString) && (methodBinding.returnType.id == T_JavaLangClass) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ForName))) {
				// This method binding is forName(java.lang.String)
				return FORNAME_CLASS_METHOD_NAME_AND_TYPE;
			}
			break;
		case '<' :
			if (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Init)) {
				if (CharOperation.equals(methodBinding.signature(), QualifiedNamesConstants.StringConstructorSignature)) {
					// This method binding is (java.lang.String)V
					return STRING_CONSTR_METHOD_NAME_AND_TYPE;
				} else
					if (CharOperation.equals(methodBinding.signature(), QualifiedNamesConstants.DefaultConstructorSignature)) {
						return DEFAULT_CONSTR_METHOD_NAME_AND_TYPE;
					}
			}
			break;
		case 'a' :
			if ((methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangStringBuffer) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Append))) {
				switch (methodBinding.parameters[0].id) {
					case T_int :
					case T_byte :
					case T_short :
						// This method binding is append(int)
						return APPEND_INT_METHOD_NAME_AND_TYPE;
					case T_float :
						// This method binding is append(float)
						return APPEND_FLOAT_METHOD_NAME_AND_TYPE;
					case T_long :
						// This method binding is append(long)
						return APPEND_LONG_METHOD_NAME_AND_TYPE;
					case T_JavaLangObject :
						// This method binding is append(java.lang.Object)
						return APPEND_OBJECT_METHOD_NAME_AND_TYPE;
					case T_char :
						// This method binding is append(char)
						return APPEND_CHAR_METHOD_NAME_AND_TYPE;
					case T_JavaLangString :
						// This method binding is append(java.lang.String)
						return APPEND_STRING_METHOD_NAME_AND_TYPE;
					case T_boolean :
						// This method binding is append(boolean)
						return APPEND_BOOLEAN_METHOD_NAME_AND_TYPE;
					case T_double :
						// This method binding is append(double)
						return APPEND_DOUBLE_METHOD_NAME_AND_TYPE;
				}
			}
			break;
		case 't' :
			if ((methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ToString))) {
				// This method binding is toString()
				return TOSTRING_METHOD_NAME_AND_TYPE;
			}
			break;
		case 'v' :
			if ((methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ValueOf))) {
				switch(methodBinding.parameters[0].id) {
					case T_Object:
						return VALUEOF_OBJECT_METHOD_NAME_AND_TYPE;
					case T_int:
					case T_short:
					case T_byte:
						return VALUEOF_INT_METHOD_NAME_AND_TYPE;
					case T_long:
						return VALUEOF_LONG_METHOD_NAME_AND_TYPE;
					case T_float:
						return VALUEOF_FLOAT_METHOD_NAME_AND_TYPE;
					case T_double:
						return VALUEOF_DOUBLE_METHOD_NAME_AND_TYPE;
					case T_boolean:
						return VALUEOF_BOOLEAN_METHOD_NAME_AND_TYPE;
					case T_char:
						return VALUEOF_CHAR_METHOD_NAME_AND_TYPE;
				}
			}
			break;
		case 'e' :
			if ((methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_int) && (methodBinding.returnType.id == T_void) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Exit))) {
				// This method binding is exit(int)
				return EXIT_METHOD_NAME_AND_TYPE;
			}
			break;
		case 'g' :
			if ((methodBinding.selector.length == 10) && (methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.GetMessage))) {
				// This method binding is getMessage()
				return GETMESSAGE_METHOD_NAME_AND_TYPE;
			}
			break;
		case 'i' :
			if ((methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Intern))) {
				// This method binding is toString()
				return INTERN_METHOD_NAME_AND_TYPE;
			}		
	}
	return -1;
}
/**
 * Return the index of the @methodBinding.
 *
 * Returns -1 if the @methodBinding is not a predefined methodBinding, 
 * the right index otherwise.
 *
 * @param methodBinding com.ibm.compiler.namelookup.MethodBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownMethods(MethodBinding methodBinding) {
	char firstChar = methodBinding.selector[0];
	switch (methodBinding.declaringClass.id) {
		case T_JavaLangClass :
			if ((firstChar == 'f') && (methodBinding.isStatic()) && (methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_JavaLangString) && (methodBinding.returnType.id == T_JavaLangClass) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ForName))) {
				// This method binding is forName(java.lang.String)
				return FORNAME_CLASS_METHOD;
			} else
				if ((firstChar == 'g') && (methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangReflectConstructor) && CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.GetConstructor) && CharOperation.equals(methodBinding.parameters[0].constantPoolName(), QualifiedNamesConstants.ArrayJavaLangClassConstantPoolName)) {
					return GETCONSTRUCTOR_CLASS_METHOD;
				}
			break;
		case T_JavaLangNoClassDefError :
			if ((firstChar == '<') && (methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Init))) {
				// This method binding is NoClassDefFoundError(java.lang.String)
				return NOCLASSDEFFOUNDERROR_CONSTR_METHOD;
			}
			break;
		case T_JavaLangReflectConstructor :
			if ((firstChar == 'n') && (methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangObject) && CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.NewInstance) && CharOperation.equals(methodBinding.parameters[0].constantPoolName(), QualifiedNamesConstants.ArrayJavaLangObjectConstantPoolName)) {
				return NEWINSTANCE_CONSTRUCTOR_METHOD;
			}
			break;
		case T_JavaLangStringBuffer :
			if ((firstChar == 'a') && (methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangStringBuffer) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Append))) {
				switch (methodBinding.parameters[0].id) {
					case T_int :
					case T_byte :
					case T_short :
						// This method binding is append(int)
						return APPEND_INT_METHOD;
					case T_float :
						// This method binding is append(float)
						return APPEND_FLOAT_METHOD;
					case T_long :
						// This method binding is append(long)
						return APPEND_LONG_METHOD;
					case T_JavaLangObject :
						// This method binding is append(java.lang.Object)
						return APPEND_OBJECT_METHOD;
					case T_char :
						// This method binding is append(char)
						return APPEND_CHAR_METHOD;
					case T_JavaLangString :
						// This method binding is append(java.lang.String)
						return APPEND_STRING_METHOD;
					case T_boolean :
						// This method binding is append(boolean)
						return APPEND_BOOLEAN_METHOD;
					case T_double :
						// This method binding is append(double)
						return APPEND_DOUBLE_METHOD;
				}
			} else
				if ((firstChar == 't') && (methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ToString))) {
					// This method binding is toString()
					return STRINGBUFFER_TOSTRING_METHOD;
				} else
					if ((firstChar == '<') && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Init))) {
						if ((methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_JavaLangString)) {
							// This method binding is <init>(String)					
							return STRINGBUFFER_STRING_CONSTR_METHOD;
						} else {
							if (methodBinding.parameters.length == 0) {
								// This method binding is <init>()
								return STRINGBUFFER_DEFAULT_CONSTR_METHOD;
							}
						}
					}
			break;
		case T_JavaLangString :
			if ((firstChar == 'v') && (methodBinding.parameters.length == 1) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.ValueOf))) {
				// This method binding is valueOf(java.lang.Object)
				switch (methodBinding.parameters[0].id) {
					case T_Object :
						return VALUEOF_OBJECT_METHOD;
					case T_int :
					case T_short :
					case T_byte :
						return VALUEOF_INT_METHOD;
					case T_long :
						return VALUEOF_LONG_METHOD;
					case T_float :
						return VALUEOF_FLOAT_METHOD;
					case T_double :
						return VALUEOF_DOUBLE_METHOD;
					case T_boolean :
						return VALUEOF_BOOLEAN_METHOD;
					case T_char :
						return VALUEOF_CHAR_METHOD;
				}
			} else
				if ((firstChar == 'i') && (methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Intern))) {
					// This method binding is valueOf(java.lang.Object)
					return STRING_INTERN_METHOD;
				}
			break;
		case T_JavaLangSystem :
			if ((firstChar == 'e') && (methodBinding.parameters.length == 1) && (methodBinding.parameters[0].id == T_int) && (methodBinding.returnType.id == T_void) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Exit))) {
				// This method binding is exit(int)
				return SYSTEM_EXIT_METHOD;
			}
			break;
		case T_JavaLangThrowable :
			if ((firstChar == 'g') && (methodBinding.selector.length == 10) && (methodBinding.parameters.length == 0) && (methodBinding.returnType.id == T_JavaLangString) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.GetMessage))) {
				// This method binding is getMessage()
				return THROWABLE_GETMESSAGE_METHOD;
			}
		case T_JavaLangError :
			if ((firstChar == '<') && (methodBinding.parameters.length == 1) && (CharOperation.equals(methodBinding.selector, QualifiedNamesConstants.Init)) && (methodBinding.parameters[0].id == T_String)) {
				return JAVALANGERROR_CONSTR_METHOD;
			}
	}
	return -1;
}
/**
 * Return the index of the @typeBinding
 *
 * Returns -1 if the @typeBinding is not a predefined binding, the right index 
 * otherwise.
 *
 * @param typeBinding com.ibm.compiler.namelookup.TypeBinding
 * @return <CODE>int</CODE>
 */
public int indexOfWellKnownTypes(TypeBinding typeBinding) {
	switch(typeBinding.id) {
		case T_JavaLangBoolean : return JAVA_LANG_BOOLEAN_TYPE;
		case T_JavaLangByte : return JAVA_LANG_BYTE_TYPE;
		case T_JavaLangCharacter : return JAVA_LANG_CHARACTER_TYPE;
		case T_JavaLangDouble : return JAVA_LANG_DOUBLE_TYPE;
		case T_JavaLangFloat : return JAVA_LANG_FLOAT_TYPE;
		case T_JavaLangInteger : return JAVA_LANG_INTEGER_TYPE;
		case T_JavaLangLong : return JAVA_LANG_LONG_TYPE;
		case T_JavaLangShort : return JAVA_LANG_SHORT_TYPE;
		case T_JavaLangVoid : return JAVA_LANG_VOID_TYPE;
		case T_JavaLangClass : return JAVA_LANG_CLASS_TYPE;
		case T_JavaLangClassNotFoundException : return JAVA_LANG_CLASSNOTFOUNDEXCEPTION_TYPE;
		case T_JavaLangNoClassDefError : return JAVA_LANG_NOCLASSDEFFOUNDERROR_TYPE;
		case T_JavaLangObject : return JAVA_LANG_OBJECT_TYPE;
		case T_JavaLangString : return JAVA_LANG_STRING_TYPE;
		case T_JavaLangStringBuffer : return JAVA_LANG_STRINGBUFFER_TYPE;
		case T_JavaLangSystem : return JAVA_LANG_SYSTEM_TYPE;
		case T_JavaLangThrowable : return JAVA_LANG_THROWABLE_TYPE;
		case T_JavaLangError : return JAVA_LANG_ERROR_TYPE;
		case T_JavaLangException : return JAVA_LANG_EXCEPTION_TYPE;
		case T_JavaLangReflectConstructor : return JAVA_LANG_REFLECT_CONSTRUCTOR_TYPE;
	}
	return -1;
}
public int literalIndex(byte[] utf8encoding, char[] stringCharArray) {
	int index;
	if ((index = UTF8Cache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		index = UTF8Cache.put(stringCharArray, currentIndex);
		currentIndex++;
		// Write the tag first
		writeU1(Utf8Tag);
		// Then the size of the stringName array
		//writeU2(utf8Constant.length);
		int savedCurrentOffset = currentOffset;
		if (currentOffset + 2 >= poolContent.length) {
			// we need to resize the poolContent array because we won't have
			// enough space to write the length
			int length = poolContent.length;
			System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
		}
		currentOffset += 2;
		// add in once the whole byte array
		int length = poolContent.length;
		int utf8encodingLength = utf8encoding.length;
		if (currentOffset + utf8encodingLength >= length) {
			System.arraycopy(poolContent, 0, (poolContent = new byte[length + utf8encodingLength + CONSTANTPOOL_GROW_SIZE]), 0, length);
		}
		System.arraycopy(utf8encoding, 0, poolContent, currentOffset, utf8encodingLength);
		currentOffset += utf8encodingLength;
		// Now we know the length that we have to write in the constant pool
		// we use savedCurrentOffset to do that
		poolContent[savedCurrentOffset] = (byte) (utf8encodingLength >> 8);
		poolContent[savedCurrentOffset + 1] = (byte) utf8encodingLength;
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param char[] stringName
 * @return <CODE>int</CODE>
 */
public int literalIndex(char[] utf8Constant) {
	int index;
	if ((index = UTF8Cache.get(utf8Constant)) < 0) {
		// The entry doesn't exit yet
		// Write the tag first
		writeU1(Utf8Tag);
		// Then the size of the stringName array
		int savedCurrentOffset = currentOffset;
		if (currentOffset + 2 >= poolContent.length) {
			// we need to resize the poolContent array because we won't have
			// enough space to write the length
			int length = poolContent.length;
			System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
		}
		currentOffset += 2;
		int length = 0;
		for (int i = 0; i < utf8Constant.length; i++) {
			char current = utf8Constant[i];
			if ((current >= 0x0001) && (current <= 0x007F)) {
				// we only need one byte: ASCII table
				writeU1(current);
				length++;
			} else
				if (current > 0x07FF) {
					// we need 3 bytes
					length += 3;
					writeU1(0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
					writeU1(0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
					writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				} else {
					// we can be 0 or between 0x0080 and 0x07FF
					// In that case we only need 2 bytes
					length += 2;
					writeU1(0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
					writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				}
		}
		if (length >= 65535) {
			currentOffset = savedCurrentOffset - 1;
			return -1;
		}
		index = UTF8Cache.put(utf8Constant, currentIndex);
		currentIndex++;		
		// Now we know the length that we have to write in the constant pool
		// we use savedCurrentOffset to do that
		poolContent[savedCurrentOffset] = (byte) (length >> 8);
		poolContent[savedCurrentOffset + 1] = (byte) length;
	}
	return index;
}
public int literalIndex(char[] stringCharArray, byte[] utf8encoding) {
	int index;
	int stringIndex;
	if ((index = stringCache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		stringIndex = literalIndex(utf8encoding, stringCharArray);
		index = stringCache.put(stringCharArray, currentIndex++);
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the double
 * value. If the double is not already present into the pool, it is added. The 
 * double cache is updated and it returns the right index.
 *
 * @param <CODE>double</CODE> key
 * @return <CODE>int</CODE>
 */
public int literalIndex(double key) {
	//Retrieve the index from the cache
	// The double constant takes two indexes into the constant pool, but we only store
	// the first index into the long table
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (doubleCache == null) {
			doubleCache = new DoubleCache(DOUBLE_INITIAL_SIZE);
	}
	if ((index = doubleCache.get(key)) < 0) {
		index = doubleCache.put(key, currentIndex++);
		currentIndex++; // a double needs an extra place into the constant pool
		// Write the double into the constant pool
		// First add the tag
		writeU1(DoubleTag);
		// Then add the 8 bytes representing the double
		long temp = java.lang.Double.doubleToLongBits(key);
		for (int i = 0; i < 8; i++) {
			try {
				poolContent[currentOffset++] = (byte) (temp >>> (56 - (i << 3)));
			} catch (IndexOutOfBoundsException e) { //currentOffset has been ++ already (see the -1)
				int length = poolContent.length;
				System.arraycopy(poolContent, 0, (poolContent = new byte[(length << 1) + CONSTANTPOOL_INITIAL_SIZE]), 0, length);
				poolContent[currentOffset - 1] = (byte) (temp >>> (56 - (i << 3)));
			}
		}
	};
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the float
 * value. If the float is not already present into the pool, it is added. The 
 * int cache is updated and it returns the right index.
 *
 * @param <CODE>float</CODE> key
 * @return <CODE>int</CODE>
 */
public int literalIndex(float key) {
	//Retrieve the index from the cache
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (floatCache == null) {
		floatCache = new FloatCache(FLOAT_INITIAL_SIZE);
	}
	if ((index = floatCache.get(key)) < 0) {
		index = floatCache.put(key, currentIndex++);
		// Write the float constant entry into the constant pool
		// First add the tag
		writeU1(FloatTag);
		// Then add the 4 bytes representing the float
		int temp = java.lang.Float.floatToIntBits(key);
		for (int i = 0; i < 4; i++) {
			try {
				poolContent[currentOffset++] = (byte) (temp >>> (24 - i * 8));
			} catch (IndexOutOfBoundsException e) { //currentOffset has been ++ already (see the -1)
				int length = poolContent.length;
				System.arraycopy(poolContent, 0, (poolContent = new byte[length * 2 + CONSTANTPOOL_INITIAL_SIZE]), 0, length);
				poolContent[currentOffset - 1] = (byte) (temp >>> (24 - i * 8));
			}
		}
	};
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the int
 * value. If the int is not already present into the pool, it is added. The 
 * int cache is updated and it returns the right index.
 *
 * @param <CODE>int</CODE> key
 * @return <CODE>int</CODE>
 */
public int literalIndex(int key) {
	//Retrieve the index from the cache
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (intCache == null) {
		intCache = new IntegerCache(INT_INITIAL_SIZE);
	}
	if ((index = intCache.get(key)) < 0) {
		index = intCache.put(key, currentIndex++);
		// Write the integer constant entry into the constant pool
		// First add the tag
		writeU1(IntegerTag);
		// Then add the 4 bytes representing the int
		for (int i = 0; i < 4; i++) {
			try {
				poolContent[currentOffset++] = (byte) (key >>> (24 - i * 8));
			} catch (IndexOutOfBoundsException e) { //currentOffset has been ++ already (see the -1)
				int length = poolContent.length;
				System.arraycopy(poolContent, 0, (poolContent = new byte[length * 2 + CONSTANTPOOL_INITIAL_SIZE]), 0, length);
				poolContent[currentOffset - 1] = (byte) (key >>> (24 - i * 8));
			}
		}
	};
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the long
 * value. If the long is not already present into the pool, it is added. The 
 * long cache is updated and it returns the right index.
 *
 * @param <CODE>long</CODE> key
 * @return <CODE>int</CODE>
 */
public int literalIndex(long key) {
	// Retrieve the index from the cache
	// The long constant takes two indexes into the constant pool, but we only store
	// the first index into the long table
	int index;
	// lazy initialization for base type caches
	// If it is null, initialize it, otherwise use it
	if (longCache == null) {
		longCache = new LongCache(LONG_INITIAL_SIZE);
	}
	if ((index = longCache.get(key)) < 0) {
		index = longCache.put(key, currentIndex++);
		currentIndex++; // long value need an extra place into thwe constant pool
		// Write the long into the constant pool
		// First add the tag
		writeU1(LongTag);
		// Then add the 8 bytes representing the long
		for (int i = 0; i < 8; i++) {
			try {
				poolContent[currentOffset++] = (byte) (key >>> (56 - (i << 3)));
			} catch (IndexOutOfBoundsException e) { //currentOffset has been ++ already (see the -1)
				int length = poolContent.length;
				System.arraycopy(poolContent, 0, (poolContent = new byte[(length << 1) + CONSTANTPOOL_INITIAL_SIZE]), 0, length);
				poolContent[currentOffset - 1] = (byte) (key >>> (56 - (i << 3)));
			}
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param stringConstant java.lang.String
 * @return <CODE>int</CODE>
 */
public int literalIndex(String stringConstant) {
	int index;
	char[] stringCharArray = stringConstant.toCharArray();
	if ((index = stringCache.get(stringCharArray)) < 0) {
		// The entry doesn't exit yet
		int stringIndex = literalIndex(stringCharArray);
		index = stringCache.put(stringCharArray, currentIndex++);
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @param FieldBinding aFieldBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(FieldBinding aFieldBinding) {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	int indexWellKnownField;
	if ((indexWellKnownField = indexOfWellKnownFields(aFieldBinding)) == -1) {
		if ((index = fieldCache.get(aFieldBinding)) < 0) {
			// The entry doesn't exit yet
			classIndex = literalIndex(aFieldBinding.declaringClass);
			nameAndTypeIndex = literalIndexForFields(literalIndex(aFieldBinding.name), literalIndex(aFieldBinding.type.signature()), aFieldBinding);
			index = fieldCache.put(aFieldBinding, currentIndex++);
			writeU1(FieldRefTag);
			writeU2(classIndex);
			writeU2(nameAndTypeIndex);
		}
	} else {
		if ((index = wellKnownFields[indexWellKnownField]) == 0) {
			// that field need to be inserted
			classIndex = literalIndex(aFieldBinding.declaringClass);
			nameAndTypeIndex = literalIndexForFields(literalIndex(aFieldBinding.name), literalIndex(aFieldBinding.type.signature()), aFieldBinding);
			index = wellKnownFields[indexWellKnownField] = currentIndex++;
			writeU1(FieldRefTag);
			writeU2(classIndex);
			writeU2(nameAndTypeIndex);
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @param MethodBinding aMethodBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(MethodBinding aMethodBinding) {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	int nameIndex;
	int indexWellKnownMethod;
	if ((indexWellKnownMethod = indexOfWellKnownMethods(aMethodBinding)) == -1) {
		if (aMethodBinding.declaringClass.isInterface()) {
			// Lookinf into the interface method ref table
			if ((index = interfaceMethodCache.get(aMethodBinding)) < 0) {
				classIndex = literalIndex(aMethodBinding.declaringClass);
				nameAndTypeIndex = literalIndexForMethods(literalIndex(aMethodBinding.constantPoolName()), literalIndex(aMethodBinding.signature()), aMethodBinding);
				index = interfaceMethodCache.put(aMethodBinding, currentIndex++);
				// Write the interface method ref constant into the constant pool
				// First add the tag
				writeU1(InterfaceMethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
		} else {
			// Lookinf into the method ref table
			if ((index = methodCache.get(aMethodBinding)) < 0) {
				classIndex = literalIndex(aMethodBinding.declaringClass);
				nameAndTypeIndex = literalIndexForMethods(literalIndex(aMethodBinding.constantPoolName()), literalIndex(aMethodBinding.signature()), aMethodBinding);
				index = methodCache.put(aMethodBinding, currentIndex++);
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
		}
	} else {
		// This is a well known method
		if ((index = wellKnownMethods[indexWellKnownMethod]) == 0) {
			// this methods was not inserted yet
			if (aMethodBinding.declaringClass.isInterface()) {
				// Lookinf into the interface method ref table
				classIndex = literalIndex(aMethodBinding.declaringClass);
				nameAndTypeIndex = literalIndexForMethods(literalIndex(aMethodBinding.constantPoolName()), literalIndex(aMethodBinding.signature()), aMethodBinding);
				index = wellKnownMethods[indexWellKnownMethod] = currentIndex++;
				// Write the interface method ref constant into the constant pool
				// First add the tag
				writeU1(InterfaceMethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			} else {
				// Lookinf into the method ref table
				classIndex = literalIndex(aMethodBinding.declaringClass);
				nameAndTypeIndex = literalIndexForMethods(literalIndex(aMethodBinding.constantPoolName()), literalIndex(aMethodBinding.signature()), aMethodBinding);
				index = wellKnownMethods[indexWellKnownMethod] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndex(TypeBinding aTypeBinding) {
	int index;
	int nameIndex;
	int indexWellKnownType;
	if ((indexWellKnownType = indexOfWellKnownTypes(aTypeBinding)) == -1) {
		if ((index = classCache.get(aTypeBinding)) < 0) {
			// The entry doesn't exit yet
			nameIndex = literalIndex(aTypeBinding.constantPoolName());
			index = classCache.put(aTypeBinding, currentIndex++);
			writeU1(ClassTag);
			// Then add the 8 bytes representing the long
			writeU2(nameIndex);
		}
	} else {
		if ((index = wellKnownTypes[indexWellKnownType]) == 0) {
			// Need to insert that binding
			nameIndex = literalIndex(aTypeBinding.constantPoolName());
			index = wellKnownTypes[indexWellKnownType] = currentIndex++;
			writeU1(ClassTag);
			// Then add the 8 bytes representing the long
			writeU2(nameIndex);
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeIndex.
 *
 * @param int nameIndex
 * @param int nameIndex
 * @param org.eclipse.jdt.internal.compiler.lookup.FieldBinding a FieldBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForFields(int nameIndex, int typeIndex, FieldBinding key) {
	int index;
	int indexOfWellKnownFieldNameAndType;
	if ((indexOfWellKnownFieldNameAndType = indexOfWellKnownFieldNameAndType(key)) == -1) {
		// check if the entry already exists
		if ((index = nameAndTypeCacheForFields.get(key)) == -1) {
			// The entry doesn't exit yet
			index = nameAndTypeCacheForFields.put(key, currentIndex++);
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
	} else {
		if ((index = wellKnownFieldNameAndTypes[indexOfWellKnownFieldNameAndType]) == 0) {
			index = wellKnownFieldNameAndTypes[indexOfWellKnownFieldNameAndType] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangBoolean() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_BOOLEAN_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangBooleanConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_BOOLEAN_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangBooleanTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_BOOLEAN_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangBoolean();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_BOOLEAN_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangByte() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_BYTE_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangByteConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_BYTE_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangByteTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_BYTE_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangByte();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_BYTE_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangCharacter() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_CHARACTER_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangCharacterConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_CHARACTER_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangCharacterTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_CHARACTER_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangCharacter();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_CHARACTER_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClass() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_CLASS_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangClassConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_CLASS_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassForName() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[FORNAME_CLASS_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangClass();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[FORNAME_CLASS_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.ForName);
			int typeIndex = literalIndex(QualifiedNamesConstants.ForNameSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[FORNAME_CLASS_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[FORNAME_CLASS_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassGetConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[GETCONSTRUCTOR_CLASS_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangClass();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[GETCONSTRUCTOR_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.GetConstructor);
			int typeIndex = literalIndex(QualifiedNamesConstants.GetConstructorSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[GETCONSTRUCTOR_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[GETCONSTRUCTOR_CLASS_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangClassNotFoundException() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_CLASSNOTFOUNDEXCEPTION_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangClassNotFoundExceptionConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_CLASSNOTFOUNDEXCEPTION_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangDouble() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_DOUBLE_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangDoubleConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_DOUBLE_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangDoubleTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_DOUBLE_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangDouble();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_DOUBLE_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangError() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_ERROR_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangErrorConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_ERROR_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangErrorConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[JAVALANGERROR_CONSTR_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangError();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Init);
			int typeIndex = literalIndex(QualifiedNamesConstants.StringConstructorSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[JAVALANGERROR_CONSTR_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
public int literalIndexForJavaLangException() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_EXCEPTION_TYPE]) == 0) {
		// The entry doesn't exit yet
		int nameIndex = literalIndex(QualifiedNamesConstants.JavaLangExceptionConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_EXCEPTION_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangFloat() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_FLOAT_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangFloatConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_FLOAT_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangFloatTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_FLOAT_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangFloat();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_FLOAT_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangInteger() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_INTEGER_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangIntegerConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_INTEGER_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangIntegerTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_INTEGER_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangInteger();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_INTEGER_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangLong() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_LONG_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangLongConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_LONG_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangLongTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_LONG_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangLong();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_LONG_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangNoClassDefFoundError() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_NOCLASSDEFFOUNDERROR_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangNoClassDefFoundErrorConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_NOCLASSDEFFOUNDERROR_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangNoClassDefFoundErrorStringConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[NOCLASSDEFFOUNDERROR_CONSTR_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangNoClassDefFoundError();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Init);
			int typeIndex = literalIndex(QualifiedNamesConstants.StringConstructorSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[NOCLASSDEFFOUNDERROR_CONSTR_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangObject() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_OBJECT_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangObjectConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_OBJECT_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangReflectConstructor() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_REFLECT_CONSTRUCTOR_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangReflectConstructor);
		index = wellKnownTypes[JAVA_LANG_REFLECT_CONSTRUCTOR_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
public int literalIndexForJavaLangReflectConstructorNewInstance() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[NEWINSTANCE_CONSTRUCTOR_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangReflectConstructor();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[NEWINSTANCE_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.NewInstance);
			int typeIndex = literalIndex(QualifiedNamesConstants.NewInstanceSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[NEWINSTANCE_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[NEWINSTANCE_CONSTRUCTOR_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangShort() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_SHORT_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangShortConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_SHORT_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangShortTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_SHORT_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangShort();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_SHORT_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangString() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_STRING_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangStringConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_STRING_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringBuffer() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_STRINGBUFFER_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangStringBufferConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_STRINGBUFFER_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringBufferAppend(int typeID) {
	int index = 0;
	int nameAndTypeIndex = 0;
	int classIndex = 0;
	switch (typeID) {
		case T_int :
		case T_byte :
		case T_short :
			if ((index = wellKnownMethods[APPEND_INT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_INT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendIntSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_INT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_INT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_long :
			if ((index = wellKnownMethods[APPEND_LONG_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_LONG_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendLongSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_LONG_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_LONG_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_float :
			if ((index = wellKnownMethods[APPEND_FLOAT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_FLOAT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendFloatSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_FLOAT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_FLOAT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_double :
			if ((index = wellKnownMethods[APPEND_DOUBLE_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_DOUBLE_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendDoubleSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_DOUBLE_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_DOUBLE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_char :
			if ((index = wellKnownMethods[APPEND_CHAR_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_CHAR_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendCharSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_CHAR_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_CHAR_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_boolean :
			if ((index = wellKnownMethods[APPEND_BOOLEAN_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_BOOLEAN_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendBooleanSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_BOOLEAN_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_BOOLEAN_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_Object :
			if ((index = wellKnownMethods[APPEND_OBJECT_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_OBJECT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendObjectSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_OBJECT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_OBJECT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_String :
		case T_null :
			if ((index = wellKnownMethods[APPEND_STRING_METHOD]) == 0) {
				classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_STRING_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Append);
					int typeIndex = literalIndex(QualifiedNamesConstants.AppendStringSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[APPEND_STRING_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[APPEND_STRING_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringBufferConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[STRINGBUFFER_STRING_CONSTR_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangStringBuffer();
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.Init);
					int typeIndex = literalIndex(QualifiedNamesConstants.StringConstructorSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[STRING_CONSTR_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
		index = wellKnownMethods[STRINGBUFFER_STRING_CONSTR_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringBufferDefaultConstructor() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[STRINGBUFFER_DEFAULT_CONSTR_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangStringBuffer();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[DEFAULT_CONSTR_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Init);
			int typeIndex = literalIndex(QualifiedNamesConstants.DefaultConstructorSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[DEFAULT_CONSTR_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[STRINGBUFFER_DEFAULT_CONSTR_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringBufferToString() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[STRINGBUFFER_TOSTRING_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangStringBuffer();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[TOSTRING_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.ToString);
			int typeIndex = literalIndex(QualifiedNamesConstants.ToStringSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[TOSTRING_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[STRINGBUFFER_TOSTRING_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringIntern() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[STRING_INTERN_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangString();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[INTERN_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Intern);
			int typeIndex = literalIndex(QualifiedNamesConstants.InternSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[INTERN_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[STRING_INTERN_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangStringValueOf(int typeID) {
	int index = 0;
	int nameAndTypeIndex = 0;
	int classIndex = literalIndexForJavaLangString();
	switch (typeID) {
		case T_int :
		case T_byte :
		case T_short :
			if ((index = wellKnownMethods[VALUEOF_INT_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_INT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfIntSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_INT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_INT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_long :
			if ((index = wellKnownMethods[VALUEOF_LONG_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_LONG_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfLongSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_LONG_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_LONG_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_float :
			if ((index = wellKnownMethods[VALUEOF_FLOAT_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_FLOAT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfFloatSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_FLOAT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_FLOAT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_double :
			if ((index = wellKnownMethods[VALUEOF_DOUBLE_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_DOUBLE_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfDoubleSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_DOUBLE_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_DOUBLE_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_char :
			if ((index = wellKnownMethods[VALUEOF_CHAR_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_CHAR_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfCharSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_CHAR_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_CHAR_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_boolean :
			if ((index = wellKnownMethods[VALUEOF_BOOLEAN_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_BOOLEAN_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfBooleanSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_BOOLEAN_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_BOOLEAN_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
		case T_Object :
			if ((index = wellKnownMethods[VALUEOF_OBJECT_METHOD]) == 0) {
				if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_OBJECT_METHOD_NAME_AND_TYPE]) == 0) {
					int nameIndex = literalIndex(QualifiedNamesConstants.ValueOf);
					int typeIndex = literalIndex(QualifiedNamesConstants.ValueOfObjectSignature);
					nameAndTypeIndex = wellKnownMethodNameAndTypes[VALUEOF_OBJECT_METHOD_NAME_AND_TYPE] = currentIndex++;
					writeU1(NameAndTypeTag);
					writeU2(nameIndex);
					writeU2(typeIndex);
				}
				index = wellKnownMethods[VALUEOF_OBJECT_METHOD] = currentIndex++;
				// Write the method ref constant into the constant pool
				// First add the tag
				writeU1(MethodRefTag);
				// Then write the class index
				writeU2(classIndex);
				// The write the nameAndType index
				writeU2(nameAndTypeIndex);
			}
			break;
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangSystem() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_SYSTEM_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangSystemConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_SYSTEM_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangSystemExitInt() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[SYSTEM_EXIT_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangSystem();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[EXIT_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Exit);
			int typeIndex = literalIndex(QualifiedNamesConstants.ExitIntSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[EXIT_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[SYSTEM_EXIT_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangSystemOut() {
	int index;
	if ((index = wellKnownFields[OUT_SYSTEM_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangSystem();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[OUT_SYSTEM_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.Out);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaIoPrintStreamSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[OUT_SYSTEM_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[OUT_SYSTEM_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangThrowable() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_THROWABLE_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangThrowableConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_THROWABLE_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the 
 * method descriptor. It can be either an interface method reference constant
 * or a method reference constant.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangThrowableGetMessage() {
	int index;
	int nameAndTypeIndex;
	int classIndex;
	// Looking into the method ref table
	if ((index = wellKnownMethods[THROWABLE_GETMESSAGE_METHOD]) == 0) {
		classIndex = literalIndexForJavaLangThrowable();
		if ((nameAndTypeIndex = wellKnownMethodNameAndTypes[GETMESSAGE_METHOD_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.GetMessage);
			int typeIndex = literalIndex(QualifiedNamesConstants.GetMessageSignature);
			nameAndTypeIndex = wellKnownMethodNameAndTypes[GETMESSAGE_METHOD_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownMethods[THROWABLE_GETMESSAGE_METHOD] = currentIndex++;
		// Write the method ref constant into the constant pool
		// First add the tag
		writeU1(MethodRefTag);
		// Then write the class index
		writeU2(classIndex);
		// The write the nameAndType index
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param TypeBinding aTypeBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangVoid() {
	int index;
	if ((index = wellKnownTypes[JAVA_LANG_VOID_TYPE]) == 0) {
		int nameIndex;
		// The entry doesn't exit yet
		nameIndex = literalIndex(QualifiedNamesConstants.JavaLangVoidConstantPoolName);
		index = wellKnownTypes[JAVA_LANG_VOID_TYPE] = currentIndex++;
		writeU1(ClassTag);
		// Then add the 8 bytes representing the long
		writeU2(nameIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool 
 * corresponding to the field binding aFieldBinding.
 *
 * @return <CODE>int</CODE>
 */
public int literalIndexForJavaLangVoidTYPE() {
	int index;
	if ((index = wellKnownFields[TYPE_VOID_FIELD]) == 0) {
		int nameAndTypeIndex;
		int classIndex;
		// The entry doesn't exit yet
		classIndex = literalIndexForJavaLangVoid();
		if ((nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE]) == 0) {
			int nameIndex = literalIndex(QualifiedNamesConstants.TYPE);
			int typeIndex = literalIndex(QualifiedNamesConstants.JavaLangClassSignature);
			nameAndTypeIndex = wellKnownFieldNameAndTypes[TYPE_JAVALANGCLASS_NAME_AND_TYPE] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
		index = wellKnownFields[TYPE_VOID_FIELD] = currentIndex++;
		writeU1(FieldRefTag);
		writeU2(classIndex);
		writeU2(nameAndTypeIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding to the type descriptor.
 *
 * @param char[] stringName
 * @return <CODE>int</CODE>
 */
public int literalIndexForLdc(char[] stringCharArray) {
	int index;
	if ((index = stringCache.get(stringCharArray)) < 0) {
		int stringIndex;
		// The entry doesn't exit yet
		if ((stringIndex = UTF8Cache.get(stringCharArray)) < 0) {
			// The entry doesn't exit yet
			// Write the tag first
			writeU1(Utf8Tag);
			// Then the size of the stringName array
			int savedCurrentOffset = currentOffset;
			if (currentOffset + 2 >= poolContent.length) {
				// we need to resize the poolContent array because we won't have
				// enough space to write the length
				int length = poolContent.length;
				System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
			}
			currentOffset += 2;
			int length = 0;
			for (int i = 0; i < stringCharArray.length; i++) {
				char current = stringCharArray[i];
				if ((current >= 0x0001) && (current <= 0x007F)) {
					// we only need one byte: ASCII table
					writeU1(current);
					length++;
				} else
					if (current > 0x07FF) {
						// we need 3 bytes
						length += 3;
						writeU1(0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
						writeU1(0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
						writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					} else {
						// we can be 0 or between 0x0080 and 0x07FF
						// In that case we only need 2 bytes
						length += 2;
						writeU1(0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
						writeU1(0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					}
			}
			if (length >= 65535) {
				currentOffset = savedCurrentOffset - 1;
				return -1;
			}
			stringIndex = UTF8Cache.put(stringCharArray, currentIndex++);
			// Now we know the length that we have to write in the constant pool
			// we use savedCurrentOffset to do that
			if (length > 65535) {
				return 0;
			}
			poolContent[savedCurrentOffset] = (byte) (length >> 8);
			poolContent[savedCurrentOffset + 1] = (byte) length;
		}
		index = stringCache.put(stringCharArray, currentIndex++);
		// Write the tag first
		writeU1(StringTag);
		// Then the string index
		writeU2(stringIndex);
	}
	return index;
}
/**
 * This method returns the index into the constantPool corresponding 
 * nameAndType constant with nameIndex, typeIndex.
 *
 * @param int nameIndex
 * @param int nameIndex
 * @param org.eclipse.jdt.internal.compiler.lookup.MethodBinding a methodBinding
 * @return <CODE>int</CODE>
 */
public int literalIndexForMethods(int nameIndex, int typeIndex, MethodBinding key) {
	int index;
	int indexOfWellKnownMethodNameAndType;
	if ((indexOfWellKnownMethodNameAndType = indexOfWellKnownMethodNameAndType(key)) == -1) {
		// check if the entry exists
		if ((index = nameAndTypeCacheForMethods.get(key)) == -1) {
			// The entry doesn't exit yet
			index = nameAndTypeCacheForMethods.put(key, currentIndex++);
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
	} else {
		if ((index = wellKnownMethodNameAndTypes[indexOfWellKnownMethodNameAndType]) == 0) {
			index = wellKnownMethodNameAndTypes[indexOfWellKnownMethodNameAndType] = currentIndex++;
			writeU1(NameAndTypeTag);
			writeU2(nameIndex);
			writeU2(typeIndex);
		}
	}
	return index;
}
/**
 * This method is used to clean the receiver in case of a clinit header is generated, but the 
 * clinit has no code.
 * This implementation assumes that the clinit is the first method to be generated.
 * @see org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.addClinit()
 */
public void resetForClinit(int constantPoolIndex, int constantPoolOffset) {
	currentIndex = constantPoolIndex;
	currentOffset = constantPoolOffset;
	if (UTF8Cache.get(AttributeNamesConstants.CodeName) >= constantPoolIndex) {
		UTF8Cache.remove(AttributeNamesConstants.CodeName);
	}
	if (UTF8Cache.get(QualifiedNamesConstants.ClinitSignature) >= constantPoolIndex) {
		UTF8Cache.remove(QualifiedNamesConstants.ClinitSignature);
	}
	if (UTF8Cache.get(QualifiedNamesConstants.Clinit) >= constantPoolIndex) {
		UTF8Cache.remove(QualifiedNamesConstants.Clinit);
	}
}
/**
 * Write a unsigned byte into the byte array
 * 
 * @param <CODE>int</CODE> The value to write into the byte array
 */
protected final void writeU1(int value) {
	try {
		poolContent[currentOffset++] = (byte) value;
	} catch (IndexOutOfBoundsException e) {
		//currentOffset has been ++ already (see the -1)
		int length = poolContent.length;
		System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
		poolContent[currentOffset - 1] = (byte) value;
	}
}
/**
 * Write a unsigned byte into the byte array
 * 
 * @param <CODE>int</CODE> The value to write into the byte array
 */
protected final void writeU2(int value) {
	//first byte
	try {
		poolContent[currentOffset++] = (byte) (value >> 8);
	} catch (IndexOutOfBoundsException e) {
		 //currentOffset has been ++ already (see the -1)
		int length = poolContent.length;
		System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
		poolContent[currentOffset - 1] = (byte) (value >> 8);
	}
	try {
		poolContent[currentOffset++] = (byte) value;
	} catch (IndexOutOfBoundsException e) {
		 //currentOffset has been ++ already (see the -1)
		int length = poolContent.length;
		System.arraycopy(poolContent, 0, (poolContent = new byte[length + CONSTANTPOOL_GROW_SIZE]), 0, length);
		poolContent[currentOffset - 1] = (byte) value;
	}
}
}
