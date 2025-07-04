package swingViews;

public interface Swappable<T extends Swappable<T>> {
	void swapWith(T other);
}
