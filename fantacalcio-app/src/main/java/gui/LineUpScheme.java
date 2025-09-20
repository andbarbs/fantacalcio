package gui;

// Visitable scheme type
public abstract class LineUpScheme {

	public interface LineUpSchemeVisitor {
		void visit433(Scheme433 scheme433);
		void visit343(Scheme343 scheme343);
		void visit532(Scheme532 scheme532);
	}

	public abstract void accept(LineUpSchemeVisitor visitor);

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}
	
	public static class Scheme433 extends LineUpScheme {
		@Override
		public
		void accept(LineUpSchemeVisitor visitor) {
			visitor.visit433(this);
		}
	}
	
	public static class Scheme343 extends LineUpScheme {
		@Override
		public
		void accept(LineUpSchemeVisitor visitor) {
			visitor.visit343(this);
		}
	}
	
	public static class Scheme532 extends LineUpScheme {
		@Override
		public
		void accept(LineUpSchemeVisitor visitor) {
			visitor.visit532(this);
		}
	}
}
