package org.jboss.ejb.plugins.cmp.ejbql;

public class ApproximateNumericToken implements Token {
   private final double value;
   
   public ApproximateNumericToken(double value) {
      this.value = value;
   }

   public double getValue() {
      return value;
   }
   
   public int hashCode() {
      long l = Double.doubleToLongBits(value);
      return 37 * 17 +  (int)(l ^ (l >>> 32));
   }
   
   public boolean equals(Object o) {
      if(o instanceof ApproximateNumericToken) {
         return ((ApproximateNumericToken)o).value == value;
      }
      return false;
   }
   
   public String toString() {
      return "" + value;
   }
}
