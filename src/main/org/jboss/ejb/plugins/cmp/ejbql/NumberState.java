package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;
import java.io.CharArrayWriter;

public class NumberState implements TokenizerState {
	private TokenizerState symbolState = new SymbolState();

	public NumberState() {
	}

	public Token nextToken(PushbackReader in, char c, Tokenizer tokenizer)
			throws IOException {
				
		if(!Character.isDigit(c) && c != '.') {
			throw new IllegalArgumentException("NumberState must begin with a digit or a '.': c="+c);
		}
		
		// do we just have a peroid
		if(c == '.') {
			int peek = peekChar(in);
			if(!Character.isDigit((char)peek)) {
				// deligate to the symbolState 
				return symbolState.nextToken(in, c, tokenizer);
			}
		}
			
		//
		// Now we are definately working on a number
		//
	
		// put the first characte back on, makes the code easier to write
		in.unread(c);
		
		// output buffer
		CharArrayWriter out = new CharArrayWriter(16);
		
		// whole number part
		readWholeNumberPart(in, out);
		
		// fractional part
		readFractionalPart(in, out);
		
		// exponent part
		readExponentPart(in, out);
		
		// exponent part
		readSuffix(in, out);
		
		String number = out.toString().toLowerCase();
		System.out.println("number is ["+number+"]");
		if(isExactNumeric(number)) {
			return createExactNumericToken(number);
		} else {
			return createApproximateNumericToken(number);
		}
	}
	
	private void readWholeNumberPart(PushbackReader in, CharArrayWriter out) throws IOException {
		int first = peekChar(in);
		if(Character.isDigit((char)first)) {
			// read the first digit off the stream and write it out
			out.write(in.read());

			// is it a hex number
			int second = peekChar(in);	
			System.out.println("HEX? " + (char)first + (char)second);
			if(first == '0' && (second == 'x' || second == 'X')) {
				// read the x off the stream and write it out
				out.write(in.read());
				
				readNumber(in, out, 16);
				
			} else {
				// can't check for octal yet, because we don't
				// know if this is a float number yet, but dec will work for now
				readNumber(in, out, 10);
			}
		} 
	}
	
	private void readFractionalPart(PushbackReader in, CharArrayWriter out) throws IOException {
		int peek = peekChar(in);
		if(peek == '.') {

			// read the peroid off the stream and write it out
			out.write(in.read());
			
			// get all the decimal digits
			readNumber(in, out, 10);
			
			System.out.println("Read fractional part numebr is ["+out.toString()+"]");
		}
	}
	
	private void readExponentPart(PushbackReader in, CharArrayWriter out) throws IOException {
		int peek = peekChar(in);
		if(peek == 'e' || peek == 'E') {

			// read the e off the stream and write it out
			out.write(in.read()); 

			// check for a sign in the exponent
			peek = peekChar(in);
			if(peek == '+' || peek == '-') {
				// read the sign off the stream and write it out
				out.write(in.read());
			}
			
			// read the integer
			readNumber(in, out, 10);
		}
	}
		
	private void readSuffix(PushbackReader in, CharArrayWriter out) throws IOException {
		int peek = peekChar(in);
		if(peek == 'l' || peek == 'L' ||
				peek == 'f' || peek == 'F' ||
				peek == 'd' || peek == 'D') {

			// read the suffix off the stream and write it out
			out.write(in.read());
		}
	}
	
	private void readNumber(PushbackReader in, CharArrayWriter out, int radix) throws IOException {
		// which the read character is a digit in the specified radix
		int c = in.read(); 
		while(Character.digit((char)c, radix) != -1) {
			out.write(c);
			c = in.read();
		}
		// unread that last character because it is not a digit
		if(c != -1) {
			in.unread(c);
		}
	}
	
	private boolean isExactNumeric(String number) {
		// is this a hexadecimal number
		if(number.startsWith("0x") || number.startsWith("0X")){
		   return true;
		}
		
		// does it contain a peroid
		if(number.indexOf('.')>=0) {
	   	return false;
		} 
		
		// does it contain an exponent
		if(number.indexOf('e')>=0 || number.indexOf('E')>=0) {
			return false;
		}
		
		// does it end with an f
		if(number.endsWith("f") || number.endsWith("F")) {
			return false;
		}
		
		// does it end with a d
		if(number.endsWith("d") || number.endsWith("D")) {
			return false;
		}
		
		return true;
	}

	/**
	 * This function is broken.  It does not support bit field style
	 * integers and longs 0xffffffff
	 */
	private ExactNumericToken createExactNumericToken(String number) throws IOException {
		// long suffix
		if(number.endsWith("l") || number.endsWith("L")) {
			// chop off the suffix
			number = number.substring(0, number.length()-1);
			System.out.println("decode long number ["+number+"]");
			return new ExactNumericToken(Long.decode(number).longValue());
		} else {
			return new ExactNumericToken(Integer.decode(number.toUpperCase()).intValue());
		}
	}
    private ExactNumericLiteral createExactNumericLiteral(String number) throws IOException {
        byte first; // first digit

        // long suffix
        if(number.endsWith("l") || number.endsWith("L")) {
            // chop off the suffix
            number = number.substring(0, number.length() - 1);
            System.out.println("decode long number [" + number + "]");
            if (number.startsWith("0X") || number.startsWith("0x")) {  // hex
                // handle literals from 0x8000000000000000L to 0xffffffffffffffffL:
                // remove sign bit, parse as positive, then calculate the negative value with the sign bit
                if (number.length() == 18) {
                    first = Byte.decode(number.substring(0, 3)).byteValue();
                    if (first >= 8) {
                        number = "0x" + (first - 8) + number.substring(3);
                        return new ExactNumericLiteral(Long.decode(number).longValue() - Long.MAX_VALUE - 1);
                    }
                }
            } else if (number.startsWith("0")) {  // octal
                // handle literals from 01000000000000000000000L to 01777777777777777777777L
                // remove sign bit, parse as positive, then calculate the negative value with the sign bit
                if (number.length() == 23) {
                    if (number.charAt(1) == '1') {
                        number = "0" + number.substring(2);
                        return new ExactNumericLiteral(Long.decode(number).longValue() - Long.MAX_VALUE - 1);
                    }
                }
            }
            return new ExactNumericLiteral(Long.decode(number).longValue());
        } else {
            // integer hex and octal literals like 0xffffffff are handled by Long.decode()
            return new ExactNumericLiteral(Long.decode(number).intValue());
        }
    }

	private ApproximateNumericToken createApproximateNumericToken(String number) throws IOException {
		// float suffix
		if(number.endsWith("f") || number.endsWith("F")) {
			// chop off the suffix
			number = number.substring(0, number.length()-1);
			return new ApproximateNumericToken(Float.parseFloat(number));
		} 
		
		// ends with a d suffix, chop it off
		if(number.endsWith("d") || number.endsWith("D")) {
			number = number.substring(0, number.length()-1);
		}
	
		// regular double
		return new ApproximateNumericToken(Double.parseDouble(number));		
	}

	private int peekChar(PushbackReader in) throws IOException {
		int nextChar = in.read();
		if(nextChar != -1) {
			in.unread(nextChar);
		}
		return nextChar;
	}
}
