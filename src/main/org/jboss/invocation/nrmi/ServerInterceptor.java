package org.jboss.invocation.nrmi;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.nrmi.algorithm.NRMI;


/**
 * The server site NRMI interceptor.
 * Must be first in the chain of server interceptors.
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 */
public class ServerInterceptor extends AbstractInterceptor {
	
/**
 * Make linear map for all copy-restore parameters, make the call, and include
 * linear maps as an attachement. 
 */
public InvocationResponse invoke(final Invocation invocation) throws Exception {
boolean[] passByCopyRestore = 
			(boolean[])invocation.getValue(ClientInterceptor.PASS_BY_COPY_RESTORE);

Object[][] linearRepresentations = NRMI.createLinearRepresentations (passByCopyRestore,
                                                                     invocation.getArguments()); 
							                                        	
InvocationResponse invocationResponse = getNext().invoke(invocation);
invocationResponse.addAttachment(ClientInterceptor.LINEAR_REPRESENTATIONS,
								 linearRepresentations);
return invocationResponse;
}

}