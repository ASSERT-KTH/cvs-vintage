/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

public class BindingKeyParser {
	
	int keyStart;
	
	class Scanner {
		static final int ARRAY = 4;
		static final int END = 6;
		static final int FIELD = 2;
		static final int LOCAL_VAR = 5;
		static final int METHOD = 3;
		static final int PACKAGE = 0;
		
		static final int START = -1;
		static final int TYPE = 1;
		
		int index = 0, start;
		char[] source;
		int token = START;
	
		Scanner(char[] source) {
			this.source = source;
		}
		
		char[] getTokenSource() {
			int length = this.index-this.start;
			char[] result = new char[length];
			System.arraycopy(this.source, this.start, result, 0, length);
			return result;
		}
		
		boolean isAtFieldOrMethodStart() {
			return 
				this.index+1 < this.source.length
				&& this.source[this.index+1] == '.';
		}
		
		boolean isAtLocalVariableStart() {
			return 
				this.index < this.source.length
				&& this.source[this.index] == '#';
		}
		
		boolean isAtMemberTypeStart() {
			return 
				this.index+2 < this.source.length
				&& (this.source[this.index] == '$'
					|| this.source[this.index+2] == '$'
					|| (this.source[this.index] == '.' && this.source[this.index-1] == '>'));
		}
		
		boolean isAtParametersEnd() {
			return 
				this.index-1 > 0
				&& this.index+1 < this.source.length
				&& ((this.source[this.index] == ';' && this.source[this.index+1] == '>') 
					|| (this.source[this.index-1] == '*' && this.source[this.index] == '>'));
		}
		
		boolean isAtParametersStart() {
			char currentChar;
			return 
				this.index > 0
				&& this.index < this.source.length
				&& ((currentChar = this.source[this.index]) == '<'
					|| currentChar == '%');
		}
		
		boolean isAtRawTypeEnd() {
			return 
				this.index+1 > 0
				&& this.index+1 < this.source.length
				&& this.source[this.index+1] == '>';
		}
		
		boolean isAtSecondaryTypeStart() {
			return 
				this.index < this.source.length
				&& this.source[this.index] == '~';
		}
		
		boolean isAtTypeParameterStart() {
			return 
				this.index+1 < this.source.length
				&& this.source[this.index+1] == 'T';
		}
	
		boolean isAtTypeStart() {
			return this.index+1 < this.source.length && "LIZVCDBFJS[".indexOf(this.source[this.index+1]) != -1; //$NON-NLS-1$
		}
		
		boolean isAtTypeVariableStart() {
			return 
				this.index+3 < this.source.length
				&& this.source[this.index+3] == ':';
		}
		
		boolean isAtWildCardStart() {
			return this.index+1 < this.source.length && "*+-".indexOf(this.source[this.index+1]) != -1; //$NON-NLS-1$
		}
		
		int nextToken() {
			int previousTokenEnd = this.index;
			this.start = this.index;
			int length = this.source.length;
			while (this.index <= length) {
				char currentChar = this.index == length ? Character.MIN_VALUE : this.source[this.index];
				switch (currentChar) {
					case 'B':
					case 'C':
					case 'D':
					case 'F':
					case 'I':
					case 'J':
					case 'N':
					case 'S':
					case 'V':
					case 'Z':
						// base type
						if (this.index == previousTokenEnd) {
							this.index++;
							this.token = TYPE;
							return this.token;
						}
						break;
					case 'L':
					case 'T':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
						}
						break;
					case ';':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else {
							this.token = TYPE;
							return this.token;
						}
						break;
					case '$':
					case '~':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
						} else {
							this.token = TYPE;
							return this.token;
						}
						break;
					case '.':
					case '%':
					case ':':
					case '>':
						this.start = this.index+1;
						previousTokenEnd = this.start;
						break;
					case '[':
						while (this.index < length && this.source[this.index] == '[')
							this.index++;
						this.token = ARRAY;
						return this.token;
					case '<':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else if (this.start > 0) {
							switch (this.source[this.start-1]) {
								case '.':
									if (this.source[this.start-2] == '>')
										// case of member type where enclosing type is parameterized
										this.token = TYPE;
									else
										this.token = METHOD;
									return this.token;
								default:
									this.token = TYPE;
									return this.token;
							}
						} 
						break;
					case '(':
						this.token = METHOD;
						return this.token;
					case ')':
						this.start = ++this.index;
						this.token = END;
						return this.token;
					case '#':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							previousTokenEnd = this.start;
						} else {
							this.token = LOCAL_VAR;
							return this.token;
						}
						break;
					case Character.MIN_VALUE:
						switch (this.token) {
							case START:
								this.token = PACKAGE;
								break;
							case METHOD:
							case LOCAL_VAR:
								this.token = LOCAL_VAR;
								break;
							case TYPE:
								if (this.index > this.start && this.source[this.start-1] == '.')
									this.token = FIELD;
								else
									this.token = END;
								break;
							default:
								this.token = END;
								break;
						}
						return this.token;
					case '*':
					case '+':
					case '-':
						this.index++;
						this.token = TYPE;
						return this.token;
				}
				this.index++;
			}
			this.token = END;
			return this.token;
		}
		
		void skipMethodSignature() {
			this.start = this.index;
			char currentChar;
			while (this.index < this.source.length && (currentChar = this.source[this.index]) != '#' && currentChar != '%')
				this.index++;
		}
		
		void skipParametersEnd() {
			while (this.index < this.source.length && this.source[this.index] != '>')
				this.index++;
			this.index++;
		}
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			switch (this.token) {
				case START:
					buffer.append("START: "); //$NON-NLS-1$
					break;
				case PACKAGE:
					buffer.append("PACKAGE: "); //$NON-NLS-1$
					break;
				case TYPE:
					buffer.append("TYPE: "); //$NON-NLS-1$
					break;
				case FIELD:
					buffer.append("FIELD: "); //$NON-NLS-1$
					break;
				case METHOD:
					buffer.append("METHOD: "); //$NON-NLS-1$
					break;
				case ARRAY:
					buffer.append("ARRAY: "); //$NON-NLS-1$
					break;
				case LOCAL_VAR:
					buffer.append("LOCAL VAR: "); //$NON-NLS-1$
					break;
				case END:
					buffer.append("END: "); //$NON-NLS-1$
					break;
			}
			if (this.index < 0) {
				buffer.append("**"); //$NON-NLS-1$
				buffer.append(this.source);
			} else if (this.index <= this.source.length) {
				buffer.append(CharOperation.subarray(this.source, 0, this.start));
				buffer.append('*');
				if (this.start <= this.index) {
					buffer.append(CharOperation.subarray(this.source, this.start, this.index));
					buffer.append('*');
					buffer.append(CharOperation.subarray(this.source, this.index, this.source.length));
				} else {
					buffer.append('*');
					buffer.append(CharOperation.subarray(this.source, this.start, this.source.length));
				}
			} else {
				buffer.append(this.source);
				buffer.append("**"); //$NON-NLS-1$
			}
			return buffer.toString();
		}
	}
	private boolean parsingPaused;
	
	private Scanner scanner;
	
	public BindingKeyParser(BindingKeyParser parser) {
		this(""); //$NON-NLS-1$
		this.scanner = parser.scanner;
	}
	
	public BindingKeyParser(String key) {
		this.scanner = new Scanner(key.toCharArray());
	}
	
	public void consumeArrayDimension(char[] brakets) {
		// default is to do nothing
	}
	
	public void consumeField(char[] fieldName) {
		// default is to do nothing
	}
	
	public void consumeParameterizedMethod() {
		// default is to do nothing
	}
	
	public void consumeLocalType(char[] uniqueKey) {
		// default is to do nothing
	}
	
	public void consumeLocalVar(char[] varName) {
		// default is to do nothing
	}
	
	public void consumeMethod(char[] selector, char[] signature) {
		// default is to do nothing
	}
	
	public void consumeNonGenericType() {
		// default is to do nothing
	}

	public void consumeMemberType(char[] simpleTypeName) {
		// default is to do nothing
	}

	public void consumePackage(char[] pkgName) {
		// default is to do nothing
	}
	
	public void consumeParameterizedType(char[] simpleTypeName, boolean isRaw) {
		// default is to do nothing
	}
	
	public void consumeParser(BindingKeyParser parser) {
		// default is to do nothing
	}
	
	public void consumeRawType() {
		// default is to do nothing
	}
	
	public void consumeScope(int scopeNumber) {
		// default is to do nothing
	}
	
	public void consumeSecondaryType(char[] simpleTypeName) {
		// default is to do nothing
	}

	public void consumeFullyQualifiedName(char[] fullyQualifiedName) {
		// default is to do nothing
	}

	public void consumeTopLevelType() {
		// default is to do nothing
	}
	
	public void consumeType() {
		// default is to do nothing
	}
	
	public void consumeTypeParameter(char[] typeParameterName) {
		// default is to do nothing
	}
	
	public void consumeTypeVariable(char[] typeVariableName) {
		// default is to do nothing
	}
	
	public void consumeWildCard(int kind, int rank) {
		// default is to do nothing
	}
	
	/*
	 * Returns the string that this binding key wraps.
	 */
	public String getKey() {
		return new String(this.scanner.source);
	}
	
	public boolean isPackage() {
		return this.scanner.token == Scanner.PACKAGE;
	}
	
	public void malformedKey() {
		// default is to do nothing
	}
	
	public BindingKeyParser newParser() {
		return new BindingKeyParser(this);
	}
	
	public void parse() {
		parse(false/*don't pause after fully qualified name*/);
	}

	public void parse(boolean pauseAfterFullyQualifiedName) {
		if (!this.parsingPaused) {
			// fully qualified name
			parseFullyQualifiedName();
			if (pauseAfterFullyQualifiedName) {
				this.parsingPaused = true;
				return;
			}
		}
		if (isPackage())
			return;
		consumeTopLevelType();
		parseSecondaryType();
		parseInnerType();
		
		if (this.scanner.isAtParametersStart()) {
			if (this.scanner.isAtTypeParameterStart())	{		
				// generic type
				parseGenericType();
				// local type in generic type
				parseInnerType();
			} else if (this.scanner.isAtTypeStart() || this.scanner.isAtWildCardStart())
				// parameterized type
				parseParameterizedType(null/*top level type*/);
			else if (this.scanner.isAtRawTypeEnd())
				// raw type
				parseRawType();
		} else {
			// non-generic type
			consumeNonGenericType();
		}
		
		consumeType();
		
		if (this.scanner.isAtFieldOrMethodStart()) {
			switch (this.scanner.nextToken()) {
 				case Scanner.FIELD:
 					consumeField(this.scanner.getTokenSource());
 					return;
 				case Scanner.METHOD:
 					parseMethod();
 					if (this.scanner.isAtLocalVariableStart()) {
 						parseLocalVariable();
 					}
 					break;
 				default:
 					malformedKey();
 					return;
			}
		} else if (this.scanner.isAtTypeVariableStart()) {
			parseTypeVariable();
		}
	}
	
	private void parseFullyQualifiedName() {
		switch(this.scanner.nextToken()) {
			case Scanner.PACKAGE:
				this.keyStart = 0;
				consumePackage(this.scanner.getTokenSource());
				return;
			case Scanner.TYPE:
				this.keyStart = this.scanner.start-1;
				consumeFullyQualifiedName(this.scanner.getTokenSource());
				break;
	 		case Scanner.ARRAY:
	 			this.keyStart = this.scanner.start;
	 			consumeArrayDimension(this.scanner.getTokenSource());
				if (this.scanner.nextToken() == Scanner.TYPE)
	 				consumeFullyQualifiedName(this.scanner.getTokenSource());
				else {
					malformedKey();
					return;
				}
				break;
			default:
	 			malformedKey();
				return;
		}
	}
	
	private void parseParameterizedMethod() {
		while (!this.scanner.isAtParametersEnd()) {
			parseTypeArgument();
		}
		consumeParameterizedMethod();
	}
	
	private void parseGenericType() {
		while (!this.scanner.isAtParametersEnd()) {
			if (this.scanner.nextToken() != Scanner.TYPE) {
				malformedKey();
				return;
			}
			consumeTypeParameter(this.scanner.getTokenSource());
		}
	}
	
	private void parseInnerType() {
		if (!this.scanner.isAtMemberTypeStart() || this.scanner.nextToken() != Scanner.TYPE)
			return;
		char[] typeName = this.scanner.getTokenSource();
	 	if (Character.isDigit(typeName[0])) {
	 		// anonymous or local type
	 		int nextToken = Scanner.TYPE;
	 		while (this.scanner.isAtMemberTypeStart()) 
	 			nextToken = this.scanner.nextToken();
	 		typeName = nextToken == Scanner.END ? this.scanner.source : CharOperation.subarray(this.scanner.source, this.keyStart, this.scanner.index+1);
	 		consumeLocalType(typeName);
	 	} else {
			consumeMemberType(typeName);
			parseInnerType();
	 	}
	}
	
	private void parseLocalVariable() {
	 	if (this.scanner.nextToken() != Scanner.LOCAL_VAR) {
	 		malformedKey();
			return;
	 	}
		char[] varName = this.scanner.getTokenSource();
		if (Character.isDigit(varName[0])) {
			int index = Integer.parseInt(new String(varName));
			consumeScope(index);
			if (!this.scanner.isAtLocalVariableStart()) {
				malformedKey();
				return;
			}
			parseLocalVariable();
		} else {
		 	consumeLocalVar(varName);
		}
 	}
	
	private void parseMethod() {
	 	char[] selector = this.scanner.getTokenSource();
	 	this.scanner.skipMethodSignature();
	 	char[] signature = this.scanner.getTokenSource();
	 	consumeMethod(selector, signature);
		if (this.scanner.isAtParametersStart())
			parseParameterizedMethod();
	}
	
	private void parseParameterizedType(char[] typeName) {
		boolean isRaw = false;
		if (this.scanner.isAtParametersStart()) {
			if (!this.scanner.isAtRawTypeEnd()) {
				int rank = 0;
				while (!this.scanner.isAtParametersEnd()) {
			 		if (this.scanner.isAtWildCardStart()) {
			 			parseWildCard(rank++);
			 		} else {
						parseTypeArgument();
			 		}
				}
			}  else
				isRaw = true;
		 	// skip ";>"
		 	this.scanner.skipParametersEnd();
		}
		consumeParameterizedType(typeName, isRaw);
	 	if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == Scanner.TYPE) {
	 		typeName = this.scanner.getTokenSource();
	 		parseParameterizedType(typeName);
	 	}
	}
	
	private void parseRawType() {
		this.scanner.skipParametersEnd();
		consumeRawType();
	}
	
	private void parseSecondaryType() {
		if (!this.scanner.isAtSecondaryTypeStart() || this.scanner.nextToken() != Scanner.TYPE) return;
		consumeSecondaryType(this.scanner.getTokenSource());
	}
	
	private void parseTypeArgument() {
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
	}
	
	private void parseTypeVariable() {
	 	// skip ";>"
	 	this.scanner.skipParametersEnd();
		if (this.scanner.nextToken() != Scanner.TYPE) {
			malformedKey();
			return;
		}
		consumeTypeVariable(this.scanner.getTokenSource());
	}
	
	private void parseWildCard(int rank) {
	 	if (this.scanner.nextToken() != Scanner.TYPE) {
	 		malformedKey();
	 		return;
	 	}
	 	char[] source = this.scanner.getTokenSource();
	 	if (source.length == 0) {
	 		malformedKey();
	 		return;
	 	}
	 	int kind = -1;
	 	switch (source[0]) {
		 	case '*':
		 		kind = Wildcard.UNBOUND;
		 		break;
		 	case '+':
		 		kind = Wildcard.EXTENDS;
		 		parseTypeArgument();
		 		break;
		 	case '-':
		 		kind = Wildcard.SUPER;
		 		parseTypeArgument();
		 		break;
	 	}
	 	if (kind == -1) {
	 		malformedKey();
	 		return;
	 	}
	 	consumeWildCard(kind, rank);
	 }
	
}
