package org.jboss.ejb.plugins.cmp.ejbql;

public class InputParameter extends Terminal {

   public InputParameter() {
   }
   
   protected boolean isValidToken(Token token) {
      return token instanceof InputParameterToken;
   }
}
