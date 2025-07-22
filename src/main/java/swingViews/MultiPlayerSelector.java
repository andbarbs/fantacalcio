package swingViews;

import javax.swing.*;

import domainModel.Player.*;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class MultiPlayerSelector extends JFrame {
	private static final long serialVersionUID = 1L;	

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
        JLabel background = new JLabel(new ImageIcon(getClass().getResource("/gui_images/raster_field.png")));
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
        

        // Goalkeeper selectors
        SwingSubPlayerSelectorView<Goalkeeper> goalieSelView = new SwingSubPlayerSelectorView<Goalkeeper>();
		PlayerSelectorPresenter<Goalkeeper> goalieSelPres = 
				new PlayerSelectorPresenter<>(goalieSelView);
		goalieSelView.setPresenter(goalieSelPres);
		
        // Create and add selectors with updated GridBagConstraints:
        GridBagConstraints gbc_goalieSelector = new GridBagConstraints();
        gbc_goalieSelector.gridx = 0;
        gbc_goalieSelector.gridy = 0;
        gbc_goalieSelector.fill = GridBagConstraints.NONE;   // or BOTH if you want them to expand
        gbc_goalieSelector.anchor = GridBagConstraints.CENTER;
        gbc_goalieSelector.weightx = 1.0;   // allow horizontal growth
        gbc_goalieSelector.weighty = 1.0;   // allow vertical growth
        gbc_goalieSelector.insets = new Insets(10, 10, 10, 10); // optional padding
        selectorsPanel.add(goalieSelView, gbc_goalieSelector);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(goalieSelPres), 
				List.of(new Goalkeeper("Gigi", "Buffon"), 
						new Goalkeeper("Manuel", "Neuer"),
						new Goalkeeper("Jan", "Oblak"), 
						new Goalkeeper("Alisson", "Becker")));
        
        // Defender selectors
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
        gbl_defendersPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_defendersPanel.rowHeights = new int[]{0, 0, 0};
        gbl_defendersPanel.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_defendersPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        defendersPanel.setLayout(gbl_defendersPanel);        
        
        SwingSubPlayerSelectorView<Defender> defSelector1View = new SwingSubPlayerSelectorView<Defender>();
		PlayerSelectorPresenter<Defender> defSelector1Pres = 
				new PlayerSelectorPresenter<>(defSelector1View);
		defSelector1View.setPresenter(defSelector1Pres);
        GridBagConstraints gbc_defSelector1 = new GridBagConstraints();
        gbc_defSelector1.gridwidth = 2;
        gbc_defSelector1.insets = new Insets(0, 0, 5, 5);
        gbc_defSelector1.gridx = 1;
        gbc_defSelector1.gridy = 0;
        defendersPanel.add(defSelector1View, gbc_defSelector1);
        
        SwingSubPlayerSelectorView<Defender> defSelector2View = new SwingSubPlayerSelectorView<Defender>();
		PlayerSelectorPresenter<Defender> defSelector2Pres = 
				new PlayerSelectorPresenter<>(defSelector2View);
		defSelector2View.setPresenter(defSelector2Pres);
        GridBagConstraints gbc_defSelector2 = new GridBagConstraints();
        gbc_defSelector2.gridwidth = 2;
        gbc_defSelector2.insets = new Insets(0, 0, 5, 5);
        gbc_defSelector2.gridx = 3;
        gbc_defSelector2.gridy = 0;
        defendersPanel.add(defSelector2View, gbc_defSelector2);
        
        SwingSubPlayerSelectorView<Defender> defSelector3View = new SwingSubPlayerSelectorView<Defender>();
		PlayerSelectorPresenter<Defender> defSelector3Pres = 
				new PlayerSelectorPresenter<>(defSelector3View);
		defSelector3View.setPresenter(defSelector3Pres);
        GridBagConstraints gbc_defSelector3 = new GridBagConstraints();
        gbc_defSelector3.gridwidth = 2;
        gbc_defSelector3.insets = new Insets(0, 0, 0, 5);
        gbc_defSelector3.gridx = 0;
        gbc_defSelector3.gridy = 1;
        defendersPanel.add(defSelector3View, gbc_defSelector3);
        
        SwingSubPlayerSelectorView<Defender> defSelector4View = new SwingSubPlayerSelectorView<Defender>();
		PlayerSelectorPresenter<Defender> defSelector4Pres = 
				new PlayerSelectorPresenter<>(defSelector4View);
		defSelector4View.setPresenter(defSelector4Pres);
        GridBagConstraints gbc_defSelector4 = new GridBagConstraints();
        gbc_defSelector4.gridwidth = 2;
        gbc_defSelector4.gridx = 4;
        gbc_defSelector4.gridy = 1;
        defendersPanel.add(defSelector4View, gbc_defSelector4);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(defSelector1Pres, defSelector2Pres, defSelector3Pres, defSelector4Pres), 
				List.of(
						new Defender("Giorgio", "Chiellini"),
                	    new Defender("Gerard", "Piqué"),
                	    new Defender("Sergio", "Ramos"),
                	    new Defender("Thiago", "Silva"),
                	    new Defender("Virgil", "van Dijk")
                	));
        
        
        // Midfielder selectors
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
        
        SwingSubPlayerSelectorView<Midfielder> midSelector1View = new SwingSubPlayerSelectorView<Midfielder>();
		PlayerSelectorPresenter<Midfielder> midSelector1Pres = 
				new PlayerSelectorPresenter<>(midSelector1View);
		midSelector1View.setPresenter(midSelector1Pres);
        GridBagConstraints gbc_midSelector1 = new GridBagConstraints();
        gbc_midSelector1.insets = new Insets(0, 0, 0, 5);
        gbc_midSelector1.gridx = 0;
        gbc_midSelector1.gridy = 0;
        midfieldersPanel.add(midSelector1View, gbc_midSelector1);
        
        SwingSubPlayerSelectorView<Midfielder> midSelector2View = new SwingSubPlayerSelectorView<Midfielder>();
		PlayerSelectorPresenter<Midfielder> midSelector2Pres = 
				new PlayerSelectorPresenter<>(midSelector2View);
		midSelector2View.setPresenter(midSelector2Pres);
        GridBagConstraints gbc_midSelector2 = new GridBagConstraints();
        gbc_midSelector2.insets = new Insets(0, 0, 0, 5);
        gbc_midSelector2.gridx = 1;
        gbc_midSelector2.gridy = 0;
        midfieldersPanel.add(midSelector2View, gbc_midSelector2);
        
        SwingSubPlayerSelectorView<Midfielder> midSelector3View = new SwingSubPlayerSelectorView<Midfielder>();
		PlayerSelectorPresenter<Midfielder> midSelector3Pres = 
				new PlayerSelectorPresenter<>(midSelector3View);
		midSelector3View.setPresenter(midSelector3Pres);
        GridBagConstraints gbc_midSelector3 = new GridBagConstraints();
        gbc_midSelector3.gridx = 2;
        gbc_midSelector3.gridy = 0;
        midfieldersPanel.add(midSelector3View, gbc_midSelector3);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(midSelector1Pres, midSelector2Pres, midSelector3Pres), 
				List.of(new Midfielder("Luka", "Modrić"), 
						new Midfielder("Kevin", "De Bruyne"),
						new Midfielder("N'Golo", "Kanté"), 
						new Midfielder("Andrés", "Iniesta"),
						new Midfielder("Toni", "Kroos")));
        
        // Forwards selector
        JPanel forwardsPanel = new JPanel();
        forwardsPanel.setOpaque(false);
        GridBagConstraints gbc_forwardsPanel = new GridBagConstraints();
        gbc_forwardsPanel.fill = GridBagConstraints.BOTH;
        gbc_forwardsPanel.gridx = 0;
        gbc_forwardsPanel.gridy = 3;
        selectorsPanel.add(forwardsPanel, gbc_forwardsPanel);
        GridBagLayout gbl_forwardsPanel = new GridBagLayout();
        gbl_forwardsPanel.columnWidths = new int[]{0, 0, 0, 0};
        gbl_forwardsPanel.rowHeights = new int[]{0, 0, 0, 0};
        gbl_forwardsPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        gbl_forwardsPanel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        forwardsPanel.setLayout(gbl_forwardsPanel);
        
        SwingSubPlayerSelectorView<Forward> forwSelector1View = new SwingSubPlayerSelectorView<Forward>();
		PlayerSelectorPresenter<Forward> forwSelector1Pres = 
				new PlayerSelectorPresenter<>(forwSelector1View);
		forwSelector1View.setPresenter(forwSelector1Pres);
        GridBagConstraints gbc_forwSelector1 = new GridBagConstraints();
        gbc_forwSelector1.gridheight = 2;
        gbc_forwSelector1.insets = new Insets(0, 0, 5, 5);
        gbc_forwSelector1.gridx = 0;
        gbc_forwSelector1.gridy = 0;
        forwardsPanel.add(forwSelector1View, gbc_forwSelector1);
        
        SwingSubPlayerSelectorView<Forward> forwSelector2View = new SwingSubPlayerSelectorView<Forward>();
		PlayerSelectorPresenter<Forward> forwSelector2Pres = 
				new PlayerSelectorPresenter<>(forwSelector2View);
		forwSelector2View.setPresenter(forwSelector2Pres);
        GridBagConstraints gbc_forwSelector2 = new GridBagConstraints();
        gbc_forwSelector2.gridheight = 2;
        gbc_forwSelector2.insets = new Insets(0, 0, 0, 5);
        gbc_forwSelector2.gridx = 1;
        gbc_forwSelector2.gridy = 1;
        forwardsPanel.add(forwSelector2View, gbc_forwSelector2);


        SwingSubPlayerSelectorView<Forward> forwSelector3View = new SwingSubPlayerSelectorView<Forward>();
		PlayerSelectorPresenter<Forward> forwSelector3Pres = 
				new PlayerSelectorPresenter<>(forwSelector3View);
		forwSelector3View.setPresenter(forwSelector3Pres);
        GridBagConstraints gbc_forwSelector3 = new GridBagConstraints();
        gbc_forwSelector3.gridheight = 2;
        gbc_forwSelector3.insets = new Insets(0, 0, 5, 0);
        gbc_forwSelector3.gridx = 2;
        gbc_forwSelector3.gridy = 0;
        forwardsPanel.add(forwSelector3View, gbc_forwSelector3);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(forwSelector1Pres, forwSelector2Pres, forwSelector3Pres), 
				List.of(new Forward("Lionel", "Messi"), 
						new Forward("Cristiano", "Ronaldo"),
						new Forward("Neymar", "Jr"), 
						new Forward("Kylian", "Mbappé"),
						new Forward("Robert", "Lewandowski")));
        
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
