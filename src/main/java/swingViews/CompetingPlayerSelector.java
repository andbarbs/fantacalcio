package swingViews;

import javax.swing.*;
import java.awt.*;
import domainModel.Player;

public class CompetingPlayerSelector extends JPanel {
	private static final long serialVersionUID = 1L;

	private CompetingComboBox<Player> comboBox;
	private JButton resetButton;
	
	public static Dimension SWING_PLAYER_SELECTOR_DIM = new Dimension(240, 30);

	public CompetingPlayerSelector() {
		setBackground(Color.RED);
		// Use absolute positioning so the controls keep their fixed layout.
		setLayout(null);

		// Create the combo box.
		comboBox = new CompetingComboBox<Player>();
		// For now, we’ll let the composing class set its model.
		// Position and size: adjust these as needed.
		comboBox.setBounds(12, -1, 175, 27);
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
		add(comboBox);
		
		// implements Combo Box -> Button interaction
		comboBox.addActionListener(
				e -> resetButton.setEnabled(comboBox.getSelectedIndex() > -1));

		// Create the reset button.
		resetButton = new JButton("Reset");
		resetButton.setBounds(63, 31, 80, 25);
		resetButton.setEnabled(false);
		add(resetButton);
		
		// implements Button -> Combo Box interaction
		resetButton.addActionListener(e -> {
				getComboBox().setSelectedIndex(-1);
				resetButton.setEnabled(false);
		});

		// Set a fixed preferred and minimum size for this functional unit.
		// These values (240x30) are just an example.
		setPreferredSize(new Dimension(195, 68));
		setMinimumSize(SWING_PLAYER_SELECTOR_DIM);
	}

	public CompetingComboBox<Player> getComboBox() {
		return comboBox;
	}
}
