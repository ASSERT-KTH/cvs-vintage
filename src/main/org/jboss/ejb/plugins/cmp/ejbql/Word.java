package org.jboss.ejb.plugins.cmp.ejbql;

public class Word extends Terminal {

   public Word() {
   }
   
   protected boolean isValidToken(Token token) {
      // basically anything but a symbol
      return token instanceof ApproximateNumericToken ||
            token instanceof ExactNumericToken ||
            token instanceof StringToken ||
            token instanceof WordToken;
   }
}
