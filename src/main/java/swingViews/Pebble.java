package swingViews;

import javax.swing.JPanel;

public abstract class Pebble<T extends Pebble<T>> extends JPanel implements Swappable<T> {
	private static final long serialVersionUID = 1L;

	private T left; 
	private T right;

	public void setLeft(T left) {
		this.left = left;
	}

	public void setRight(T right) {
		this.right = right;
	}

	public void swapLeft() {
		swapWith(left);
	}

	public void swapRight() {
		swapWith(right);
	}
	
	
}
