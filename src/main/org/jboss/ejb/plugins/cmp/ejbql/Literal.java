package org.jboss.ejb.plugins.cmp.ejbql;

public class Literal extends Terminal {
   private String literal;
   
   public Literal(String literal) {
      if(literal == null) {
         throw new IllegalArgumentException("literal is null");
      }
      this.literal = literal;
   }

   protected boolean isValidToken(Token token) {
      if(token instanceof WordToken) {
         return literal.equalsIgnoreCase(token.toString());
      }
      return false;
   }
   
   public String toString() {
      return "[Literal: literal="+literal+"]";
   }
}
