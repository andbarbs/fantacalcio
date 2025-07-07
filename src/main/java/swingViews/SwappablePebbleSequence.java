package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import swingViews.SwappablePebbleSequence.*;

@SuppressWarnings("serial")
public class SwappablePebbleSequence
	<SwappablePebble extends JComponent & SwappableSequenceDriver.Swappable<SwappablePebble> & Highlightable>
		extends JPanel {
	
	/* 
	 * arranges suitable clients in a sequence such that the contents 
	 * of one client can be swapped with its left and right neighbors.
	 * 
	 * To this end, it
	 * 	> employs a SwappableSequenceDriver that services client swap requests
	 * 	> provides a graphical "selection" facility that allows the 
	 *    user to pick the argument for a client swap request
	 *  > provides buttons that allow the user to send a swap 
	 *    request to the driver concerning the selected client
	 */
	
	
	/* implementation of the Selection mechanism */
	
	// an interface for clients to augment their selection
	public static interface Highlightable {
		void highlight();   // Draw yourself in a “highlighted” state
		void dehighlight(); // Draw yourself in a “normal” state
	}

	// a type for the selectable slot that contains a client
	private class PebbleSlot extends JPanel {
		SwappablePebble pebble; // the client instance

		PebbleSlot(SwappablePebble panel) {
			this.pebble = panel;
			setLayout(new BorderLayout());
			add(panel, BorderLayout.CENTER);
		}

		void select() {
			repaint();
			pebble.highlight();
		}

		void deselect() {
			repaint();
			pebble.dehighlight();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth(), h = getHeight();
			Graphics2D g2 = (Graphics2D) g.create();

			// draw pebble circle
			g2.setColor(this == selectedSlot ? Color.ORANGE : Color.LIGHT_GRAY);
			g2.fillOval(5, 5, w - 10, h - 10);

			g2.dispose();
		}
	}

	private List<PebbleSlot> slots;
	private PebbleSlot selectedSlot; // bookkeeping reference for managing selection

	private SwappableSequenceDriver<SwappablePebble> driver;
	private Action leftSwapAction, rightSwapAction;
	
	public SwappablePebbleSequence(List<SwappablePebble> clients) {

		// 1. Initializes driver to client instances received
		driver = new SwappableSequenceDriver<SwappablePebble>(clients);

		// 2. Wraps each client in a slot and stores slots
		slots = clients.stream().map(PebbleSlot::new).collect(Collectors.toList());

		// 3. Add slots to their container and wire selection mechanism
		JPanel pebblePanel = new JPanel(new FlowLayout());
		slots.forEach(slot -> {
			pebblePanel.add(slot);
			slot.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (slot != selectedSlot) {
						// implements out-of-nowhere selection
						handSelectionTo(slot);
					}
				}
			});
		});

		// 4. Define Actions for swapping adjacent pebbles
		leftSwapAction = new AbstractAction("←") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && driver.canSwapLeft(selectedSlot.pebble)) {
					driver.swapLeft(selectedSlot.pebble); // sends swap request to driver

					// implements content-tracking selection
					handSelectionTo(slots.get(slots.indexOf(selectedSlot) - 1));
				}
			}
		};

		rightSwapAction = new AbstractAction("→") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && driver.canSwapRight(selectedSlot.pebble)) {
					driver.swapRight(selectedSlot.pebble); // sends swap request to driver

					// implements content-tracking selection
					handSelectionTo(slots.get(slots.indexOf(selectedSlot) + 1));
				}
			}
		};

		// 5. Buttons panel
		JPanel controls = new JPanel(new FlowLayout());
		controls.add(new JButton(leftSwapAction));
		controls.add(new JButton(rightSwapAction));

		// 6. Lays out frame
		setLayout(new BorderLayout());
		add(pebblePanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);

		// 7. Initial selection
		handSelectionTo(slots.get(0));
	}

	// 'passes' the selection on to a newly selected slot
	private void handSelectionTo(PebbleSlot recipientSlot) {
		// implements toggle-like selection
		if (selectedSlot != null) { // is null at initialization!
			selectedSlot.deselect();
		}
		selectedSlot = recipientSlot; // updates selectedPebble
		recipientSlot.select();

		// updates actions based on selectedPebble
		leftSwapAction.setEnabled(selectedSlot != null && driver.canSwapLeft(selectedSlot.pebble));
		rightSwapAction.setEnabled(selectedSlot != null && driver.canSwapRight(selectedSlot.pebble));
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("letter Swap game");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new SwappablePebbleSequence<LetterPebble>(LetterPebble.fromString("PALESTINA")));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
