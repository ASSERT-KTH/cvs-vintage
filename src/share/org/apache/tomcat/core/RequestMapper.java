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


package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import java.util.Hashtable;


/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class RequestMapper {
    private Container container = null;
    private Hashtable prefixMaps = null;
    private Hashtable extensionMaps = null;
    private Hashtable pathMaps = null;

    private String servletPath = null;
    private String mapPath = null;
    private String pathInfo = null;
    private String resourceName = null;

    RequestMapper(Container container) {
        this.container = container;
    }

    void setPathMaps(Hashtable pathMaps) {
        this.pathMaps = pathMaps;
    }

    void setPrefixMaps(Hashtable prefixMaps) {
        this.prefixMaps = prefixMaps;
    }
    
    void setExtensionMaps(Hashtable extensionMaps) {
        this.extensionMaps = extensionMaps;
    }

    Request lookupServlet(String path) {
	Request lookupResult = null;
	ServletWrapper wrapper = getMatch(path);

	if (wrapper != null) {
            this.mapPath = getMapPath(wrapper);
            String resolvedServlet = getResolvedServlet(this.mapPath);

	    lookupResult = new Request();
	    lookupResult.setWrapper( wrapper );
	    lookupResult.setServletPath( servletPath );
	    lookupResult.setMappedPath( mapPath );
	    lookupResult.setPathInfo(pathInfo);
	    lookupResult.setResolvedServlet( resolvedServlet );
        }

	return lookupResult;
    }

    private ServletWrapper getMatch(String path) {
        ServletWrapper wrapper = null;

	// try an exact match

        wrapper = getPathMatch(path);

	// try a prefix match

	if (wrapper == null) {
	    wrapper = getPrefixMatch(path);
	}

	// try an extension match

	if (wrapper == null) {
	    wrapper = getExtensionMatch(path);
	}

	// lookup real servlet if what we're actually
	// dealing with a jsp file

        wrapper = getResolvedServlet(wrapper);

	return wrapper;
    }

    private ServletWrapper getPathMatch(String path) {
        ServletWrapper wrapper = null;

	wrapper = (ServletWrapper)pathMaps.get(path);

	if (wrapper != null) {
	    this.servletPath = path;
	}

        return wrapper;
    }

    private ServletWrapper getPrefixMatch(String path) {
	ServletWrapper wrapper = null;
        String s = path;

	while (s.length() > 0) {
	    String suffix = (s.endsWith("/")) ? "*" : "/*";
 
	    wrapper = (ServletWrapper)prefixMaps.get(s + suffix);

	    if (wrapper != null) {
	        if (s.endsWith("/")) {
                    String t = s.substring(0, s.length() - 1);

		    this.servletPath = (t.trim().length() == 0) ? null : t;
		    t = s.substring(s.length() - 1);
		    this.pathInfo = (t.trim().length() == 0) ? null : t;
		} else {
                    String t = s;

		    this.servletPath = (t.trim().length() == 0) ? null : t;
                    t = path.substring(s.length(), path.length());
		    this.pathInfo = (t.trim().length() == 0) ? null : t;
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

    private ServletWrapper getExtensionMatch(String path) {
        ServletWrapper wrapper = null;
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf("/");

	if (i > -1) {
	    String extension = path.substring(i);


	    if (j > i) {
	        int k = extension.indexOf("/");

		extension = extension.substring(0, k);
	    }

	    wrapper = (ServletWrapper)extensionMaps.get(
	        "*" + extension);

	    if (wrapper != null) {
	        this.servletPath = path;

		if (j > i) {
		    int k = i + path.substring(i).indexOf("/");
                    String s = path.substring(0, k);

		    this.servletPath = (s.trim().length() == 0) ? null : s;
                    s = path.substring(k);
		    this.pathInfo = (s.trim().length() == 0) ? null : s;
		}
	    }
	}

	return wrapper;
    }

    private ServletWrapper getResolvedServlet(ServletWrapper wrapper) {
        if (wrapper != null) {
            String servletPath = this.servletPath;
            String pathInfo = this.pathInfo;
            boolean stillSearching = true;
            int counter = 0;

            this.resourceName = this.servletPath;

            while (stillSearching) {
                if (wrapper != null &&
                    wrapper.getPath() != null &&
                    wrapper.getServletClass() == null) {
                        this.resourceName = wrapper.getPath();
                        wrapper = getMatch(wrapper.getPath() +
                            (pathInfo == null ? "" : pathInfo));
                        this.mapPath = this.servletPath;

                        if (stillSearching &&
                            ++counter > Constants.RequestURIMatchRecursion) {
                            stillSearching = false;
                        }
                } else {
                    stillSearching = false;
                }
            }

            this.servletPath = servletPath;
            this.pathInfo = pathInfo;
        }

        return wrapper;
    }

    private String getMapPath(ServletWrapper wrapper) {
        String mapPath = this.mapPath;

        // XXX
        // this is added to make available the destination
        // resource be it a servlet or jsp file - could be
        // cleaned up a bit (wobbly)
        if (this.servletPath.equals(Constants.Servlet.Invoker.Map) &&
            this.pathInfo != null) {
            String s = this.pathInfo;

            if (this.pathInfo.startsWith("/")) {
                s = this.pathInfo.substring(1);
            }

            int i = s.indexOf("/");

            if (i > -1) {
                s = s.substring(0, i);
            }

            mapPath = "/" + s;
        } else if (mapPath == null &&
            this.resourceName != null) {
            mapPath = this.resourceName;
        }

        return mapPath;
    }

    private String getResolvedServlet(String path) {
        String resolvedServlet = null;
        ServletWrapper[] sw = this.container.getServletsByPath(path);

        if (sw.length > 0) {
            // assume one
            resolvedServlet = sw[0].getServletName();
        }

        return resolvedServlet;
    }
}
