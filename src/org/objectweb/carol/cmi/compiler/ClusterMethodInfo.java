/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
package org.objectweb.carol.cmi.compiler;

import java.util.Vector;

public class ClusterMethodInfo {
    private String signature = null;
    private int type = 0;
    public static final int ONE_CHOICE = 1;
    public static final int REDO_CHOICE = 2;
    public static final int REDO_CHOICE_RETRY = 3;

    private String methodName;
    private String returnType;
    private String[] paramTypes;
    private String className;

	public ClusterMethodInfo(String clName) {
		className = clName;
	}

    public void setSignature(String sign) throws Exception {
        try {
            Vector p = new Vector();
            if (signature != null)
                throw new Exception("Only one signature per method allowed, method : " + sign);
            signature = sign;
            int obr = sign.indexOf('(');
            if (obr < 0) badSignature(sign);
            int mns = sign.lastIndexOf(' ', obr) + 1;
            if (mns <= 0) badSignature(sign);
            methodName = sign.substring(mns, obr);
            int mnd = methodName.lastIndexOf('.');
            if (mnd >= 0) {
            	methodName = methodName.substring(mnd+1);
            }
            mns--;
            while (sign.charAt(mns - 1) == ' ') mns--;
            int rts = sign.lastIndexOf(' ', mns - 1) + 1;
            if (rts <= 0) badSignature(sign);
            returnType = sign.substring(rts, mns);
            int cbr = sign.indexOf(')');
            String params = sign.substring(obr+1, cbr);
            params.trim();
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
            	if (te > 0)
            		param = param.substring(0, te);
            	p.addElement(param);
            }
            paramTypes = new String[p.size()];
            p.copyInto(paramTypes);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw e;
        }
    }

    /**
     * Method badSignature.
     */
    private void badSignature(String sign) throws Exception {
    	throw new Exception("Bad method signature : " + sign);
    }


    private void assertType() throws Exception {
        if (type != 0)
            throw new Exception("Only one type per method allowed.");
    }

    public void setOneChoice() throws Exception {
        assertType();
        type = ONE_CHOICE;
    }

    public void setRedoChoice() throws Exception {
        assertType();
        type = REDO_CHOICE;
    }

    public void setRedoChoiceRetry() throws Exception {
        assertType();
        type = REDO_CHOICE_RETRY;
    }

    public void verify() throws Exception {
        if (signature == null)
            throw new Exception("A signature has to be be provided for each method.");
        if (type == 0)
            throw new Exception("Clustering type not provided for : " + signature);
    }

    public String getSignature() {
        return signature;
    }

    public int getType() {
        return type;
    }

    public String methodFieldType() {
        switch (type) {
        case REDO_CHOICE_RETRY:
            return "org.objectweb.StubListRandomChooser";
        case ONE_CHOICE:
            return null;
        case REDO_CHOICE:
            return null;
        default:
            return null;
        }
    }

    /**
     * Method match.
     * @param m
     * @return boolean
     */
    public boolean match(MethodContext m) {
    	if (!methodName.equals(m.mthName)) return false;
    	if (!returnType.equals(m.returnTypeName)) return false;
    	int i = paramTypes.length;
    	String ptn[] = m.getParamTypeNames();
    	int j = ptn.length;
    	if (i != j) return false;
    	for (i=0; i<j; i++) {
    		if (!paramTypes[i].equals(ptn[i])) return false;
    	}
    	return true;
    }
}
