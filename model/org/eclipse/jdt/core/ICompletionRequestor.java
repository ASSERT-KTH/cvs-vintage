package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A completion requestor accepts results as they are computed and is aware
 * of source positions to complete the various different results.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see ICodeAssist
 * @since 2.0
 */
public interface ICompletionRequestor {
/**
 * Code assist notification of an anonynous type declaration completion.
 * @param superTypePackageName char[]
 * 		Name of the package that contains the super type of thw new anonynous type declaration .
 * 
 * @param superTypeName char[]
 * 		Name of the super type of this new anonynous type declaration.
 * 
 * @param parameterPackageNames char[][]
 * 		Names of the packages in which the parameter types are declared.
 *    	Should contain as many elements as parameterTypeNames.
 * 
 * @param parameterTypeNames char[][]
 * 		Names of the parameters types.
 *    	Should contain as many elements as parameterPackageNames.
 * 
 * @param completionName char[]
 * 		The completion for the anonynous type declaration.
 * 		Can include zero, one or two brackets. If the closing bracket is included,
 * 		then the cursor should be placed before it.
 * 
 * @param modifiers int
 * 		The modifiers of the constructor.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this new anonynous type declaration.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this new anonynous type declaration.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 * 
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 *
 * NOTE: parameter names can be retrieved from the source model after the user selects a specific method.
 */
void acceptAnonymousType(
	char[] superTypePackageName,
	char[] superTypeName,
	char[][] parameterPackageNames,
	char[][] parameterTypeNames,
	char[][] parameterNames,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a class completion.
 * @param packageName char[]
 * 		Declaring package name of the class.
 * 
 * @param className char[]
 *		Name of the class.
 * 
 * @param completionName char[]
 *		The completion for the class.
 *   	Can include ';' for imported classes.
 * 
 * @param modifiers int
 *		The modifiers of the class.
 * 
 * @param completionStart int
 *		The start position of insertion of the name of the class.
 * 
 * @param completionEnd int
 *		The end position of insertion of the name of the class.
 * 
 *  @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptClass(
	char[] packageName,
	char[] className,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a compilation error detected during completion.
 *  @param error org.eclipse.jdt.core.compiler.IProblem
 *      Only problems which are categorized as non-sytax errors are notified to the 
 *     requestor, warnings are silently ignored.
 *		In case an error got signaled, no other completions might be available,
 *		therefore the problem message should be presented to the user.
 *		The source positions of the problem are related to the source where it was
 *		detected (might be in another compilation unit, if it was indirectly requested
 *		during the code assist process).
 *      Note: the problem knows its originating file name.
 */
void acceptError(IProblem error);
/**
 * Code assist notification of a field completion.
 * @param declaringTypePackageName char[]
 * 		Name of the package in which the type that contains this field is declared.
 * 
 * @param declaringTypeName char[]
 * 		Name of the type declaring this new field.
 * 
 * @param name char[]
 * 		Name of the field.
 * 
 * @param typePackageName char[]
 * 		Name of the package in which the type of this field is declared.
 * 
 * @param typeName char[]
 * 		Name of the type of this field.
 * 
 * @param completionName char[]
 * 		The completion for the field.
 * 
 * @param modifiers int
 * 		The modifiers of this field.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this field.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this field.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 * 
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptField(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] name,
	char[] typePackageName,
	char[] typeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of an interface completion.
 * @param packageName char[]
 * 		Declaring package name of the interface.
 * 
 * @param className char[]
 * 		Name of the interface.
 * 
 * @param completionName char[]
 * 		The completion for the interface.
 *   	Can include ';' for imported interfaces.
 * 
 * @param modifiers int
 * 		The modifiers of the interface.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of the interface.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of the interface.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptInterface(
	char[] packageName,
	char[] interfaceName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a keyword completion.
 * @param keywordName char[]
 * 		The keyword source.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this keyword.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this keyword.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 */
void acceptKeyword(char[] keywordName, int completionStart, int completionEnd, int relevance);
/**
 * Code assist notification of a label completion.
 * @param labelName char[]
 * 		The label source.
 * 
 * @param completionStart int
 *		The start position of insertion of the name of this label.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this label.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 */
void acceptLabel(char[] labelName, int completionStart, int completionEnd, int relevance);
/**
 * Code assist notification of a local variable completion.
 * @param name char[]
 *		Name of the new local variable.
 * 
 * @param typePackageName char[]
 * 		Name of the package in which the type of this new local variable is declared.
 * 
 * @param typeName char[]
 * 		Name of the type of this new local variable.
 * 
 * @param modifiers int
 * 		The modifiers of this new local variable.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this new local variable.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this new local variable.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptLocalVariable(
	char[] name,
	char[] typePackageName,
	char[] typeName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a method completion.
 * @param declaringTypePackageName char[]
 * 		Name of the package in which the type that contains this new method is declared.
 * 
 * @param declaringTypeName char[]
 * 		Name of the type declaring this new method.
 * 
 * @param selector char[]
 * 		Name of the new method.
 * 
 * @param parameterPackageNames char[][]
 * 		Names of the packages in which the parameter types are declared.
 *    	Should contain as many elements as parameterTypeNames.
 * 
 * @param parameterTypeNames char[][]
 * 		Names of the parameters types.
 *    	Should contain as many elements as parameterPackageNames.
 * 
 * @param returnTypePackageName char[]
 * 		Name of the package in which the return type is declared.
 * 
 * @param returnTypeName char[]
 * 		Name of the return type of this new method, should be <code>null</code> for a constructor.
 * 
 * @param completionName char[]
 * 		The completion for the method.
 *   	Can include zero, one or two brackets. If the closing bracket is included, then the cursor should be placed before it.
 * 
 * @param modifiers int
 * 		The modifiers of this new method.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this new method.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this new method.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 *
 * NOTE: parameter names can be retrieved from the source model after the user selects a specific method.
 */
void acceptMethod(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] selector,
	char[][] parameterPackageNames,
	char[][] parameterTypeNames,
	char[][] parameterNames,
	char[] returnTypePackageName,
	char[] returnTypeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
	
void acceptMethodDeclaration(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] selector,
	char[][] parameterPackageNames,
	char[][] parameterTypeNames,
	char[][] parameterNames,
	char[] returnTypePackageName,
	char[] returnTypeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a modifier completion.
 * @param modifierName char[]
 * 		The new modifier.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this new modifier.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this new modifier.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 */
void acceptModifier(char[] modifierName, int completionStart, int completionEnd, int relevance);
/**
 * Code assist notification of a package completion.
 * @param packageName char[]
 * 		The package name.
 * @param completionName char[]
 * 		The completion for the package.
 *   	Can include '.*;' for imports.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this new package.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this new package.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    The default package is represented by an empty array.
 */
void acceptPackage(
	char[] packageName,
	char[] completionName,
	int completionStart,
	int completionEnd,
	int relevance);
/**
 * Code assist notification of a type completion.
 * @param packageName char[]
 * 		Declaring package name of the type.
 * 
 * @param typeName char[]
 * 		Name of the type.
 * 
 * @param completionName char[]
 * 		The completion for the type.
 *   	Can include ';' for imported types.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of the type.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of the type.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptType(
	char[] packageName,
	char[] typeName,
	char[] completionName,
	int completionStart,
	int completionEnd,
	int relevance);
	
/**
 * Code assist notification of a variable name completion.
 * @param typePackageName char[]
 * 		Name of the package in which the type of this variable is declared.
 * 
 * @param typeName char[]
 * 		Name of the type of this variable.
 * 
 * @param name char[]
 * 		Name of the variable.
 * 
 * @param completionName char[]
 * 		The completion for the variable.
 * 
 * @param completionStart int
 * 		The start position of insertion of the name of this variable.
 * 
 * @param completionEnd int
 * 		The end position of insertion of the name of this variable.
 * 
 * @param relevance int
 * 		The relevance of the completion proposal
 * 		It is a positive integer which are used for determine if this proposal is more relevant than another proposal.
 * 		This value can only be used for compare relevance. A proposal is more relevant than another if his relevance
 * 		value is higher.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptVariableName(
	char[] typePackageName,
	char[] typeName,
	char[] name,
	char[] completionName,
	int completionStart,
	int completionEnd,
	int relevance);
}
