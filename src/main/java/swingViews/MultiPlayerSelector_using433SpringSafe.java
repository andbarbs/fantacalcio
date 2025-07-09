package swingViews;

import javax.swing.*;

import domainModel.Player.*;

import static swingViews.CompetingComboBox.initializeCompetition;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.border.LineBorder;

public class MultiPlayerSelector_using433SpringSafe extends JFrame {
	private static final long serialVersionUID = 1L;	

	public MultiPlayerSelector_using433SpringSafe() throws IOException {
        setTitle("Football Player Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set a minimum size for the whole unit, as suggested.
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        // Create a JLayeredPane so that we can overlay components.
        JLayeredPane layeredPane = new JLayeredPane();
        GridBagLayout gbl_layeredPane = new GridBagLayout();
        gbl_layeredPane.rowWeights = new double[]{1.0};
        gbl_layeredPane.rowHeights = new int[]{0};
        gbl_layeredPane.columnWeights = new double[]{1.0};
        gbl_layeredPane.columnWidths = new int[]{0};
        layeredPane.setLayout(gbl_layeredPane);
//        layeredPane.setPreferredSize(new Dimension(800, 1100));
        
        // Create the background label with the football field image.
        // In WindowBuilder you can adjust the file path or resource as needed.
        JLabel background = new JLabel(new ImageIcon(MultiPlayerSelector_using433SpringSafe.class.getResource("/gui_images/raster_field.png")));
		GridBagConstraints gbc_background = new GridBagConstraints();
		gbc_background.fill = GridBagConstraints.BOTH;
		gbc_background.gridx = 0;
		gbc_background.gridy = 0;
		layeredPane.add(background, gbc_background);
		layeredPane.setLayer(background, 0);
        
        // Create a panel to hold our SwingPlayerSelector components.
        SpringSchemePanel selectorsPanel = new _433SpringSafe();
        selectorsPanel.setBorder(new LineBorder(new Color(255, 0, 0), 8));
		GridBagConstraints gbc_selectorsPanel = new GridBagConstraints();
		gbc_selectorsPanel.fill = GridBagConstraints.BOTH;
		gbc_selectorsPanel.anchor = GridBagConstraints.NORTH;
		gbc_selectorsPanel.gridx = 0;
		gbc_selectorsPanel.gridy = 0;
		layeredPane.add(selectorsPanel, gbc_selectorsPanel);
		layeredPane.setLayer(selectorsPanel, 1);
		
		// derive rescaling hint for CompetingPlayerSelectors under current scheme panel
		Dimension availableWindow = _433SpringSafe.recommendedSlotDimensions(background.getPreferredSize());
		Dimension reducedAvailableWindow = // ensures selectors actually fit inside slots after insets
				new Dimension(availableWindow.width-5, availableWindow.height-10);

        // Goalkeeper selectors
        CompetingPlayerSelector goalieSelector = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getGoalie().add(goalieSelector);
        
		initializeCompetition(
				Set.of(goalieSelector.getCompetingComboBox()),
				List.of(new Goalkeeper("Gigi", "Buffon"), 
						new Goalkeeper("Manuel", "Neuer"),
						new Goalkeeper("Jan", "Oblak"), 
						new Goalkeeper("Alisson", "Becker")));
        
        // Defender selectors        
        CompetingPlayerSelector defSelector1 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getDef1().add(defSelector1);
        
        CompetingPlayerSelector defSelector2 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getDef2().add(defSelector2);
        
        CompetingPlayerSelector defSelector3 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getDef3().add(defSelector3);
        
        CompetingPlayerSelector defSelector4 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getDef4().add(defSelector4);
        
		initializeCompetition(
				Stream.of(defSelector1, defSelector2, defSelector3, defSelector4)
                	.map(CompetingPlayerSelector::getCompetingComboBox)
                	.collect(Collectors.toSet()),
                List.of(
                	    new Defender("Sergio", "Ramos"),
                	    new Defender("Virgil", "van Dijk"),
                	    new Defender("Gerard", "Piqué"),
                	    new Defender("Thiago", "Silva"),
                	    new Defender("Giorgio", "Chiellini")
                	));
        
        
        // Midfielder selectors
        CompetingPlayerSelector midSelector1 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getMid1().add(midSelector1);
        
        CompetingPlayerSelector midSelector2 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getMid2().add(midSelector2);
        
        CompetingPlayerSelector midSelector3 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getMid3().add(midSelector3);
        
		initializeCompetition(
				Stream.of(midSelector1, midSelector2, midSelector3)
                	.map(CompetingPlayerSelector::getCompetingComboBox)
                	.collect(Collectors.toSet()),
				List.of(new Midfielder("Luka", "Modrić"), 
						new Midfielder("Kevin", "De Bruyne"),
						new Midfielder("N'Golo", "Kanté"), 
						new Midfielder("Andrés", "Iniesta"),
						new Midfielder("Toni", "Kroos")));
        
        // Forward selectors
		CompetingPlayerSelector forwSelector1 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getForw1().add(forwSelector1);
        
        CompetingPlayerSelector forwSelector2 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getForw2().add(forwSelector2);

        CompetingPlayerSelector forwSelector3 = new CompetingPlayerSelector(reducedAvailableWindow);
        selectorsPanel.getForw3().add(forwSelector3);
        
		initializeCompetition(
				Stream.of(forwSelector1, forwSelector2, forwSelector3)	
					.map(CompetingPlayerSelector::getCompetingComboBox)
					.collect(Collectors.toSet()),
				List.of(new Forward("Lionel", "Messi"), 
						new Forward("Cristiano", "Ronaldo"),
						new Forward("Neymar", "Jr"), 
						new Forward("Kylian", "Mbappé"),
						new Forward("Robert", "Lewand")));
       
		
		// Finally add the layeredPane to the frame.
        getContentPane().add(layeredPane);
        
        // Pack and display.
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
			try {
				new MultiPlayerSelector_using433SpringSafe();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    }
}
