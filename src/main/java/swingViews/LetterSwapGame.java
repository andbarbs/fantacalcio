package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

		for (PebbleLetter p : pebbles) {
			p.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					// implements out-of-the-blue pebble selection
			        if (!p.isSelected()) {
			        	LetterSwapGame.this.passSelectionTo(p);
			        }
				}
			});
			pebblePanel.add(p);
			pebblePanel.add(Box.createHorizontalStrut(8));
		}

		// 2. Define Actions
		leftAction = new AbstractAction("←") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapLeft()) {
					selectedPebble.swapLeft();
					// implements letter-tracking selection
					passSelectionTo(selectedPebble.getLeftNeighbor());
					// arrow-enablement will be updated via the selection listener
				}
			}
		};

		rightAction = new AbstractAction("→") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapRight()) {
					selectedPebble.swapRight();
					passSelectionTo(selectedPebble.getRightNeighbor());
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
			passSelectionTo(pebbles.get(0));
			updateActions();
		});
	}

	// 'passes' the selection on to a different pebble
	private void passSelectionTo(PebbleLetter recipient) {
		// implements toggle-like selection
		if (selectedPebble != null) {   // is null at initialization!
			selectedPebble.setSelected(false);  
		}
		recipient.setSelected(true);		
		selectedPebble = recipient;  // updates selectedPebble
		updateActions();

	}

	private void updateActions() {
		leftAction.setEnabled(selectedPebble != null && selectedPebble.canSwapLeft());
		rightAction.setEnabled(selectedPebble != null && selectedPebble.canSwapRight());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new LetterSwapGame("INLAPTESA".toCharArray()).setVisible(true);
		});
	}
}
