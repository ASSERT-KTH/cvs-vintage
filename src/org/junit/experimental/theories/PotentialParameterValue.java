package org.junit.experimental.theories;

public abstract class PotentialParameterValue {
	public static class CouldNotGenerateValueException extends Exception {
		private static final long serialVersionUID= 1L;
	}
	
	public static PotentialParameterValue forValue(final Object value) {
		return new PotentialParameterValue() {		
			@Override
			public Object getValue() throws CouldNotGenerateValueException {
				return value;
			}
			
			@Override
			public String toString() {
				return String.format("[%s]", value);
			}
		};
	}
	
	public abstract Object getValue() throws CouldNotGenerateValueException;
}
