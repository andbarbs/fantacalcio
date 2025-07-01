package swingViews;

import javax.swing.*;

import java.awt.*;

public class _433NestedGridBagPanel extends JPanel {
	private static final long serialVersionUID = 1L;	
	private JPanel goalie;
	private JPanel def1;
	private JPanel def2;
	private JPanel def4;
	private JPanel def3;
	private JPanel mid3;
	private JPanel mid2;
	private JPanel mid1;
	private JPanel forw2;
	private JPanel forw3;
	private JPanel forw1;

	public _433NestedGridBagPanel() {
		super();
        setOpaque(false); // allow background to show
        // Do not set null layout; we use GridBagLayout instead.
        GridBagLayout gbl = new GridBagLayout();
        gbl.rowHeights = new int[]{0, 0, 0, 0};
        gbl.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0};
        gbl.columnWeights = new double[]{1.0};
        setLayout(gbl);

        // Give the panel a preferred size so that GridBagLayout knows the space available.
        setPreferredSize(new Dimension(800, 600));
        

        // Goalkeeper selectors
        goalie = new JPanel();
        // Create and add selectors with updated GridBagConstraints:
        GridBagConstraints gbc_goalie = new GridBagConstraints();
        gbc_goalie.gridx = 0;
        gbc_goalie.gridy = 0;
        gbc_goalie.fill = GridBagConstraints.NONE;   // or BOTH if you want them to expand
        gbc_goalie.anchor = GridBagConstraints.CENTER;
        gbc_goalie.weightx = 1.0;   // allow horizontal growth
        gbc_goalie.weighty = 1.0;   // allow vertical growth
        gbc_goalie.insets = new Insets(10, 10, 10, 10); // optional padding
        add(goalie, gbc_goalie);
        goalie.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        // Defender selectors
        JPanel defendersPanel = new JPanel();
        defendersPanel.setOpaque(false);
        defendersPanel.setBackground(new Color(238, 238, 238));
        GridBagConstraints gbc_defendersPanel = new GridBagConstraints();
        gbc_defendersPanel.fill = GridBagConstraints.BOTH;
        gbc_defendersPanel.insets = new Insets(0, 0, 5, 0);
        gbc_defendersPanel.gridx = 0;
        gbc_defendersPanel.gridy = 1;
        add(defendersPanel, gbc_defendersPanel);
        GridBagLayout gbl_defendersPanel = new GridBagLayout();
        gbl_defendersPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_defendersPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_defendersPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_defendersPanel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        defendersPanel.setLayout(gbl_defendersPanel);        
        
        def1 = new JPanel();
        GridBagConstraints gbc_def1 = new GridBagConstraints();
        gbc_def1.gridheight = 2;
        gbc_def1.fill = GridBagConstraints.BOTH;
        gbc_def1.insets = new Insets(0, 0, 5, 5);
        gbc_def1.gridx = 1;
        gbc_def1.gridy = 0;
        defendersPanel.add(def1, gbc_def1);
        def1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        def2 = new JPanel();
        GridBagConstraints gbc_def2 = new GridBagConstraints();
        gbc_def2.gridheight = 2;
        gbc_def2.fill = GridBagConstraints.BOTH;
        gbc_def2.insets = new Insets(0, 0, 5, 5);
        gbc_def2.gridx = 2;
        gbc_def2.gridy = 0;
        defendersPanel.add(def2, gbc_def2);
        def2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        def3 = new JPanel();
        GridBagConstraints gbc_def3 = new GridBagConstraints();
        gbc_def3.gridheight = 2;
        gbc_def3.fill = GridBagConstraints.BOTH;
        gbc_def3.insets = new Insets(0, 0, 0, 5);
        gbc_def3.gridx = 0;
        gbc_def3.gridy = 1;
        defendersPanel.add(def3, gbc_def3);
        def3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        def4 = new JPanel();
        GridBagConstraints gbc_def4 = new GridBagConstraints();
        gbc_def4.gridheight = 2;
        gbc_def4.fill = GridBagConstraints.BOTH;
        gbc_def4.gridx = 3;
        gbc_def4.gridy = 1;
        defendersPanel.add(def4, gbc_def4);
        def4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        
        // Midfielder selectors
        JPanel midfieldersPanel = new JPanel();
        midfieldersPanel.setOpaque(false);
        GridBagConstraints gbc_midfieldersPanel = new GridBagConstraints();
        gbc_midfieldersPanel.fill = GridBagConstraints.BOTH;
        gbc_midfieldersPanel.insets = new Insets(0, 0, 5, 0);
        gbc_midfieldersPanel.gridx = 0;
        gbc_midfieldersPanel.gridy = 2;
        add(midfieldersPanel, gbc_midfieldersPanel);
        GridBagLayout gbl_midfieldersPanel = new GridBagLayout();
        gbl_midfieldersPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_midfieldersPanel.rowHeights = new int[]{0, 0};
        gbl_midfieldersPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_midfieldersPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        midfieldersPanel.setLayout(gbl_midfieldersPanel);
        
        mid1 = new JPanel();
        GridBagConstraints gbc_mid1 = new GridBagConstraints();
        gbc_mid1.fill = GridBagConstraints.BOTH;
        gbc_mid1.insets = new Insets(0, 0, 0, 5);
        gbc_mid1.gridx = 0;
        gbc_mid1.gridy = 0;
        midfieldersPanel.add(mid1, gbc_mid1);
        mid1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		mid2 = new JPanel();
		GridBagConstraints gbc_mid2 = new GridBagConstraints();
		gbc_mid2.fill = GridBagConstraints.BOTH;
		gbc_mid2.insets = new Insets(0, 0, 0, 5);
		gbc_mid2.gridx = 1;
		gbc_mid2.gridy = 0;
		midfieldersPanel.add(mid2, gbc_mid2);
		mid2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        mid3 = new JPanel();
        GridBagConstraints gbc_mid3 = new GridBagConstraints();
        gbc_mid3.fill = GridBagConstraints.BOTH;
        gbc_mid3.gridx = 2;
        gbc_mid3.gridy = 0;
        midfieldersPanel.add(mid3, gbc_mid3);
        mid3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        // Forwards selector
        JPanel forwardsPanel = new JPanel();
        forwardsPanel.setOpaque(false);
        GridBagConstraints gbc_forwardsPanel = new GridBagConstraints();
        gbc_forwardsPanel.fill = GridBagConstraints.BOTH;
        gbc_forwardsPanel.gridx = 0;
        gbc_forwardsPanel.gridy = 3;
        add(forwardsPanel, gbc_forwardsPanel);
        GridBagLayout gbl_forwardsPanel = new GridBagLayout();
        gbl_forwardsPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_forwardsPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_forwardsPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_forwardsPanel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        forwardsPanel.setLayout(gbl_forwardsPanel);
        
        forw1 = new JPanel();
        GridBagConstraints gbc_forw1 = new GridBagConstraints();
        gbc_forw1.fill = GridBagConstraints.BOTH;
        gbc_forw1.gridheight = 2;
        gbc_forw1.insets = new Insets(0, 0, 0, 5);
        gbc_forw1.gridx = 0;
        gbc_forw1.gridy = 0;
        forwardsPanel.add(forw1, gbc_forw1);
        forw1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		forw2 = new JPanel();
		GridBagConstraints gbc_forw2 = new GridBagConstraints();
		gbc_forw2.fill = GridBagConstraints.BOTH;
		gbc_forw2.gridheight = 2;
		gbc_forw2.gridx = 2;
		gbc_forw2.gridy = 0;
		forwardsPanel.add(forw2, gbc_forw2);
		forw2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		forw3 = new JPanel();
		GridBagConstraints gbc_forw3 = new GridBagConstraints();
		gbc_forw3.gridheight = 2;
		gbc_forw3.fill = GridBagConstraints.BOTH;
		gbc_forw3.insets = new Insets(0, 0, 0, 5);
		gbc_forw3.gridx = 1;
		gbc_forw3.gridy = 1;
		forwardsPanel.add(forw3, gbc_forw3);
		forw3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new _433NestedGridBagPanel());
    }
	public JPanel getGoalie() {
		return goalie;
	}
	public JPanel getDef1() {
		return def1;
	}
	public JPanel getDef2() {
		return def2;
	}
	public JPanel getDef4() {
		return def4;
	}
	public JPanel getDef3() {
		return def3;
	}
	public JPanel getMid3() {
		return mid3;
	}
	public JPanel getMid2() {
		return mid2;
	}
	public JPanel getMid1() {
		return mid1;
	}
	public JPanel getForw2() {
		return forw2;
	}
	public JPanel getForw3() {
		return forw3;
	}
	public JPanel getForw1() {
		return forw1;
	}
}
