package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class NameReference extends Reference implements InvocationSite, BindingIds {

	public Binding binding, codegenBinding; //may be aTypeBinding-aFieldBinding-aLocalVariableBinding
	
	public TypeBinding receiverType, actualReceiverType;

	//the error printing
	//some name reference are build as name reference but
	//only used as type reference. When it happens, instead of
	//creating a new objet (aTypeReference) we just flag a boolean
	//This concesion is valuable while their are cases when the NameReference
	//will be a TypeReference (static message sends.....) and there is
	//no changeClass in java.
public NameReference() {
	super();
	bits |= TYPE | VARIABLE; // restrictiveFlag
	
}
public FieldBinding fieldBinding() {
	//this method should be sent ONLY after a check against isFieldReference()
	//check its use doing senders.........

	return (FieldBinding) binding ;
}
public boolean isSuperAccess() {
	return false;
}
public boolean isTypeAccess() {
	// null is acceptable when we are resolving the first part of a reference
	return binding == null || binding instanceof ReferenceBinding;
}
public boolean isTypeReference() {
	return binding instanceof ReferenceBinding;
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	this.actualReceiverType = receiverType;
}
public void setDepth(int depth) {
	if (depth > 0) {
		bits &= ~DepthMASK; // flush previous depth if any			
		bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
	}
}
public void setFieldIndex(int index){
	// ignored
}

public abstract String unboundReferenceErrorName();
}
