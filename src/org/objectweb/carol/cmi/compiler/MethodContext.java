/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
/***
 * Jonathan: an Open Distributed Processing Environment 
 * Copyright (C) 1999 France Telecom R&D
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Release: 2.0
 *
 * Contact: jonathan@objectweb.org
 *
 * Author: Kathleen Milsted
 *
 * 
 */
package org.objectweb.carol.cmi.compiler;

import java.lang.reflect.Method;

/**
 * This is a container class containing information relevant to a remote method
 * of a class being processed by the stub compiler.
 */
public class MethodContext {

    int index;
    Method mth;
    String mthName, classVarName, declItfName;
    int nbParams, nbExcs;
    String returnTypeName,
        returnRefTypeName,
        returnConversion,
        returnCast,
        returnMarshaller,
        returnUnMarshaller;
    boolean returnsVoid = false;
    boolean declaresJavaLangException = false;
    boolean declaresJavaRmiRemoteException = false;

    private Class[] paramTypes, excTypes;
    private String[] paramTypeNames,
        paramNames,
        paramsAsObjs,
        paramMarshallers,
        paramUnMarshallers,
        paramCasts,
        excTypeNames;

    MethodContext(Method mth, int index, Class itf) {
        this.mth = mth;
        this.index = index;
        mthName = mth.getName();
        classVarName = "$method_" + mthName + "_" + index;
        declItfName = itf.getName();
        // parameter info
        paramTypes = mth.getParameterTypes();
        nbParams = paramTypes.length;
        // return type info
        Class returnType = mth.getReturnType();
        if (returnType == Void.TYPE)
            returnsVoid = true;
        returnTypeName = Utils.typeName(returnType);
        returnRefTypeName = Utils.prim2refTypeName(returnType);
        returnConversion = Utils.ref2primConversion(returnType);
        if (returnType.isPrimitive()) {
            returnCast = "";
        } else {
            returnCast = "(" + returnTypeName + ") ";
        }
        returnUnMarshaller = Utils.typeUnMarshaller(returnType);
        returnMarshaller = Utils.typeMarshaller(returnType);
        // exception info
        excTypes = Utils.getSortedExceptions(this);
        nbExcs = excTypes.length;
    }

    String[] getParamTypeNames() {
        if (paramTypeNames == null) {
            paramTypeNames = new String[nbParams];
            for (int i = 0; i < nbParams; i++) {
                paramTypeNames[i] = Utils.typeName(paramTypes[i]);
            }
        }
        return paramTypeNames;
    }

    String[] getParamNames() {
        if (paramNames == null) {
            paramNames = new String[nbParams];
            for (int i = 0; i < nbParams; i++) {
                paramNames[i] = "$param_" + (i + 1);
            }
        }
        return paramNames;
    }

    String[] getParamsAsObjs() {
        if (paramsAsObjs == null) {
            paramsAsObjs = new String[nbParams];
            String[] ignored = getParamNames();
            for (int i = 0; i < nbParams; i++) {
                if (paramTypes[i].isPrimitive()) {
                    paramsAsObjs[i] =
                        "new "
                            + Utils.prim2refTypeName(paramTypes[i])
                            + "("
                            + paramNames[i]
                            + ")";
                } else {
                    paramsAsObjs[i] = paramNames[i];
                }
            }
        }
        return paramsAsObjs;
    }

    String[] getParamMarshallers() {
        if (paramMarshallers == null) {
            paramMarshallers = new String[nbParams];
            for (int i = 0; i < nbParams; i++) {
                paramMarshallers[i] = Utils.typeMarshaller(paramTypes[i]);
            }
        }
        return paramMarshallers;
    }

    String[] getParamUnMarshallers() {
        if (paramUnMarshallers == null) {
            paramUnMarshallers = new String[nbParams];
            for (int i = 0; i < nbParams; i++) {
                paramUnMarshallers[i] = Utils.typeUnMarshaller(paramTypes[i]);
            }
        }
        return paramUnMarshallers;
    }

    String[] getParamCasts() {
        if (paramCasts == null) {
            paramCasts = new String[nbParams];
            String[] ignored = getParamTypeNames();
            for (int i = 0; i < nbParams; i++) {
                if (paramTypes[i].isPrimitive()) {
                    paramCasts[i] = "";
                } else {
                    paramCasts[i] = "(" + paramTypeNames[i] + ") ";
                }
            }
        }
        return paramCasts;
    }

    String[] getExceptionTypeNames() {
        if (excTypeNames == null) {
            excTypeNames = new String[nbExcs];
            for (int i = 0; i < nbExcs; i++) {
                excTypeNames[i] = excTypes[i].getName();
            }
        }
        return excTypeNames;
    }

}
