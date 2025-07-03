package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class LetterSwapGame extends JFrame {
	private static final long serialVersionUID = 1L;

	private List<PebbleLetter> pebbles;
	private PebbleLetter selectedPebble;
	private Action leftAction, rightAction; // Actions are stored instead of JButtons!

	public LetterSwapGame(char[] initialLetters) {
		super("LetterSwapGame");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 1. Create pebbles and wire selection
		pebbles = PebbleLetter.createChain(initialLetters);
		JPanel pebblePanel = new JPanel();
		pebblePanel.setLayout(new BoxLayout(pebblePanel, BoxLayout.X_AXIS));
		pebblePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		PropertyChangeListener selectListener = this::onPebbleSelected;
		for (PebbleLetter p : pebbles) {
			p.addPropertyChangeListener("newlySelected", selectListener);
			pebblePanel.add(p);
			pebblePanel.add(Box.createHorizontalStrut(8));
		}

		// 2. Define Actions
		leftAction = new AbstractAction("←") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapLeft()) {
					PebbleLetter old = selectedPebble;
					old.swapLeft();
					// implements letter-tracking selection
					old.setSelected(false);
					old.getLeftNeighbor().setSelected(true);
					// arrow-enablement will be updated via the selection listener
				}
			}
		};

		rightAction = new AbstractAction("→") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapRight()) {
					PebbleLetter old = selectedPebble;
					old.swapRight();
					old.setSelected(false);
					old.getRightNeighbor().setSelected(true);
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
		SwingUtilities.invokeLater(() -> {
			pebbles.get(0).setSelected(true);
			updateActions();
		});
	}

	/* this is the subscriber callback: 
	 * notice how LetterSwapGame is tightly coupled with PebbleLetter
	 * but PebbleLetter is agnostic of LetterSwapGame   */
	private void onPebbleSelected(PropertyChangeEvent evt) {
		PebbleLetter source = (PebbleLetter) evt.getSource();
		if (selectedPebble != null && selectedPebble != source) {
			selectedPebble.setSelected(false);  // implements toggle-like selection
		}
		// updates selectedPebble
		selectedPebble = source;
		updateActions();

	}

	private void updateActions() {
		boolean canL = selectedPebble != null && selectedPebble.canSwapLeft();
		boolean canR = selectedPebble != null && selectedPebble.canSwapRight();
		leftAction.setEnabled(canL);
		rightAction.setEnabled(canR);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new LetterSwapGame("NABNAA".toCharArray()).setVisible(true);
		});
	}
}
