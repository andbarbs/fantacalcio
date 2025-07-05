package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import swingViews.BidirectionalPebbleChain.*;

@SuppressWarnings("serial")
public class BidirectionalPebbleChain
	<Q extends JPanel & Swappable<Q> & Highlightable> 
		extends JPanel {
	
	// public interfaces clients must implement – besides extending JPanel – so PebbleFrame can drive them
	public static interface Swappable<T> {	    
	    void swapContentWith(T other);  // Swap the content of this with another
	}
	
	public static interface Highlightable {	    
	    void highlight(); 				// Draw yourself in a “highlighted” state
	    void dehighlight(); 			// Draw yourself in a “normal” state
	}
	 
	// internal plumbing type PebbleFrame uses to handle client instances
	private class PebbleSlot extends JPanel {
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

    public BidirectionalPebbleChain(List<Q> clients) {
    	
        // 1. Wrap each client in the plumbing type
        List<PebbleSlot> slots = clients.stream()
            .map(PebbleSlot::new)
            .collect(Collectors.toList());

        // 2. Chain slots
        for (int i = 0; i < slots.size(); i++) {
            if (i > 0)  slots.get(i).leftSlot = slots.get(i-1);
            if (i < slots.size()-1) 
                slots.get(i).rightSlot = slots.get(i+1);
        }
        
        // 3. Add slots to container and wire Panel listener
        JPanel pebblePanel = new JPanel(new FlowLayout());
        slots.forEach(slot -> {
            pebblePanel.add(slot);
            slot.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                	if (slot != selectedSlot) {
						BidirectionalPebbleChain.this.moveSelectionTo(slot); // implements out-of-nowhere selection
					}
                }
            });
        });

		// 4. Define Actions for swapping adjacent pebbles
		leftAction = new AbstractAction("←") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && selectedSlot.canSwapLeft()) {
					selectedSlot.swapLeft(); 
					BidirectionalPebbleChain.this.moveSelectionTo(selectedSlot.leftSlot); // implements content-tracking selection
				}
			}
		};

		rightAction = new AbstractAction("→") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && selectedSlot.canSwapRight()) {
					selectedSlot.swapRight();
					BidirectionalPebbleChain.this.moveSelectionTo(selectedSlot.rightSlot); // implements content-tracking selection
				}
			}
		};

		// 5. Buttons panel
		JPanel controls = new JPanel(new FlowLayout());
		controls.add(new JButton(leftAction));
		controls.add(new JButton(rightAction));

		// 6. Lays out frame
		setLayout(new BorderLayout());
		add(pebblePanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);

		// 7. Initial selection
		moveSelectionTo(slots.get(0));
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
			JFrame frame = new JFrame("letter Swap game");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(
					new BidirectionalPebbleChain<LetterPebble>(
							LetterPebble.fromString("PALESTINA")));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);			
		});
	}
}
