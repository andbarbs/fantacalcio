package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class PebbleFrame<Q extends JPanel & PebbleFrame.Swappable<Q> & PebbleFrame.Highlightable> extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// public interfaces clients must implement – besides extending JPanel – so PebbleFrame can drive them
	public static interface Swappable<T> {	    
	    void swapContentWith(T other);  // Swap the content of this with another
	}
	
	public static interface Highlightable {	    
	    void highlight(); 				// Draw yourself in a “highlighted” state
	    void dehighlight(); 			// Draw yourself in a “normal” state
	}
	 
	// the internal plumbing type PebbleFrame uses to handle client instances
	private class PebbleSlot extends JPanel {
		static final long serialVersionUID = 1L;
		Q panel;                        // the client instance
		
		PebbleSlot(Q panel) {
		    this.panel = panel;
		    setLayout(new BorderLayout());
		    add(panel, BorderLayout.CENTER);
		}
		
		PebbleSlot leftSlot, rightSlot;   		// neighbors in the chain
		boolean canSwapLeft()  { return leftSlot  != null; }
		boolean canSwapRight() { return rightSlot != null; }		
		void swapLeft()  { panel.swapContentWith(leftSlot.panel);  }
		void swapRight() { panel.swapContentWith(rightSlot.panel); }
		
		void select()   { panel.highlight();   }
		void deselect() { panel.dehighlight(); }
	}
	
	private PebbleSlot selectedSlot; // bookkeeping reference for managing selection
    private Action leftAction, rightAction;

    public PebbleFrame(String title, List<Q> clients) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 1. Wrap each client in the plumbing type
        List<PebbleSlot> slots = clients.stream()
            .map(PebbleSlot::new)
            .collect(Collectors.toList());

        // 2. Chain them
        for (int i = 0; i < slots.size(); i++) {
            if (i > 0)  slots.get(i).leftSlot = slots.get(i-1);
            if (i < slots.size()-1) 
                slots.get(i).rightSlot = slots.get(i+1);
        }
        
        // 3. Build UI with adapters
        JPanel pebblePanel = new JPanel(new FlowLayout());
        slots.forEach(slot -> {
            pebblePanel.add(slot);
            slot.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                	if (slot != selectedSlot) {
						PebbleFrame.this.moveSelectionTo(slot); // implements out-of-nowhere selection
					}
                }
            });
        });

		// 4. Define Actions for swapping adjacent pebbles
		leftAction = new AbstractAction("←") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && selectedSlot.canSwapLeft()) {
					selectedSlot.swapLeft(); 
					PebbleFrame.this.moveSelectionTo(selectedSlot.leftSlot); // implements content-tracking selection
				}
			}
		};

		rightAction = new AbstractAction("→") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && selectedSlot.canSwapRight()) {
					selectedSlot.swapRight();
					PebbleFrame.this.moveSelectionTo(selectedSlot.rightSlot); // implements content-tracking selection
				}
			}
		};

		// 5. Buttons panel
		JPanel controls = new JPanel(new FlowLayout());
		controls.add(new JButton(leftAction));
		controls.add(new JButton(rightAction));

		// 6. Lays out frame
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pebblePanel, BorderLayout.CENTER);
		getContentPane().add(controls, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);

		// 7. Initial selection
		moveSelectionTo(slots.get(0));
		setVisible(true);
	}

	// 'passes' the selection on to a newly selected slot
	private void moveSelectionTo(PebbleSlot  recipientSlot) {
		// implements toggle-like selection
		if (selectedSlot != null) { // is null at initialization!
			selectedSlot.deselect();
		}
		selectedSlot = recipientSlot; // updates selectedPebble
		recipientSlot.select();

		// updates actions based on selectedPebble
		leftAction.setEnabled(selectedSlot != null && selectedSlot.canSwapLeft());
		rightAction.setEnabled(selectedSlot != null && selectedSlot.canSwapRight());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new PebbleFrame<LetterPebble>("Letter Swap Game", LetterPebble.fromString("PALESTINA"));
		});
	}
}
