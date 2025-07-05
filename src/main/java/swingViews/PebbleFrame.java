package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PebbleFrame<E extends PebbleFrame.SwappablePanel<E>> extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// the type for the kind of Panel that can be handled by this Frame
	public static abstract class SwappablePanel<T> extends JPanel {
		private static final long serialVersionUID = 1L;
		
		// 1) swapping logic
		private T leftNeighbor, rightNeighbor;
		void setLeftNeighbor(T left)  { this.leftNeighbor  = left; }
		void setRightNeighbor(T right){ this.rightNeighbor = right; }

		protected abstract void swapContentWith(T other);	// subclasses must implement this swapping method!

		private void swapLeft() {	swapContentWith(leftNeighbor); 	}
		private void swapRight() {	swapContentWith(rightNeighbor);	}	
		private boolean canSwapLeft() {   return leftNeighbor  != null;  }
	    private boolean canSwapRight() {  return rightNeighbor != null;  }	
	    
	    // 2) highlighting logic
	    protected abstract void highlight();
	    protected abstract void dehighlight();
	}
	
	// private utility method for wiring up SwappablePanel instances
	private void chain(List<E> panels) {
	    for (int i = 0; i < panels.size(); i++) {
	      E cur = panels.get(i);
	      if (i > 0)  cur.setLeftNeighbor(panels.get(i - 1));
	      if (i < panels.size() - 1) cur.setRightNeighbor(panels.get(i + 1));
	    }
	 }	

	private SwappablePanel<E> selectedPebble;
	private Action leftAction, rightAction;

	public PebbleFrame(String title, List<E> pebbles) {
		super(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 1. Create selectable pebbles and wire out-of-nowhere selection
		chain(pebbles);
		JPanel pebblePanel = new JPanel();
		pebblePanel.setLayout(new BoxLayout(pebblePanel, BoxLayout.X_AXIS));
		pebblePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		for (SwappablePanel<E> p : pebbles) {
			p.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (p != selectedPebble) {
						PebbleFrame.this.passSelectionTo(p); // implements out-of-nowhere selection
					}
				}
			});
			pebblePanel.add(p);
			pebblePanel.add(Box.createHorizontalStrut(8));
		}

		// 2. Define Actions for swapping adjacent pebbles
		leftAction = new AbstractAction("←") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapLeft()) {
					selectedPebble.swapLeft(); 
					PebbleFrame.this.passSelectionTo(selectedPebble.leftNeighbor); // implements content-tracking selection
				}
			}
		};

		rightAction = new AbstractAction("→") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapRight()) {
					selectedPebble.swapRight();
					PebbleFrame.this.passSelectionTo(selectedPebble.rightNeighbor); // implements content-tracking selection
				}
			}
		};

		// 3. Buttons panel
		JPanel controls = new JPanel(new FlowLayout());
		controls.add(new JButton(leftAction));
		controls.add(new JButton(rightAction));

		// 4. Layout frame
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pebblePanel, BorderLayout.CENTER);
		getContentPane().add(controls, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);

		// 5. Initial selection
		passSelectionTo(pebbles.get(0));
		setVisible(true);
	}

	// 'passes' the selection on to a newly selected recipient pebble
	private void passSelectionTo(SwappablePanel<E> recipient) {
		// implements toggle-like selection
		if (selectedPebble != null) { // is null at initialization!
			selectedPebble.dehighlight();
		}
		recipient.highlight();
		selectedPebble = recipient; // updates selectedPebble

		// updates actions based on selectedPebble
		leftAction.setEnabled(selectedPebble != null && selectedPebble.canSwapLeft());
		rightAction.setEnabled(selectedPebble != null && selectedPebble.canSwapRight());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new PebbleFrame<LetterPebble>("Letter Swap Game", LetterPebble.fromString("PALESTINA"));
		});
	}
}
