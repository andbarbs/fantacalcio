package swingViews;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;

public class _433SingleGridBagPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public _433SingleGridBagPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(0, 0, 255));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridheight = 2;
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 4;
		gbc_panel_2.gridy = 2;
		add(panel_2, gbc_panel_2);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBackground(new Color(0, 0, 255));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.gridheight = 2;
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 7;
		gbc_panel_3.gridy = 2;
		add(panel_3, gbc_panel_3);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(0, 0, 255));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.gridheight = 2;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 3;
		add(panel_1, gbc_panel_1);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBackground(new Color(0, 0, 255));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 2;
		gbc_panel_4.gridheight = 2;
		gbc_panel_4.insets = new Insets(0, 0, 5, 5);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 9;
		gbc_panel_4.gridy = 3;
		add(panel_4, gbc_panel_4);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 0, 0));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 2;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 2;
		gbc_panel.gridy = 7;
		add(panel, gbc_panel);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBackground(new Color(255, 0, 0));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.gridheight = 2;
		gbc_panel_5.gridwidth = 2;
		gbc_panel_5.insets = new Insets(0, 0, 0, 5);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 5;
		gbc_panel_5.gridy = 7;
		add(panel_5, gbc_panel_5);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(255, 0, 0));
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.gridheight = 2;
		gbc_panel_6.gridwidth = 2;
		gbc_panel_6.insets = new Insets(0, 0, 0, 5);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 8;
		gbc_panel_6.gridy = 7;
		add(panel_6, gbc_panel_6);

	}

}
