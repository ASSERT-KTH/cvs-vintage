/*
 *  Copyright 2001-2004 The Apache Software Foundation
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

package tadm;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Child tag to add ant targets to Ant taglib.
 * 
 */
public class AntTarget extends TagSupport {
    
    public AntTarget() {}

    public int doStartTag() throws JspException {
	Tag parent=getParent();
	if( parent == null  )
	    throw new JspException( "AntTarget used as top-level tag,"+
				    "must be inside <ant>");
	if( ! (parent instanceof AntTag ))
	    throw new JspException( "AntTarget must be child of <ant>");

	AntTag antTag=(AntTag)parent;
	if( name == null ) {
	    String names[]=pageContext.getRequest().
		getParameterValues( param );
	    for( int i=0; i<names.length; i++ ) 
		antTag.addTarget( names[i]);
	} else {
	    antTag.addTarget( name );
	}
	return SKIP_BODY;
    }

    //-------------------- Properties --------------------
    String name;
    String param;

    public void setName( String s ) {
	name=s;
    }
    public void setParam( String s ) {
	param=s;
    }
}
