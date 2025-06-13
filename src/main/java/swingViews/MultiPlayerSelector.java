package swingViews;

import javax.swing.*;

import domainModel.Player;
import swingViews.CompetingComboBox.CompetingComboBoxPool;

import java.awt.*;
import java.util.List;

public class MultiPlayerSelector extends JFrame {
	private static final long serialVersionUID = 1L;

	// The global pool of players
	private final List<Player> allPlayers = List.of(
			new Player.Forward("Lionel", "Messi"), 
			new Player.Forward("Cristiano", "Ronaldo"), 
			new Player.Goalkeeper("Gigi", "Buffon"));
	
	

	public MultiPlayerSelector() {
        setTitle("Football Player Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set a minimum size for the whole unit, as suggested.
        setMinimumSize(new Dimension(800, 600));
        
        // Create a JLayeredPane so that we can overlay components.
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 1100));
        
        // Create the background label with the football field image.
        // In WindowBuilder you can adjust the file path or resource as needed.
        JLabel background = new JLabel(new ImageIcon(getClass().getResource("/images/raster_field.png")));
        // We'll use absolute bounds for the background image.
        background.setBounds(0, 0, 788, 1100);
        layeredPane.add(background, Integer.valueOf(0)); // lowest layer
        
        // Create a panel to hold our SwingPlayerSelector components.
        // This panel will use null layout so we can position them freely.
        JPanel selectorsPanel = new JPanel();
        selectorsPanel.setBounds(81, 95, 622, 860);
        selectorsPanel.setOpaque(false); // allow background to show
        // Do not set null layout; we use GridBagLayout instead.
        GridBagLayout gbl_selectorsPanel = new GridBagLayout();
        gbl_selectorsPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0};
        gbl_selectorsPanel.columnWeights = new double[]{1.0};
        selectorsPanel.setLayout(gbl_selectorsPanel);

        // Give the panel a preferred size so that GridBagLayout knows the space available.
        selectorsPanel.setPreferredSize(new Dimension(800, 600));
        

        CompetingComboBoxPool<Player> competingCboxes = new CompetingComboBoxPool<Player>();
        
        CompetingPlayerSelector goalieSelector = new CompetingPlayerSelector();
        competingCboxes.add(goalieSelector.getComboBox());
        // Create and add selectors with updated GridBagConstraints:
        GridBagConstraints gbc_goalieSelector = new GridBagConstraints();
        gbc_goalieSelector.gridx = 0;
        gbc_goalieSelector.gridy = 0;
        gbc_goalieSelector.fill = GridBagConstraints.NONE;   // or BOTH if you want them to expand
        gbc_goalieSelector.anchor = GridBagConstraints.CENTER;
        gbc_goalieSelector.weightx = 1.0;   // allow horizontal growth
        gbc_goalieSelector.weighty = 1.0;   // allow vertical growth
        gbc_goalieSelector.insets = new Insets(10, 10, 10, 10); // optional padding
        selectorsPanel.add(goalieSelector, gbc_goalieSelector);
        
        JPanel defendersPanel = new JPanel();
        defendersPanel.setOpaque(false);
        defendersPanel.setBackground(new Color(238, 238, 238));
        GridBagConstraints gbc_defendersPanel = new GridBagConstraints();
        gbc_defendersPanel.fill = GridBagConstraints.BOTH;
        gbc_defendersPanel.insets = new Insets(0, 0, 5, 0);
        gbc_defendersPanel.gridx = 0;
        gbc_defendersPanel.gridy = 1;
        selectorsPanel.add(defendersPanel, gbc_defendersPanel);
        GridBagLayout gbl_defendersPanel = new GridBagLayout();
        gbl_defendersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gbl_defendersPanel.rowHeights = new int[]{0, 0, 0};
        gbl_defendersPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_defendersPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        defendersPanel.setLayout(gbl_defendersPanel);
        
        CompetingPlayerSelector defSelector1 = new CompetingPlayerSelector();
        GridBagConstraints gbc_defSelector1 = new GridBagConstraints();
        gbc_defSelector1.insets = new Insets(0, 0, 5, 5);
        gbc_defSelector1.gridx = 1;
        gbc_defSelector1.gridy = 0;
        defendersPanel.add(defSelector1, gbc_defSelector1);
        
        CompetingPlayerSelector defSelector2 = new CompetingPlayerSelector();
        GridBagConstraints gbc_defSelector2 = new GridBagConstraints();
        gbc_defSelector2.insets = new Insets(0, 0, 5, 5);
        gbc_defSelector2.gridx = 2;
        gbc_defSelector2.gridy = 0;
        defendersPanel.add(defSelector2, gbc_defSelector2);
        
        CompetingPlayerSelector defSelector3 = new CompetingPlayerSelector();
        GridBagConstraints gbc_defSelector3 = new GridBagConstraints();
        gbc_defSelector3.insets = new Insets(0, 0, 0, 5);
        gbc_defSelector3.gridx = 0;
        gbc_defSelector3.gridy = 1;
        defendersPanel.add(defSelector3, gbc_defSelector3);
        
        CompetingPlayerSelector defSelector4 = new CompetingPlayerSelector();
        GridBagConstraints gbc_defSelector4 = new GridBagConstraints();
        gbc_defSelector4.gridx = 4;
        gbc_defSelector4.gridy = 1;
        defendersPanel.add(defSelector4, gbc_defSelector4);
        
        JPanel midfieldersPanel = new JPanel();
        midfieldersPanel.setOpaque(false);
        GridBagConstraints gbc_midfieldersPanel = new GridBagConstraints();
        gbc_midfieldersPanel.fill = GridBagConstraints.BOTH;
        gbc_midfieldersPanel.insets = new Insets(0, 0, 5, 0);
        gbc_midfieldersPanel.gridx = 0;
        gbc_midfieldersPanel.gridy = 2;
        selectorsPanel.add(midfieldersPanel, gbc_midfieldersPanel);
        GridBagLayout gbl_midfieldersPanel = new GridBagLayout();
        gbl_midfieldersPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_midfieldersPanel.rowHeights = new int[]{0, 0};
        gbl_midfieldersPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_midfieldersPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        midfieldersPanel.setLayout(gbl_midfieldersPanel);
        
        CompetingPlayerSelector midSelector1 = new CompetingPlayerSelector();
        GridBagConstraints gbc_midSelector1 = new GridBagConstraints();
        gbc_midSelector1.insets = new Insets(0, 0, 0, 5);
        gbc_midSelector1.fill = GridBagConstraints.HORIZONTAL;
        gbc_midSelector1.gridx = 0;
        gbc_midSelector1.gridy = 0;
        midfieldersPanel.add(midSelector1, gbc_midSelector1);
        
        CompetingPlayerSelector midSelector2 = new CompetingPlayerSelector();
        GridBagConstraints gbc_midSelector2 = new GridBagConstraints();
        gbc_midSelector2.insets = new Insets(0, 0, 0, 5);
        gbc_midSelector2.fill = GridBagConstraints.HORIZONTAL;
        gbc_midSelector2.gridx = 1;
        gbc_midSelector2.gridy = 0;
        midfieldersPanel.add(midSelector2, gbc_midSelector2);
        
        CompetingPlayerSelector midSelector3 = new CompetingPlayerSelector();
        GridBagConstraints gbc_midSelector3 = new GridBagConstraints();
        gbc_midSelector3.gridx = 2;
        gbc_midSelector3.gridy = 0;
        midfieldersPanel.add(midSelector3, gbc_midSelector3);
        
        JPanel forwardsPanel = new JPanel();
        forwardsPanel.setOpaque(false);
        GridBagConstraints gbc_forwardsPanel = new GridBagConstraints();
        gbc_forwardsPanel.fill = GridBagConstraints.BOTH;
        gbc_forwardsPanel.gridx = 0;
        gbc_forwardsPanel.gridy = 3;
        selectorsPanel.add(forwardsPanel, gbc_forwardsPanel);
        GridBagLayout gbl_forwardsPanel = new GridBagLayout();
        gbl_forwardsPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_forwardsPanel.rowHeights = new int[]{0, 0};
        gbl_forwardsPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_forwardsPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        forwardsPanel.setLayout(gbl_forwardsPanel);
        
        CompetingPlayerSelector forwSelector1 = new CompetingPlayerSelector();
        GridBagConstraints gbc_forwSelector1 = new GridBagConstraints();
        gbc_forwSelector1.insets = new Insets(0, 0, 0, 5);
        gbc_forwSelector1.fill = GridBagConstraints.HORIZONTAL;
        gbc_forwSelector1.gridx = 0;
        gbc_forwSelector1.gridy = 0;
        forwardsPanel.add(forwSelector1, gbc_forwSelector1);
        
        CompetingPlayerSelector forwSelector2 = new CompetingPlayerSelector();
        GridBagConstraints gbc_forwSelector2 = new GridBagConstraints();
        gbc_forwSelector2.insets = new Insets(0, 0, 0, 5);
        gbc_forwSelector2.fill = GridBagConstraints.HORIZONTAL;
        gbc_forwSelector2.gridx = 1;
        gbc_forwSelector2.gridy = 0;
        forwardsPanel.add(forwSelector2, gbc_forwSelector2);


        CompetingPlayerSelector forwSelector3 = new CompetingPlayerSelector();
        GridBagConstraints gbc_forwSelector3 = new GridBagConstraints();
        gbc_forwSelector3.gridx = 2;
        gbc_forwSelector3.gridy = 0;
        forwardsPanel.add(forwSelector3, gbc_forwSelector3);
        competingCboxes.add(forwSelector3.getComboBox());
        
        // uses the CompetingComboBoxPool to initialize competition
        competingCboxes.initializeCompetitionOnContents(allPlayers);

        
        layeredPane.add(selectorsPanel, Integer.valueOf(1)); // higher layer over background
        // Finally add the layeredPane to the frame.
        getContentPane().add(layeredPane);
        
        // Pack and display.
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiPlayerSelector());
    }
}
