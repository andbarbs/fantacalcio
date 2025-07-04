package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LetterSwapGame extends JFrame {
	private static final long serialVersionUID = 1L;

	// adds toggle-like & content-tracking 'selectability' to PebbleLetter
	private class SelectablePebbleLetter extends PebbleLetter {
		private static final long serialVersionUID = 1L;

		boolean selected = false;

		SelectablePebbleLetter(char letter) {
			super(letter);
		}

		@Override
		protected void pullFrom(PebbleLetter sender) {
			super.pullFrom(sender); // deals with content swapping

			// adds selection swapping (enabling content-tracking selection)
			LetterSwapGame.this.passSelectionTo(this);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth(), h = getHeight();
			Graphics2D g2 = (Graphics2D) g.create();

			// draw pebble circle
			g2.setColor(selected ? Color.ORANGE : Color.LIGHT_GRAY);
			g2.fillOval(5, 5, w - 10, h - 10);

			// draw letter
			g2.setColor(Color.BLACK);
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
			FontMetrics fm = g2.getFontMetrics();
			String s = String.valueOf(letter);
			int sw = fm.stringWidth(s);
			int sh = fm.getAscent();
			g2.drawString(s, (w - sw) / 2, (h + sh) / 2 - 4);

			g2.dispose();
		}
	}

	private SelectablePebbleLetter selectedPebble;
	private Action leftAction, rightAction; // Actions are stored instead of JButtons!

	public LetterSwapGame(String initialLetters) {
		super("LetterSwapGame");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 1. Create selectable pebbles and wire out-of-nowhere selection
		List<SelectablePebbleLetter> pebbles = initialLetters
				.chars()
				.mapToObj(c -> (char) c)
				.map(t -> new SelectablePebbleLetter(t)).collect(Collectors.toCollection(ArrayList::new));
		PebbleLetter.chain(pebbles);
		JPanel pebblePanel = new JPanel();
		pebblePanel.setLayout(new BoxLayout(pebblePanel, BoxLayout.X_AXIS));
		pebblePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		for (SelectablePebbleLetter p : pebbles) {
			p.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					// implements out-of-nowhere selection
					if (!p.selected) {
						LetterSwapGame.this.passSelectionTo(p);
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
					selectedPebble.swapLeft(); // includes content-tracking selection
				}
			}
		};

		rightAction = new AbstractAction("→") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPebble != null && selectedPebble.canSwapRight()) {
					selectedPebble.swapRight(); // includes content-tracking selection
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

	// 'passes' the selection on to the newly selected recipient pebble
	private void passSelectionTo(SelectablePebbleLetter recipient) {
		// implements toggle-like selection
		if (selectedPebble != null) { // is null at initialization!
			selectedPebble.selected = false;
			selectedPebble.repaint();
		}
		recipient.selected = true;
		recipient.repaint();
		selectedPebble = recipient; // updates selectedPebble

		// updates actions based on selectedPebble
		leftAction.setEnabled(selectedPebble != null && selectedPebble.canSwapLeft());
		rightAction.setEnabled(selectedPebble != null && selectedPebble.canSwapRight());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new LetterSwapGame("neidoac");
		});
	}
}
