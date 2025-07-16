package swingViews;

import javax.swing.*;

import domainModel.Player.*;

import java.awt.*;
import java.beans.Beans;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class MultiPlayerSelector_usingSingleScheme extends JFrame {

	public MultiPlayerSelector_usingSingleScheme() throws IOException {
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
        JLabel background = new JLabel(new ImageIcon(MultiPlayerSelector_usingSingleScheme.class.getResource("/gui_images/raster_field.png")));
		GridBagConstraints gbc_background = new GridBagConstraints();
		gbc_background.fill = GridBagConstraints.BOTH;
		gbc_background.gridx = 0;
		gbc_background.gridy = 0;
		layeredPane.add(background, gbc_background);
		layeredPane.setLayer(background, 0);
        
        // Create a panel to hold our SwingPlayerSelector components.
		Spring433Scheme selectorsPanel = new Spring433Scheme(Beans.isDesignTime());
        selectorsPanel.setBorder(new LineBorder(new Color(255, 0, 0), 8));
		GridBagConstraints gbc_selectorsPanel = new GridBagConstraints();
		gbc_selectorsPanel.fill = GridBagConstraints.BOTH;
		gbc_selectorsPanel.anchor = GridBagConstraints.NORTH;
		gbc_selectorsPanel.gridx = 0;
		gbc_selectorsPanel.gridy = 0;
		layeredPane.add(selectorsPanel, gbc_selectorsPanel);
		layeredPane.setLayer(selectorsPanel, 1);
		
		// derive rescaling hint for CompetingPlayerSelectors under current scheme panel
		Dimension availableWindow = Spring433Scheme.recommendedSlotDimensions(background.getPreferredSize());
		Dimension reducedAvailableWindow = // ensures selectors actually fit inside slots after insets
				new Dimension(availableWindow.width-5, availableWindow.height-10);

        // Goalkeeper selectors
        StarterPlayerSelector<Goalkeeper> goalieSelector = new StarterPlayerSelector<Goalkeeper>(reducedAvailableWindow);
        selectorsPanel.getGoalie().add(goalieSelector);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(goalieSelector), 
				List.of(new Goalkeeper("Gigi", "Buffon"), 
						new Goalkeeper("Manuel", "Neuer"),
						new Goalkeeper("Jan", "Oblak"), 
						new Goalkeeper("Alisson", "Becker")));
        
        // Defender selectors        
        StarterPlayerSelector<Defender> defSelector1 = new StarterPlayerSelector<Defender>(reducedAvailableWindow);
        selectorsPanel.getDef1().add(defSelector1);
        
        StarterPlayerSelector<Defender> defSelector2 = new StarterPlayerSelector<Defender>(reducedAvailableWindow);
        selectorsPanel.getDef2().add(defSelector2);
        
        StarterPlayerSelector<Defender> defSelector3 = new StarterPlayerSelector<Defender>(reducedAvailableWindow);
        selectorsPanel.getDef3().add(defSelector3);
        
        StarterPlayerSelector<Defender> defSelector4 = new StarterPlayerSelector<Defender>(reducedAvailableWindow);
        selectorsPanel.getDef4().add(defSelector4);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(defSelector1, defSelector2, defSelector3, defSelector4), 
				List.of(
                	    new Defender("Sergio", "Ramos"),
                	    new Defender("Virgil", "van Dijk"),
                	    new Defender("Gerard", "Piqué"),
                	    new Defender("Thiago", "Silva"),
                	    new Defender("Giorgio", "Chiellini")
                	));
        
        
        // Midfielder selectors
        StarterPlayerSelector<Midfielder> midSelector1 = new StarterPlayerSelector<Midfielder>(reducedAvailableWindow);
        selectorsPanel.getMid1().add(midSelector1);
        
        StarterPlayerSelector<Midfielder> midSelector2 = new StarterPlayerSelector<Midfielder>(reducedAvailableWindow);
        selectorsPanel.getMid2().add(midSelector2);
        
        StarterPlayerSelector<Midfielder> midSelector3 = new StarterPlayerSelector<Midfielder>(reducedAvailableWindow);
        selectorsPanel.getMid3().add(midSelector3);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(midSelector1, midSelector2, midSelector3), 
				List.of(new Midfielder("Luka", "Modrić"), 
						new Midfielder("Kevin", "De Bruyne"),
						new Midfielder("N'Golo", "Kanté"), 
						new Midfielder("Andrés", "Iniesta"),
						new Midfielder("Toni", "Kroos")));
        
        // Forward selectors
		StarterPlayerSelector<Forward> forwSelector1 = new StarterPlayerSelector<Forward>(reducedAvailableWindow);
        selectorsPanel.getForw1().add(forwSelector1);
        
        StarterPlayerSelector<Forward> forwSelector2 = new StarterPlayerSelector<Forward>(reducedAvailableWindow);
        selectorsPanel.getForw2().add(forwSelector2);

        StarterPlayerSelector<Forward> forwSelector3 = new StarterPlayerSelector<Forward>(reducedAvailableWindow);
        selectorsPanel.getForw3().add(forwSelector3);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(forwSelector1, forwSelector2, forwSelector3), 
				List.of(new Forward("Lionel", "Messi"), 
						new Forward("Cristiano", "Ronaldo"),
						new Forward("Neymar", "Jr"), 
						new Forward("Kylian", "Mbappé"),
						new Forward("Robert", "Lewandowski")));       
		
		// Finally add the layeredPane to the frame.
        getContentPane().add(layeredPane);
        
        // Pack and display.
        pack();
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
			try {
				new MultiPlayerSelector_usingSingleScheme();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    }
}
