/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation.nrmi;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.nrmi.algorithm.NRMI;
import org.jboss.proxy.Interceptor;

/**
 * The NRMI client interceptor.
 * Must be last in the chain of client interceptors. 
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
  */
public class ClientInterceptor extends Interceptor 
{

/**
 * The key for the copy restore boolean array value
 */
final static String PASS_BY_COPY_RESTORE = "PassByCopyRestore";

/**
 * Linear map representations key used to put them as an attachement for InvocationResponse
 * on the server.
 */
final static String LINEAR_REPRESENTATIONS = "LinearRepresentations";
/**
 * Specifies which parameters should be passed by-copy-restore
 * The information should be extracted via XDoclet
 * possibly by using tag "@by-copy-restore paramName"
 * if a location in _passByCopyRestore is set to true,
 * the corresponding parameters should be passed by-copy-restore
 */
private boolean[] _passByCopyRestore;

/**
 * No-args constructor for externalization
 */
public ClientInterceptor () {}

/**
 * Linear map representations for the parameters passed by-copy-restore
 * and null otherwise.
 */
private Object[][] _linearRepresentations; 

public void setPassByCopyRestore(boolean[] passByCopyRestore) {
_passByCopyRestore = passByCopyRestore;
}

/**
 * @see org.jboss.proxy.Interceptor#invoke(Invocation)
 * Create a linear map for copy-restore parameters before making the call.
 * Restore the parameters after the call using the linear maps created by the server.
 */
public InvocationResponse invoke(Invocation invocation) throws Throwable {
Object[] arguments = invocation.getArguments();	
if (null == _passByCopyRestore)
	getDefaultPassByCopyRestore (arguments);
//create linear map representations for copy-restore arguments
_linearRepresentations = NRMI.createLinearRepresentations (_passByCopyRestore,
                                                           arguments);
//let the server know which args are passed by-copy-restore
invocation.setValue (PASS_BY_COPY_RESTORE, _passByCopyRestore);
InvocationResponse response = getNext().invoke(invocation);
Object[][] newLinearRepresentations = 
			(Object[][])response.getAttachment(LINEAR_REPRESENTATIONS);
//after the invocation, perform the restore for copy-restore args
NRMI.performRestore (_passByCopyRestore,
                     newLinearRepresentations,
                     _linearRepresentations);
_passByCopyRestore = null;
return response;
}

private boolean[] getDefaultPassByCopyRestore (Object[] arguments) {
_passByCopyRestore = new boolean[arguments.length];
//pass all reference parameters by copy-restore
//with the exception for unmodifiable classes --
//for now, exclude everything in java.lang
for (int i = 0; i < _passByCopyRestore.length; ++i)	
	_passByCopyRestore[i] = !arguments[i].getClass().getName().startsWith("java.lang");
return _passByCopyRestore;
}

}