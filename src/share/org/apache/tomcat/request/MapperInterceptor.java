/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.util.*;
import java.util.Hashtable;

// Based on RequestMapper.

/**
 * Process the URI, find the Wrapper ( the Servlet that will handle the request) 
 * and set all Paths.
 * 
 * This is the original parser from Tomcat - please don't change it, but
 * create new interceptors ( we need to split it in more specialized
 * modules - and better parsers )
 *
 */
public class MapperInterceptor  implements  RequestInterceptor {

    public MapperInterceptor() {
    }

    // no configuration 
    
    public int handleRequest(Request req) {
	Context context=req.getContext();
	String path=req.getLookupPath();
        ServletWrapper wrapper = null;

	wrapper = getMatch(context, path, req);

	if (wrapper == null) {
	    wrapper = context.getDefaultServlet();
	    if (wrapper == null) {
	        wrapper = context.getServletByName(Constants.DEFAULT_SERVLET_NAME );
	    }

	    req.setWrapper( wrapper );
	    req.setServletPath( "" );
	    req.setPathInfo( path);
	} else {
	    getMapPath(wrapper, req);
	    String resolvedServlet = getResolvedServlet(context, req.getMappedPath());
	    
	    req.setWrapper( wrapper );
	    req.setResolvedServlet( resolvedServlet );
	}
	
	if (req.getResolvedServlet() != null) {
	    req.setAttribute(Constants.Attribute.RESOLVED_SERVLET,
				 req.getResolvedServlet());
	} else if (req.getMappedPath() != null) {
	    req.setAttribute(Constants.Attribute.RESOLVED_SERVLET,
				 req.getMappedPath());
	} else {
	    req.removeAttribute(Constants.Attribute.RESOLVED_SERVLET);
	}

	return OK;
    }

    private ServletWrapper getMatch(Context context, String path, Request req) {
        ServletWrapper wrapper = null;
	// try an exact match

        wrapper = getPathMatch(context, path, req);

	// try a prefix match

	if (wrapper == null) {
	    wrapper = getPrefixMatch(context, path, req);
	}

	// try an extension match

	if (wrapper == null) {
	    wrapper = getExtensionMatch(context, path, req);
	}

	// lookup real servlet if what we're actually
	// dealing with a jsp file

        wrapper = getServletForJsp(context, wrapper, req);

	return wrapper;
    }

    private ServletWrapper getPathMatch(Context context, String path, Request req) {
        ServletWrapper wrapper = null;

	wrapper = (ServletWrapper)context.getPathMap().get(path);

	if (wrapper != null) {
	    req.setServletPath( path );
	    // this.servletPath = path;
	}

        return wrapper;
    }

    private ServletWrapper getPrefixMatch(Context context, String path, Request req) {
	ServletWrapper wrapper = null;
        String s = path;
	//	System.out.println("GetPrefixMatch: " + path  + " ctx=" + context.getPath());
	
	while (s.length() > 0) {
	    String suffix = (s.endsWith("/")) ? "*" : "/*";
	    
	    wrapper = (ServletWrapper)context.getPrefixMap().get(s + suffix);

	    if (wrapper != null) {
		if (s.endsWith("/")) {
                    String t = s.substring(0, s.length() - 1);

		    req.setServletPath( (t.trim().length() == 0) ? null : t );
		    t = s.substring(s.length() - 1);
		    req.setPathInfo(  (t.trim().length() == 0) ? null : t);
		    //    System.out.println("Mapper: pathInfo=" + t + " ctx=" + context.getPath());
		} else {
                    String t = s;

		    req.setServletPath(  (t.trim().length() == 0) ? null : t);
                    t = path.substring(s.length(), path.length());
		    req.setPathInfo((t.trim().length() == 0) ? null : t);
		    //		    System.out.println("Mapper: pathInfo=" + t + " ctx=" + context.getPath());
		}

		s = "";
	    } else {
	        int i = s.lastIndexOf("/");

                if (i > 0) {
		    s = s.substring(0, i);
                } else if (i == 0 &&
                    ! s.equals("/")) {
                    s = "/";
                } else {
                    s = "";
                }
            }
	}

	return wrapper;
    }

    private ServletWrapper getExtensionMatch(Context context, String path, Request req) {
        ServletWrapper wrapper = null;
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf("/");

	if (i > -1) {
	    String extension = path.substring(i);


	    if (j > i) {
	        int k = extension.indexOf("/");

		extension = extension.substring(0, k);
	    }

	    wrapper = (ServletWrapper)context.getExtensionMap().get(
	        "*" + extension);

	    if (wrapper != null) {
	        req.setServletPath( path );

		if (j > i) {
		    int k = i + path.substring(i).indexOf("/");
                    String s = path.substring(0, k);

		    req.setServletPath( (s.trim().length() == 0) ? null : s );
                    s = path.substring(k);
		    req.setPathInfo( (s.trim().length() == 0) ? null : s );
		}
	    }
	}

	return wrapper;
    }

    // XXX XXX XXX eliminate recursivity (costin )
    private ServletWrapper getServletForJsp(Context context, ServletWrapper wrapper, Request req) {
        if (wrapper != null) {
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo();
            req.setResourceName( req.getServletPath());

            boolean stillSearching = true;
            int counter = 0;


            while (stillSearching) {
                if (wrapper != null &&
                    wrapper instanceof JspWrapper  &&
                    wrapper.getServletClass() == null) {
                        req.setResourceName( ((JspWrapper)wrapper).getPath() );
                        wrapper = getMatch(context,
					   ((JspWrapper)wrapper).getPath() + (pathInfo == null ? "" : pathInfo),
					   req);
                        req.setMappedPath(  req.getServletPath() );

                        if (stillSearching &&
                            ++counter > Constants.RequestURIMatchRecursion) {
                            stillSearching = false;
                        }
                } else {
                    stillSearching = false;
                }
            }

            req.setServletPath( servletPath);
            req.setPathInfo(pathInfo);
        }

        return wrapper;
    }

    private void getMapPath(ServletWrapper wrapper, Request req) {
        String mapPath = req.getMappedPath();

        // XXX
        // this is added to make available the destination
        // resource be it a servlet or jsp file - could be
        // cleaned up a bit (wobbly)
        if (req.getServletPath().equals("/servlet") &&
            req.getPathInfo() != null) {
            String s = req.getPathInfo();

            if (req.getPathInfo().startsWith("/")) {
                s = req.getPathInfo().substring(1);
            }

            int i = s.indexOf("/");

            if (i > -1) {
                s = s.substring(0, i);
            }

            mapPath = "/" + s;
        } else if (mapPath == null &&
            req.getResourceName() != null) {
            mapPath = req.getResourceName();
        }

        req.setMappedPath( mapPath );
    }

    private String getResolvedServlet(Context context, String path) {
        String resolvedServlet = null;
        ServletWrapper[] sw = context.getServletsByPath(path);

        if (sw.length > 0) {
            // assume one
            resolvedServlet = sw[0].getServletName();
        }

        return resolvedServlet;
    }
}
