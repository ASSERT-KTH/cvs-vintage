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

import java.util.ArrayList;

/**
 * @author nieuviar
 *
 */
public class MethodProto {
    private String methodName;
    private String returnType;
    private String[] paramTypes;
    private int hash;

    public MethodProto(String sign) throws Exception {
        try {
            int obr = sign.indexOf('(');
            if (obr < 0)
                badSignature(sign);
            int mns = sign.lastIndexOf(' ', obr);
            if (mns < 0)
                badSignature(sign);
            methodName = sign.substring(mns + 1, obr);
            int mnd = methodName.lastIndexOf('.');
            if (mnd >= 0) {
                methodName = methodName.substring(mnd + 1);
            }
            while (sign.charAt(mns - 1) == ' ')
                mns--;
            int rts = sign.lastIndexOf(' ', mns - 1);
            // OK even if rts is -1 : no modifier, only a return type, get it
            returnType = sign.substring(rts + 1, mns);
            int cbr = sign.indexOf(')');
            String params = sign.substring(obr + 1, cbr);
            ArrayList p = new ArrayList();
            params = params.trim();
            while (!"".equals(params)) {
                int com = params.indexOf(',');
                String param;
                if (com < 0) {
                    param = params;
                    params = "";
                } else {
                    param = params.substring(0, com);
                    params = params.substring(com + 1);
                }
                int te = param.indexOf(' ');
                if (te > 0) {
                    param = param.substring(0, te);
                    p.add(param);
                } else if (te < 0) {
                    p.add(param);
                }
                params = params.trim();
            }
            paramTypes = new String[p.size()];
            p.toArray(paramTypes);
            doHash();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public MethodProto(MethodContext mc) {
        returnType = mc.returnTypeName;
        methodName = mc.mthName;
        String[] args = mc.getParamTypeNames();
        paramTypes = new String[args.length];
        System.arraycopy(args, 0, paramTypes, 0, args.length);
        doHash();
    }

    /**
     * Generate hash value for this method prototype
     */
    private void doHash() {
        hash = returnType.hashCode() + methodName.hashCode();
        for (int i=0; i<paramTypes.length; i++) {
            hash += paramTypes[i].hashCode();
        }
    }

    /**
     * Method badSignature.
     */
    private void badSignature(String sign) throws Exception {
        throw new Exception("Bad method signature : " + sign);
    }
    
    public int hashCode() {
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj instanceof MethodProto) {
            MethodProto mp = (MethodProto)obj;
            String[] pt = mp.paramTypes;
            if (methodName.equals(mp.methodName) && (paramTypes.length == pt.length) && returnType.equals(mp.returnType)) {
                for (int i=0; i<paramTypes.length; i++) {
                    if (! paramTypes[i].equals(pt[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        String s = returnType + " " + methodName + "(";
        for (int i=0; i<paramTypes.length; i++) {
            if (i != 0) s += ",";
            s += paramTypes[i];
        }
        return s + ")";
    }
}
