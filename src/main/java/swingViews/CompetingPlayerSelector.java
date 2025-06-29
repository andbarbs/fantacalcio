package swingViews;

import javax.swing.*;
import java.awt.*;
import domainModel.Player;

public class CompetingPlayerSelector extends JPanel {
	private static final long serialVersionUID = 1L;

	public static Dimension SWING_PLAYER_SELECTOR_DIM = new Dimension(141, 230);
	private CompetingComboBox<Player> comboBox;

	public CompetingPlayerSelector() {
		setBackground(Color.RED);

		// Set a fixed preferred and minimum size for this functional unit.
		// These values (240x30) are just an example.
		setPreferredSize(new Dimension(141, 230));
		setMinimumSize(SWING_PLAYER_SELECTOR_DIM);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 0 };
		gridBagLayout.rowHeights = new int[] { 1, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLayeredPane layeredPane = new JLayeredPane();
		GridBagConstraints gbc_layeredPane = new GridBagConstraints();
		gbc_layeredPane.fill = GridBagConstraints.BOTH;
		gbc_layeredPane.gridx = 0;
		gbc_layeredPane.gridy = 0;
		add(layeredPane, gbc_layeredPane);

		JLabel figureLabel = new JLabel("");
		figureLabel.setIcon(new ImageIcon(getClass().getResource("/images/player_figure_120x225.png")));
		figureLabel.setBounds(12, 0, 120, 225);
		layeredPane.add(figureLabel);

		JLabel headLabel = new JLabel("");
		layeredPane.setLayer(headLabel, 1);
		headLabel.setBounds(12, 0, 120, 225);
		layeredPane.add(headLabel);
		headLabel.setIcon(new ImageIcon(getClass().getResource("/images/ronaldo_head_120x225.png")));

		JPanel panel = new JPanel();
		layeredPane.setLayer(panel, 2);
		panel.setOpaque(false);
		panel.setBounds(0, 0, 143, 231);
		layeredPane.add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 27, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		comboBox = new CompetingComboBox<Player>();
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

		JButton resetButton = new JButton("Reset");
		resetButton.setEnabled(false);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.gridx = 0;
		gbc_resetButton.gridy = 1;
		panel.add(resetButton, gbc_resetButton);

		// implements Combo Box -> Button interaction
		comboBox.addActionListener(e -> resetButton.setEnabled(comboBox.getSelectedIndex() > -1));

		// implements Button -> Combo Box interaction
		resetButton.addActionListener(e -> {
			getCompetingComboBox().setSelectedIndex(-1);
			resetButton.setEnabled(false);
		});
	}

	public CompetingComboBox<Player> getCompetingComboBox() {
		return comboBox;
	}
}
