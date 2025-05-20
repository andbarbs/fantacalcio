package swingViews;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Box;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import java.awt.GridBagConstraints;

public class provaLineUpLayoutOmini extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public provaLineUpLayoutOmini() {
		setBackground(new Color(143, 240, 164));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 274, 0, 3};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 386, 3};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JComboBox comboBox = new JComboBox();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 4;
		add(comboBox, gbc_comboBox);

	}

}
