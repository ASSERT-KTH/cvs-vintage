package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

/** 
 * Element info for ISourceReference elements. 
 */
/* package */ class SourceRefElementInfo extends JavaElementInfo {
	protected int fSourceRangeStart, fSourceRangeEnd;
protected SourceRefElementInfo() {
	setIsStructureKnown(true);
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceEnd()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceEnd()
 */
public int getDeclarationSourceEnd() {
	return fSourceRangeEnd;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceMethod#getDeclarationSourceStart()
 * @see org.eclipse.jdt.internal.compiler.env.ISourceField#getDeclarationSourceStart()
 */
public int getDeclarationSourceStart() {
	return fSourceRangeStart;
}
protected ISourceRange getSourceRange() {
	return new SourceRange(fSourceRangeStart, fSourceRangeEnd - fSourceRangeStart + 1);
}
protected void setSourceRangeEnd(int end) {
	fSourceRangeEnd = end;
}
protected void setSourceRangeStart(int start) {
	fSourceRangeStart = start;
}
}
