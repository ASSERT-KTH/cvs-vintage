/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
package org.objectweb.carol.cmi.compiler;

import java.lang.reflect.Method;


public class MethodConf {
    private MethodProto methodProto;
    private ClassConf ccc;
    private String balancer;
    private Method method = null;

    public MethodConf(ClassConf ccc, MethodProto mp, String balancerName) {
        this.ccc = ccc;
        methodProto = mp;
        if (balancerName == null) {
            balancerName = ccc.getBalancer();
        }
        this.balancer = balancerName;
    }

    public ClassConf getClassConf() {
        return ccc;
    }

    public MethodProto getMethodProto() {
        return methodProto;
    }

    public String getBalancer() {
        return balancer;
    }

    public Method getMethod() {
        return method;
    }

    public String getMthName() {
        return method.getName();
    }

    public String getReturnTypeName() {
        return method.getReturnType().getName();
    }

    private String paramString;

    public String getParamString() {
        if (paramString != null) {
            return paramString;
        }
        Class[] params = method.getParameterTypes();
        String s = "";
        for (int i=0; i<params.length; i++) {
            if (s.equals("")) {
                s = params[i].getName() + " param" + (i + 1);
            } else {
                s += ", " + params[i].getName() + " param" + (i + 1);
            }
        }
        paramString = s;
        return s;
    }

    private String paramNamesString;

    public String getParamNamesString() {
        if (paramNamesString != null) {
            return paramNamesString;
        }
        int len = method.getParameterTypes().length;
        String s = "";
        for (int i=0; i<len; i++) {
            if (s.equals("")) {
                s = "param" + (i + 1);
            } else {
                s += ", param" + (i + 1);
            }
        }
        paramNamesString = s;
        return s;
    }

    private String throwsString;

    public String getThrowsString() {
        if (throwsString != null) {
            return throwsString;
        }
        Class[] ex = method.getExceptionTypes();
        String s = "";
        for (int i=0; i<ex.length; i++) {
            if (s.equals("")) {
                s = ex[i].getName();
            } else {
                s += ", " + ex[i].getName();
            }
        }
        throwsString = s;
        return s;
    }

    public String getDeclItfName() {
        return method.getDeclaringClass().getName();
    }

    public boolean returnsVoid() {
        return method.getReturnType().equals(void.class);
    }

    public void setMethod(Method method) throws CompilerException {
        if (this.method == null) {
            this.method = method;
        } else {
            throw new CompilerException("internal error");
        }
    }
}
