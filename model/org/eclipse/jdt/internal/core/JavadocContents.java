/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToIntArray;
import org.eclipse.jdt.internal.core.util.Util;

public class JavadocContents {
	private static final int[] UNKNOWN_FORMAT = new int[0]; 
	
	private BinaryType type;
	private String content;
	
	private int childrenStart;
	
	private boolean hasComputedChildrenSections = false;
	private int indexOfFieldDetails;
	private int indexOfConstructorDetails;
	private int indexOfMethodDetails;
	private int indexOfEndOfClassData;
	
	private int indexOfFieldsBottom;
	private int indexOfAllMethodsTop;
	private int indexOfAllMethodsBottom;
	
	private int[] typeDocRange;
	private HashtableOfObjectToIntArray fieldDocRanges;
	private HashtableOfObjectToIntArray methodDocRanges;
	
	private int[] fieldAnchorIndexes;
	private int fieldAnchorIndexesCount;
	private int fieldLastAnchorFoundIndex;
	private int[] methodAnchorIndexes;
	private int methodAnchorIndexesCount;
	private int methodLastAnchorFoundIndex;
	private int[] unknownFormatAnchorIndexes;
	private int unknownFormatAnchorIndexesCount;
	private int unknownFormatLastAnchorFoundIndex;
	private int[] tempAnchorIndexes;
	private int tempAnchorIndexesCount;
	private int tempLastAnchorFoundIndex;
	
	public JavadocContents(BinaryType type, String content) {
		this.type = type;
		this.content = content;
	}
	
	/*
	 * Return the full content of the javadoc
	 */
	public String getContent() {
		return this.content;
	}
	
	/*
	 * Returns the part of the javadoc that describe the type
	 */
	public String getTypeDoc() throws JavaModelException {
		if (this.content == null) return null;
		
		synchronized (this) {
			if (this.typeDocRange == null) {
				computeTypeRange();
			}
		}
		
		if (this.typeDocRange != null) {
			if (this.typeDocRange == UNKNOWN_FORMAT) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNKNOWN_JAVADOC_FORMAT, this.type));
			return this.content.substring(this.typeDocRange[0], this.typeDocRange[1]);
		}
		return null;
	}
	
	/*
	 * Returns the part of the javadoc that describe a field of the type
	 */
	public String getFieldDoc(IField child) throws JavaModelException {
		if (this.content == null) return null;
		
		int[] range = null;
		synchronized (this) {
			if (this.fieldDocRanges == null) {
				this.fieldDocRanges = new HashtableOfObjectToIntArray();
			} else {
				range = this.fieldDocRanges.get(child);
			}
			
			if (range == null) {
				range = computeFieldRange(child);
				this.fieldDocRanges.put(child, range);
			}
		}
		
		if (range != null) {
			if (range == UNKNOWN_FORMAT) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNKNOWN_JAVADOC_FORMAT, child));
			return this.content.substring(range[0], range[1]);
		}
		return null;
	}
	
	/*
	 * Returns the part of the javadoc that describe a method of the type
	 */
	public String getMethodDoc(IMethod child) throws JavaModelException {
		if (this.content == null) return null;
		
		int[] range = null;
		synchronized (this) {
			if (this.methodDocRanges == null) {
				this.methodDocRanges = new HashtableOfObjectToIntArray();
			} else {
				range = this.methodDocRanges.get(child);
			}
			
			if (range == null) {
				range = computeMethodRange(child);
				this.methodDocRanges.put(child, range);
			}
		}
		
		if (range != null) {
			if (range == UNKNOWN_FORMAT) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNKNOWN_JAVADOC_FORMAT, child));
			return this.content.substring(range[0], range[1]);
		}
		return null;
	}
	
	/*
	 * Compute the ranges of the parts of the javadoc that describe each method of the type
	 */
	private int[] computeChildRange(String anchor, int indexOfSectionBottom) throws JavaModelException {
		
		// checks each known anchor locations
		if (this.tempAnchorIndexesCount > 0) {
			for (int i = 0; i < this.tempAnchorIndexesCount; i++) {
				int anchorEndStart = this.tempAnchorIndexes[i];
				
				if (anchorEndStart != -1 && this.content.startsWith(anchor, anchorEndStart)) {
					
					this.tempAnchorIndexes[i] = -1;
					
					return computeChildRange(anchorEndStart, anchor, indexOfSectionBottom);
				}
			}
		}
		
		int fromIndex = this.tempLastAnchorFoundIndex;
		int index;
		
		// check each next unknown anchor locations
		while ((index = this.content.indexOf(JavadocConstants.ANCHOR_PREFIX_START, fromIndex)) != -1 && (index < indexOfSectionBottom || indexOfSectionBottom == -1)) {
			fromIndex = index + 1;
			
			int anchorEndStart = index + JavadocConstants.ANCHOR_PREFIX_START_LENGHT;
			
			this.tempLastAnchorFoundIndex = anchorEndStart;
			
			if (this.content.startsWith(anchor, anchorEndStart)) {
				
				return computeChildRange(anchorEndStart, anchor, indexOfSectionBottom);
			} else {
				if (this.tempAnchorIndexes.length == this.tempAnchorIndexesCount) {
					System.arraycopy(this.tempAnchorIndexes, 0, this.tempAnchorIndexes = new int[this.tempAnchorIndexesCount + 20], 0, this.tempAnchorIndexesCount);
				}
				
				this.tempAnchorIndexes[this.tempAnchorIndexesCount++] = anchorEndStart;
			}
		}
		
		return null;
	}
	
	private int[] computeChildRange(int anchorEndStart, String anchor, int indexOfBottom) {
		int[] range = null;
				
		// try to find the bottom of the section
		if (indexOfBottom != -1) {
			// try to find the end of the anchor
			int indexOfEndLink = this.content.indexOf(JavadocConstants.ANCHOR_SUFFIX, anchorEndStart + anchor.length());
			if (indexOfEndLink != -1) {
				// try to find the next anchor
				int indexOfNextElement = this.content.indexOf(JavadocConstants.ANCHOR_PREFIX_START, indexOfEndLink);
				
				int javadocStart = indexOfEndLink + JavadocConstants.ANCHOR_SUFFIX_LENGTH;
				int javadocEnd = indexOfNextElement == -1 ? indexOfBottom : Math.min(indexOfNextElement, indexOfBottom);
				range = new int[]{javadocStart, javadocEnd};
			} else {
				// the anchor has no suffix
				range = UNKNOWN_FORMAT;
			}
		} else {
			// the detail section has no bottom
			range = UNKNOWN_FORMAT;
		}
		
		return range;
	}

	private void computeChildrenSections() {
		// try to find the next separator part
		int lastIndex = this.content.indexOf(JavadocConstants.SEPARATOR_START, this.childrenStart);
		
		// try to find field detail start
		this.indexOfFieldDetails = this.content.indexOf(JavadocConstants.FIELD_DETAIL, lastIndex);
		lastIndex = this.indexOfFieldDetails == -1 ? lastIndex : this.indexOfFieldDetails;
		
		// try to find constructor detail start
		this.indexOfConstructorDetails = this.content.indexOf(JavadocConstants.CONSTRUCTOR_DETAIL, lastIndex);
		lastIndex = this.indexOfConstructorDetails == -1 ? lastIndex : this.indexOfConstructorDetails;
		
		// try to find method detail start
		this.indexOfMethodDetails = this.content.indexOf(JavadocConstants.METHOD_DETAIL, lastIndex);
		lastIndex = this.indexOfMethodDetails == -1 ? lastIndex : this.indexOfMethodDetails;
		
		// we take the end of class data
		this.indexOfEndOfClassData = this.content.indexOf(JavadocConstants.END_OF_CLASS_DATA, lastIndex);
		
		// try to find the field detail end
		this.indexOfFieldsBottom =
			this.indexOfConstructorDetails != -1 ? this.indexOfConstructorDetails :
				this.indexOfMethodDetails != -1 ? this.indexOfMethodDetails:
					this.indexOfEndOfClassData;
		
		this.indexOfAllMethodsTop =
			this.indexOfConstructorDetails != -1 ?
					this.indexOfConstructorDetails :
						this.indexOfMethodDetails;
		
		this.indexOfAllMethodsBottom = this.indexOfEndOfClassData;
	
		this.hasComputedChildrenSections = true;
	}

	/*
	 * Compute the ranges of the parts of the javadoc that describe each child of the type (fields, methods)
	 */
	private int[] computeFieldRange(IField field) throws JavaModelException {
		if (!this.hasComputedChildrenSections) {
			computeChildrenSections();
		}
		
		String anchor = field.getElementName() + JavadocConstants.ANCHOR_PREFIX_END;
		
		int[] range = null;
		
		if (this.indexOfFieldDetails == -1 || this.indexOfFieldsBottom == -1) {
			// the detail section has no top or bottom, so the doc has an unknown format
			if (this.unknownFormatAnchorIndexes == null) {
				this.unknownFormatAnchorIndexes = new int[this.type.getChildren().length];
				this.unknownFormatAnchorIndexesCount = 0;
				this.unknownFormatLastAnchorFoundIndex = this.childrenStart;
			}
			
			this.tempAnchorIndexes = this.unknownFormatAnchorIndexes;
			this.tempAnchorIndexesCount = this.unknownFormatAnchorIndexesCount;
			this.tempLastAnchorFoundIndex = this.unknownFormatLastAnchorFoundIndex;
			
			range = computeChildRange(anchor, this.indexOfFieldsBottom);
			
			this.unknownFormatLastAnchorFoundIndex = this.tempLastAnchorFoundIndex;
			this.unknownFormatAnchorIndexesCount = this.tempAnchorIndexesCount;
			this.unknownFormatAnchorIndexes = this.tempAnchorIndexes;
		} else {
			if (this.fieldAnchorIndexes == null) {
				this.fieldAnchorIndexes = new int[this.type.getFields().length];
				this.fieldAnchorIndexesCount = 0;
				this.fieldLastAnchorFoundIndex = this.indexOfFieldDetails;
			}
			
			this.tempAnchorIndexes = this.fieldAnchorIndexes;
			this.tempAnchorIndexesCount = this.fieldAnchorIndexesCount;
			this.tempLastAnchorFoundIndex = this.fieldLastAnchorFoundIndex;
			
			range = computeChildRange(anchor, this.indexOfFieldsBottom);
			
			this.fieldLastAnchorFoundIndex = this.tempLastAnchorFoundIndex;
			this.fieldAnchorIndexesCount = this.tempAnchorIndexesCount;
			this.fieldAnchorIndexes = this.tempAnchorIndexes;
		}
		
		return range;
	}
	
	/*
	 * Compute the ranges of the parts of the javadoc that describe each method of the type
	 */
	private int[] computeMethodRange(IMethod method) throws JavaModelException {
		if (!this.hasComputedChildrenSections) {
			computeChildrenSections();
		}
		
		String anchor = computeMethodAnchorPrefixEnd((BinaryMethod)method);
		
		int[] range = null;
		
		if (this.indexOfAllMethodsTop == -1 || this.indexOfAllMethodsBottom == -1) {
			// the detail section has no top or bottom, so the doc has an unknown format
			if (this.unknownFormatAnchorIndexes == null) {
				this.unknownFormatAnchorIndexes = new int[this.type.getChildren().length];
				this.unknownFormatAnchorIndexesCount = 0;
				this.unknownFormatLastAnchorFoundIndex = this.childrenStart;
			}
			
			this.tempAnchorIndexes = this.unknownFormatAnchorIndexes;
			this.tempAnchorIndexesCount = this.unknownFormatAnchorIndexesCount;
			this.tempLastAnchorFoundIndex = this.unknownFormatLastAnchorFoundIndex;
			
			range = computeChildRange(anchor, this.indexOfFieldsBottom);
			
			this.unknownFormatLastAnchorFoundIndex = this.tempLastAnchorFoundIndex;
			this.unknownFormatAnchorIndexesCount = this.tempAnchorIndexesCount;
			this.unknownFormatAnchorIndexes = this.tempAnchorIndexes;
		} else {			
			if (this.methodAnchorIndexes == null) {
				this.methodAnchorIndexes = new int[this.type.getFields().length];
				this.methodAnchorIndexesCount = 0;
				this.methodLastAnchorFoundIndex = this.indexOfAllMethodsTop;
			}
			
			this.tempAnchorIndexes = this.methodAnchorIndexes;
			this.tempAnchorIndexesCount = this.methodAnchorIndexesCount;
			this.tempLastAnchorFoundIndex = this.methodLastAnchorFoundIndex;
			
			range = computeChildRange(anchor, this.indexOfAllMethodsBottom);
			
			this.methodLastAnchorFoundIndex = this.tempLastAnchorFoundIndex;
			this.methodAnchorIndexesCount = this.tempAnchorIndexesCount;
			this.methodAnchorIndexes = this.tempAnchorIndexes;
		}
		
		return range;
	}
	
	private String computeMethodAnchorPrefixEnd(BinaryMethod method) throws JavaModelException {
		String typeQualifiedName = null;
		if (this.type.isMember()) {
			IType currentType = this.type;
			StringBuffer buffer = new StringBuffer();
			while (currentType != null) {
				buffer.insert(0, currentType.getElementName());
				currentType = currentType.getDeclaringType();
				if (currentType != null) {
					buffer.insert(0, '.');
				}
			}
			typeQualifiedName = new String(buffer.toString());
		} else {
			typeQualifiedName = this.type.getElementName();
		}
		
		String methodName = method.getElementName();
		if (method.isConstructor()) {
			methodName = typeQualifiedName;
		}
		IBinaryMethod info = (IBinaryMethod) method.getElementInfo();

		char[] genericSignature = info.getGenericSignature();
		String anchor = null;
		if (genericSignature != null) {
			genericSignature = CharOperation.replaceOnCopy(genericSignature, '/', '.');
			anchor = Util.toAnchor(0, genericSignature, methodName, Flags.isVarargs(method.getFlags()));
			if (anchor == null) throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.UNKNOWN_JAVADOC_FORMAT, method));
		} else {
			anchor = Signature.toString(method.getSignature().replace('/', '.'), methodName, null, true, false, Flags.isVarargs(method.getFlags()));
		}
		IType declaringType = this.type;
		if (declaringType.isMember()) {
			int depth = 0;
			final String packageFragmentName = declaringType.getPackageFragment().getElementName();
			// might need to remove a part of the signature corresponding to the synthetic argument
			final IJavaProject javaProject = declaringType.getJavaProject();
			char[][] typeNames = CharOperation.splitOn('.', typeQualifiedName.toCharArray());
			if (!Flags.isStatic(declaringType.getFlags())) depth++;
			StringBuffer typeName = new StringBuffer();
			for (int i = 0, max = typeNames.length; i < max; i++) {
				if (typeName.length() == 0) {
					typeName.append(typeNames[i]);
				} else {
					typeName.append('.').append(typeNames[i]);
				}
				IType resolvedType = javaProject.findType(packageFragmentName, String.valueOf(typeName));
				if (resolvedType != null && resolvedType.isMember() && !Flags.isStatic(resolvedType.getFlags())) depth++;
			}
			if (depth != 0) {
				int indexOfOpeningParen = anchor.indexOf('(');
				if (indexOfOpeningParen == -1) return null;
				int index = indexOfOpeningParen;
				indexOfOpeningParen++;
				for (int i = 0; i < depth; i++) {
					int indexOfComma = anchor.indexOf(',', index);
					if (indexOfComma != -1) {
						index = indexOfComma + 2;
					}
				}
				anchor = anchor.substring(0, indexOfOpeningParen) + anchor.substring(index);
			}
		}
		return anchor + JavadocConstants.ANCHOR_PREFIX_END;
	}
	
	/*
	 * Compute the range of the part of the javadoc that describe the type
	 */
	private void computeTypeRange() throws JavaModelException {
		final int indexOfStartOfClassData = this.content.indexOf(JavadocConstants.START_OF_CLASS_DATA);
		if (indexOfStartOfClassData == -1) {
			this.typeDocRange = UNKNOWN_FORMAT;
			return;
		}
		int indexOfNextSeparator = this.content.indexOf(JavadocConstants.SEPARATOR_START, indexOfStartOfClassData);
		if (indexOfNextSeparator == -1) {
			this.typeDocRange = UNKNOWN_FORMAT;
			return;
		}
		int indexOfNextSummary = this.content.indexOf(JavadocConstants.NESTED_CLASS_SUMMARY, indexOfNextSeparator);
		if (indexOfNextSummary == -1 && this.type.isEnum()) {
			// try to find enum constant summary start
			indexOfNextSummary = this.content.indexOf(JavadocConstants.ENUM_CONSTANT_SUMMARY, indexOfNextSeparator);
		}
		if (indexOfNextSummary == -1 && this.type.isAnnotation()) {
			// try to find required enum constant summary start
			indexOfNextSummary = this.content.indexOf(JavadocConstants.ANNOTATION_TYPE_REQUIRED_MEMBER_SUMMARY, indexOfNextSeparator);
			if (indexOfNextSummary == -1) {
				// try to find optional enum constant summary start
				indexOfNextSummary = this.content.indexOf(JavadocConstants.ANNOTATION_TYPE_OPTIONAL_MEMBER_SUMMARY, indexOfNextSeparator);
			}
		}
		if (indexOfNextSummary == -1) {
			// try to find field summary start
			indexOfNextSummary = this.content.indexOf(JavadocConstants.FIELD_SUMMARY, indexOfNextSeparator);
		}
		if (indexOfNextSummary == -1) {
			// try to find constructor summary start
			indexOfNextSummary = this.content.indexOf(JavadocConstants.CONSTRUCTOR_SUMMARY, indexOfNextSeparator);
		}
		if (indexOfNextSummary == -1) {
			// try to find method summary start
			indexOfNextSummary = this.content.indexOf(JavadocConstants.METHOD_SUMMARY, indexOfNextSeparator);
		}
		
		if (indexOfNextSummary == -1) {
			// we take the end of class data
			indexOfNextSummary = this.content.indexOf(JavadocConstants.END_OF_CLASS_DATA, indexOfNextSeparator);
		} else {
			// improve performance of computation of children ranges
			this.childrenStart = indexOfNextSummary + 1;
		}
		
		if (indexOfNextSummary == -1) {
			this.typeDocRange = UNKNOWN_FORMAT;
			return;
		}
		/*
		 * Check out to cut off the hierarchy see 119844
		 * We remove what the contents between the start of class data and the first <P>
		 */
		int start = indexOfStartOfClassData + JavadocConstants.START_OF_CLASS_DATA_LENGTH;
		int indexOfFirstParagraph = this.content.indexOf("<P>", start); //$NON-NLS-1$
		if (indexOfFirstParagraph == -1) {
			indexOfFirstParagraph = this.content.indexOf("<p>", start); //$NON-NLS-1$
		}
		if (indexOfFirstParagraph != -1 && indexOfFirstParagraph < indexOfNextSummary) {
			start = indexOfFirstParagraph;
		}
		
		this.typeDocRange = new int[]{start, indexOfNextSummary};
	}
}
