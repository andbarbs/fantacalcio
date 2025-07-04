package swingViews;

public class ConcreteSwappables{
	
	private interface Swappable<T extends Swappable<T>> {
		void swapWith(T other);
	}
	
	public static class ConcreteSwappable1 implements Swappable<ConcreteSwappable1>{

		@Override
		public void swapWith(ConcreteSwappable1 other) {
			// TODO Auto-generated method stub
			
		}
	}
	
																// NOT NICE!!
	public static class ConcretePebble2 implements Swappable<ConcreteSwappable1>{

		@Override
		public void swapWith(ConcreteSwappable1 other) {
			// TODO Auto-generated method stub
			
		}
	}
}
