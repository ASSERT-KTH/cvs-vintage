/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.loggers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;

/**
 * Time counting. 
 *
 */
public class AccountingInterceptor extends  BaseInterceptor {
    ContextManager cm;
    int debug=0;
    boolean acc=true;
    String trace="trace.log";
    BufferedOutputStream logF=null;
    public static final int ACC_PRE_CMAP=0;
    public static final int ACC_PRE_RMAP=1;
    public static final int ACC_POST_MAP=2;
    public static final int ACC_PRE_SERVICE=3;
    public static final int ACC_POST_SERVICE=4;
    public static final int ACC_IN_OUT=5;
    public static final int ACC_OUT_COUNT=6;

    public static final int COUNTERS_COUNT=10;

    int reqCountNote=0;

    
    public AccountingInterceptor() {
    }

    public void setTrace( String file ) {
	trace=file;
    }
    
    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	try {
	    logF=new BufferedOutputStream( new FileOutputStream( trace ));
	    reqCountNote=cm.getNoteId(ContextManager.REQUEST_NOTE,
				      "counters");
	} catch(IOException ex ) {
	    ex.printStackTrace();
	}
    }


    public int requestMap(Request request ) {
	if( acc ) {
	    Counters ct=(Counters)request.getNote(reqCountNote);
	    if( ct==null ) {
		request.setNote( reqCountNote, new Counters(COUNTERS_COUNT));
	    }
	    ct.setCounter( ACC_PRE_RMAP, System.currentTimeMillis() );
	}
	return 0;
    }

    public int contextMap( Request request ) {
	if( acc ) {
	    Counters ct=(Counters)request.getNote(reqCountNote);
	    if( ct==null ) {
		request.setNote( reqCountNote, new Counters(COUNTERS_COUNT));
	    }
	    ct.setCounter( ACC_PRE_CMAP, System.currentTimeMillis() );
	}
	return 0;
    }

    public int authenticate(Request request, Response response) {
	if( acc  ) {
	    Counters ct=(Counters)request.getNote(reqCountNote);
	    if( ct==null ) {
		request.setNote( reqCountNote, new Counters(COUNTERS_COUNT));
	    }
	    ct.setCounter( ACC_POST_MAP, System.currentTimeMillis() );
	}
	return 0;
    }

    public int authorize(Request request, Response response) {
	return 0;
    }


    public int preService(Request request, Response response) {
	if( acc ) {
	    Counters ct=(Counters)request.getNote(reqCountNote);
	    if( ct==null ) {
		request.setNote( reqCountNote, new Counters(COUNTERS_COUNT));
	    }
	    ct.setCounter( ACC_PRE_SERVICE, System.currentTimeMillis() );
	}
	return 0;
    }

    public int beforeBody( Request rrequest, Response response ) {
	return 0;
    }

    public int beforeCommit( Request request, Response response) {
	return 0;
    }


    public int afterBody( Request request, Response response) {
	return 0;
    }

    public int postService(Request request, Response response) {
	if( acc  ) {
	    Counters ct=(Counters)request.getNote(reqCountNote);
	    if( ct==null ) {
		request.setNote( reqCountNote, new Counters(COUNTERS_COUNT));
	    }
	    ct.setCounter( ACC_POST_SERVICE, System.currentTimeMillis() );

	    long t1=ct.getCounter( ACC_PRE_CMAP );
	    long t2=ct.getCounter( ACC_PRE_RMAP );
	    long t3=ct.getCounter( ACC_POST_MAP );
	    long t4=ct.getCounter( ACC_PRE_SERVICE );
	    long t5=ct.getCounter( ACC_POST_SERVICE );

	    long t21=t2-t1;
	    long t31=t3-t1;
	    long t54=t5-t4;
	    long t41=t4-t1;

	    long tout=ct.getCounter( ACC_OUT_COUNT );
	    StringBuffer sb=new StringBuffer();
	    // ContextMap, Map, Service, Pre-Service-Overhead
	    sb.append(t21).append(",");
	    sb.append(t31).append(",");
	    sb.append(t54).append(",");
	    sb.append(tout).append(",");
	    sb.append(t41).append("\n");
	    ct.recycle();
	    cnt++;
	    try {
		if( logF!=null ) logF.write(sb.toString().getBytes());
		if( (cnt % 64) == 0 ) logF.flush();
	    } catch( IOException ex ) {
		ex.printStackTrace();
	    }
	}

	
	return 0;
    }

    static int cnt=0;
    
    
}

/*
 */
final class Counters {
    long accTable[];

    public Counters( int len ) {
	accTable=new long[len];
    }
    

    public final void touchCounter( int pos ) {
	accTable[pos]=System.currentTimeMillis();
    }
	
    public final void setCounter( int pos, long value ) {
	accTable[pos]=value;
    }

    public final long getCounter( int pos ) {
	return accTable[pos];
    }

    public void recycle() {
	for( int i=0; i<accTable.length; i++ ) accTable[i]=0;
    }
}
