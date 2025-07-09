package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import swingViews.FillableSwappableGadgetSequence.*;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableVisual;

@SuppressWarnings("serial")
public class FillableSwappableGadgetSequence
		<FillableSwappable extends JComponent & ToggleSelectable & FillableSwappableGadget<FillableSwappable>> 
			extends JPanel implements FillableSwappableVisual<FillableSwappable> {
	
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
	public interface ToggleSelectable {
		void toggleSelect();   // Draw yourself in a “highlighted” state
		void toggleDeselect(); // Draw yourself in a “normal” state
	}

	// a type for the toggle-selectable slot that contains a client
	private class GadgetSlot extends JPanel {
		FillableSwappable gadget; // the client instance

		GadgetSlot(FillableSwappable client) {
			this.gadget = client;
			GridBagLayout gbl_slot = new GridBagLayout();
			gbl_slot.columnWidths = new int[]{0, 0};
			gbl_slot.rowHeights = new int[]{0, 0};
			gbl_slot.columnWeights = new double[]{0.0, Double.MIN_VALUE};
			gbl_slot.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			setLayout(gbl_slot);
			
			GridBagConstraints gbc_client = new GridBagConstraints();
			gbc_client.gridy = 0;
			gbc_client.gridx = 0;
			gbc_client.anchor = GridBagConstraints.CENTER;
			gbc_client.fill = GridBagConstraints.BOTH;
			gbc_client.insets = new Insets(30, 30, 30, 30);
			add(client, gbc_client);
		}

		void select() {
			repaint();
			gadget.toggleSelect();
		}

		void deselect() {
			repaint();
			gadget.toggleDeselect();
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

	// internal bookkeeping for the selection mechanism
	private List<GadgetSlot> slots;
	private GadgetSlot selectedSlot;

	private FillableSwappableSequenceDriver<FillableSwappable> driver;
	private Action leftSwapAction, rightSwapAction;
	
	public FillableSwappableGadgetSequence(List<FillableSwappable> clients) {

		// 1. Initializes driver to client instances received
		driver = new FillableSwappableSequenceDriver<FillableSwappable>(clients, this);

		// 2. Wraps each client in a slot and stores slots
		slots = clients.stream().map(GadgetSlot::new).collect(Collectors.toList());

		// 3. Add slots to their container and wire selection mechanism
		JPanel pebblePanel = new JPanel(new FlowLayout());
		slots.forEach(slot -> {
			pebblePanel.add(slot);
			slot.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.out.printf("driver says %s content!\n", (driver.hasContent(slot.gadget)? "YES" : "NO"));
					if (slot != selectedSlot && driver.hasContent(slot.gadget)) {
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
				if (selectedSlot != null && driver.canSwapLeft(selectedSlot.gadget)) {
					driver.swapLeft(selectedSlot.gadget); // sends swap request to driver

					// implements content-tracking selection
					handSelectionTo(slots.get(slots.indexOf(selectedSlot) - 1));
				}
			}
		};

		rightSwapAction = new AbstractAction("→") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot != null && driver.canSwapRight(selectedSlot.gadget)) {
					driver.swapRight(selectedSlot.gadget); // sends swap request to driver

					// implements content-tracking selection
					handSelectionTo(slots.get(slots.indexOf(selectedSlot) + 1));
				}
			}
		};

		// 5. Buttons panel
		JPanel controls = new JPanel(new FlowLayout());
		JButton leftSwapButton = new JButton(leftSwapAction);
		JButton rightSwapButton = new JButton(rightSwapAction);
		List.of(leftSwapButton, rightSwapButton).forEach(b -> b.setBackground(Color.ORANGE));
		controls.add(leftSwapButton);
		controls.add(rightSwapButton);

		// 6. Lays out frame
		setLayout(new BorderLayout());
		add(pebblePanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);

		// 7. Initial selection
		handSelectionTo(slots.get(0));
	}

	// 'passes' the selection on to a newly selected slot
	private void handSelectionTo(GadgetSlot recipientSlot) {
		// implements toggle-like selection
		if (selectedSlot != null) { // is null at initialization!
			selectedSlot.deselect();
		}
		selectedSlot = recipientSlot; // updates selectedPebble
		recipientSlot.select();

		// updates actions based on selectedPebble
		leftSwapAction.setEnabled(selectedSlot != null && driver.canSwapLeft(selectedSlot.gadget));
		rightSwapAction.setEnabled(selectedSlot != null && driver.canSwapRight(selectedSlot.gadget));
	}
	
	@Override
	public void becameEmpty(FillableSwappable emptiedGadget) {
		GadgetSlot slot = slots.stream().filter(s -> s.gadget == emptiedGadget).findFirst().get();
		handSelectionTo(slots.get(slots.indexOf(slot) - 1));
	}

	@Override
	public void becameFilled(FillableSwappable filledGadget) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("rightward swappable text input game");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new FillableSwappableGadgetSequence<FillableSwappableTextField>(List.of(
					new FillableSwappableTextField(5),
					new FillableSwappableTextField(5),
					new FillableSwappableTextField(5),
					new FillableSwappableTextField(5))));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	

}
