package net.jackw.authenticator;


public abstract class CodeGenerator {
	public enum Type {
		TOTP (1),
		HOTP (2);

		public final int value;
		private Type(int value) {
			this.value = value;
		}

		public static Type get (int i) {
			for (Type type : Type.values()) {
				if (type.value == i) {
					return type;
				}
			}
			return null;
		}
	}

	public abstract String getExtra ();

	public abstract Type getType ();

	public abstract String getCodeForDisplay ();
}
