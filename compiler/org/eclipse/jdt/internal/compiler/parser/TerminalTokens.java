/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation. 
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API. 
 * The mirror implementation is using the backward compatible ITerminalSymbols constant 
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens 
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer. 
 * This integer is used to represent the terminal when computing a parsing action. 
 * 
 * Disclaimer : These constant values are generated automatically using a Java 
 * grammar, therefore their actual values are subject to change if new keywords 
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;

	int TokenNameIdentifier = 26,
		TokenNameabstract = 56,
		TokenNameassert = 74,
		TokenNameboolean = 32,
		TokenNamebreak = 75,
		TokenNamebyte = 33,
		TokenNamecase = 100,
		TokenNamecatch = 101,
		TokenNamechar = 34,
		TokenNameclass = 72,
		TokenNamecontinue = 76,
		TokenNameconst = 108,
		TokenNamedefault = 96,
		TokenNamedo = 77,
		TokenNamedouble = 35,
		TokenNameelse = 102,
		TokenNameenum = 103,
		TokenNameextends = 97,
		TokenNamefalse = 45,
		TokenNamefinal = 57,
		TokenNamefinally = 104,
		TokenNamefloat = 36,
		TokenNamefor = 78,
		TokenNamegoto = 109,
		TokenNameif = 79,
		TokenNameimplements = 106,
		TokenNameimport = 99,
		TokenNameinstanceof = 15,
		TokenNameint = 37,
		TokenNameinterface = 80,
		TokenNamelong = 38,
		TokenNamenative = 58,
		TokenNamenew = 43,
		TokenNamenull = 46,
		TokenNamepackage = 98,
		TokenNameprivate = 59,
		TokenNameprotected = 60,
		TokenNamepublic = 61,
		TokenNamereturn = 81,
		TokenNameshort = 39,
		TokenNamestatic = 54,
		TokenNamestrictfp = 62,
		TokenNamesuper = 41,
		TokenNameswitch = 82,
		TokenNamesynchronized = 55,
		TokenNamethis = 42,
		TokenNamethrow = 83,
		TokenNamethrows = 105,
		TokenNametransient = 63,
		TokenNametrue = 47,
		TokenNametry = 84,
		TokenNamevoid = 40,
		TokenNamevolatile = 64,
		TokenNamewhile = 73,
		TokenNameIntegerLiteral = 48,
		TokenNameLongLiteral = 49,
		TokenNameFloatingPointLiteral = 50,
		TokenNameDoubleLiteral = 51,
		TokenNameCharacterLiteral = 52,
		TokenNameStringLiteral = 53,
		TokenNamePLUS_PLUS = 10,
		TokenNameMINUS_MINUS = 11,
		TokenNameEQUAL_EQUAL = 18,
		TokenNameLESS_EQUAL = 16,
		TokenNameGREATER_EQUAL = 17,
		TokenNameNOT_EQUAL = 19,
		TokenNameLEFT_SHIFT = 13,
		TokenNameRIGHT_SHIFT = 8,
		TokenNameUNSIGNED_RIGHT_SHIFT = 9,
		TokenNamePLUS_EQUAL = 85,
		TokenNameMINUS_EQUAL = 86,
		TokenNameMULTIPLY_EQUAL = 87,
		TokenNameDIVIDE_EQUAL = 88,
		TokenNameAND_EQUAL = 89,
		TokenNameOR_EQUAL = 90,
		TokenNameXOR_EQUAL = 91,
		TokenNameREMAINDER_EQUAL = 92,
		TokenNameLEFT_SHIFT_EQUAL = 93,
		TokenNameRIGHT_SHIFT_EQUAL = 94,
		TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 95,
		TokenNameOR_OR = 25,
		TokenNameAND_AND = 24,
		TokenNamePLUS = 1,
		TokenNameMINUS = 2,
		TokenNameNOT = 67,
		TokenNameREMAINDER = 5,
		TokenNameXOR = 21,
		TokenNameAND = 20,
		TokenNameMULTIPLY = 4,
		TokenNameOR = 22,
		TokenNameTWIDDLE = 68,
		TokenNameDIVIDE = 6,
		TokenNameGREATER = 12,
		TokenNameLESS = 7,
		TokenNameLPAREN = 28,
		TokenNameRPAREN = 29,
		TokenNameLBRACE = 69,
		TokenNameRBRACE = 31,
		TokenNameLBRACKET = 14,
		TokenNameRBRACKET = 70,
		TokenNameSEMICOLON = 27,
		TokenNameQUESTION = 23,
		TokenNameCOLON = 65,
		TokenNameCOMMA = 30,
		TokenNameDOT = 3,
		TokenNameEQUAL = 71,
		TokenNameAT = 44,
		TokenNameELLIPSIS = 107,
		TokenNameEOF = 66,
		TokenNameERROR = 110;
}
