package org.jboss.ejb.plugins.cmp.ejbql;

public class NumericLiteral extends Terminal {

   public NumericLiteral() {
   }
   
   protected boolean isValidToken(Token token) {
      // basically anything but a symbol
      return token instanceof ApproximateNumericToken ||
            token instanceof ExactNumericToken;
   }
}