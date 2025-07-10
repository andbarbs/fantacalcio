package swingViews;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import domainModel.Player;

@SuppressWarnings("serial")
public class CompetingPlayerSelector<T extends Player> extends JPanel {

	// path to the pngs for the Icons
	private static final String FIGURE_PNG_PATH = "/gui_images/player_figure_120x225.png";
	private static final String HEAD_PNG_PATH = "/gui_images/ronaldo_head_120x225.png";

	protected CompetingComboBox<T> comboBox;
	private JButton resetButton;
	protected JLabel figureLabel;

	// WB-compatible constructor
	public CompetingPlayerSelector() {
		initializeFromIcon(new ImageIcon(getClass().getResource(FIGURE_PNG_PATH)),
				new ImageIcon(getClass().getResource(HEAD_PNG_PATH)));
	}

	private void initializeFromIcon(ImageIcon figureIcon, ImageIcon headIcon) {
		setBackground(Color.RED);

		// sets the CompetingPlayerSelector Panel's own GridBagLayout
		GridBagLayout rootGBL = new GridBagLayout();
		rootGBL.columnWidths = new int[] { 1, 0 };
		rootGBL.rowHeights = new int[] { 1, 0 };
		rootGBL.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		rootGBL.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(rootGBL);

		// sets up the JLayeredPane overlaying labels and controls
		JLayeredPane layeredPane = new JLayeredPane();
		GridBagConstraints gbc_layeredPane = new GridBagConstraints();
		gbc_layeredPane.fill = GridBagConstraints.BOTH;
		gbc_layeredPane.gridx = 0;
		gbc_layeredPane.gridy = 0;
		add(layeredPane, gbc_layeredPane);
		GridBagLayout layeredPaneGBL = new GridBagLayout();
		layeredPaneGBL.columnWidths = new int[] { 1, 0 };
		layeredPaneGBL.rowHeights = new int[] { 1, 0 };
		layeredPaneGBL.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		layeredPaneGBL.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		layeredPane.setLayout(layeredPaneGBL);

		figureLabel = new JLabel("");
		figureLabel.setIcon(figureIcon);
		GridBagConstraints gbc_figureLabel = new GridBagConstraints();
		gbc_figureLabel.anchor = GridBagConstraints.NORTH;
		gbc_figureLabel.gridx = 0;
		gbc_figureLabel.gridy = 0;
		layeredPane.add(figureLabel, gbc_figureLabel);

		JLabel headLabel = new JLabel("");
		layeredPane.setLayer(headLabel, 1);
		GridBagConstraints gbc_headLabel = new GridBagConstraints();
		gbc_headLabel.anchor = GridBagConstraints.NORTH;
		gbc_headLabel.gridx = 0;
		gbc_headLabel.gridy = 0;
		layeredPane.add(headLabel, gbc_headLabel);
		headLabel.setIcon(headIcon);

		JPanel panel = new JPanel();
		layeredPane.setLayer(panel, 2);
		panel.setOpaque(false);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		layeredPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 27, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 5.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		comboBox = new CompetingComboBox<T>();
		comboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				// For the field display (index == -1) and null value, just show an empty string
				if (index == -1 && value == null)
					label.setText("");
				else {
					Player p = (Player) value;
					label.setText(p.getName() + " " + p.getSurname());
				}

				return label;
			}
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.SOUTH;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		panel.add(comboBox, gbc_comboBox);

		resetButton = new JButton("Reset");
		getResetButton().setEnabled(false);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.NORTH;
		gbc_resetButton.gridx = 0;
		gbc_resetButton.gridy = 1;
		panel.add(resetButton, gbc_resetButton);

		// implements Combo Box -> Button interaction
		comboBox.addActionListener(e -> onSelectionSet());

		// implements Button -> Combo Box interaction
		resetButton.addActionListener(e -> {
			clearSelection();
		});
	}

	// rescaling-augmented constructor available to clients
	public CompetingPlayerSelector(Dimension availableWindow) throws IOException {
		// 1. Load original images
		BufferedImage origFigure = ImageIO.read(getClass().getResourceAsStream(FIGURE_PNG_PATH));
		BufferedImage origHead = ImageIO.read(getClass().getResourceAsStream(HEAD_PNG_PATH));

		// 2. Compute target width & height, preserving original aspect ratio
		int ow = origFigure.getWidth(), oh = origFigure.getHeight();
		double scale = Math.min(availableWindow.width / (double) ow, availableWindow.height / (double) oh);
		int tw = (int) (ow * scale), th = (int) (oh * scale);

		// 3. Invoke Icon initializer using scaled instances
		initializeFromIcon(
				new ImageIcon(origFigure.getScaledInstance(tw, th, Image.SCALE_SMOOTH)),
				new ImageIcon(origHead.getScaledInstance(tw, th, Image.SCALE_SMOOTH)));
	}

	public CompetingComboBox<T> getCompetingComboBox() {
		return comboBox;
	}

	public JButton getResetButton() {
		return resetButton;
	}
	
	protected void onSelectionSet() {
		resetButton.setEnabled(comboBox.getSelectedIndex() > -1);
	}
	
	protected void clearSelection() {
		comboBox.setSelectedIndex(-1);
		resetButton.setEnabled(false);
	}
}
