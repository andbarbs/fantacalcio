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
        SwingSubPlayerSelector<Goalkeeper> goalieSelectorView = 
        		new SwingSubPlayerSelector<Goalkeeper>(reducedAvailableWindow);
		PlayerSelectorPresenter<Goalkeeper> goalieSelectorPres = 
				new PlayerSelectorPresenter<>(goalieSelectorView);
		goalieSelectorView.setPresenter(goalieSelectorPres);
        selectorsPanel.getGoalie().add(goalieSelectorView);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(goalieSelectorPres), 
				List.of(new Goalkeeper("Gigi", "Buffon"), 
						new Goalkeeper("Manuel", "Neuer"),
						new Goalkeeper("Jan", "Oblak"), 
						new Goalkeeper("Alisson", "Becker")));
        
        // Defender selectors        
        SwingSubPlayerSelector<Defender> defSelector1View = 
        		new SwingSubPlayerSelector<Defender>(reducedAvailableWindow);
		PlayerSelectorPresenter<Defender> defSelector1Pres = 
				new PlayerSelectorPresenter<>(defSelector1View);
		defSelector1View.setPresenter(defSelector1Pres);
        selectorsPanel.getDef1().add(defSelector1View);
        
        SwingSubPlayerSelector<Defender> defSelector2View = 
        		new SwingSubPlayerSelector<Defender>(reducedAvailableWindow);
		PlayerSelectorPresenter<Defender> defSelector2Pres = 
				new PlayerSelectorPresenter<>(defSelector2View);
		defSelector2View.setPresenter(defSelector2Pres);
        selectorsPanel.getDef2().add(defSelector2View);
        
        SwingSubPlayerSelector<Defender> defSelector3View = 
        		new SwingSubPlayerSelector<Defender>(reducedAvailableWindow);
		PlayerSelectorPresenter<Defender> defSelector3Pres = 
				new PlayerSelectorPresenter<>(defSelector3View);
		defSelector3View.setPresenter(defSelector3Pres);
        selectorsPanel.getDef3().add(defSelector3View);
        
        SwingSubPlayerSelector<Defender> defSelector4View = 
        		new SwingSubPlayerSelector<Defender>(reducedAvailableWindow);
		PlayerSelectorPresenter<Defender> defSelector4Pres = 
				new PlayerSelectorPresenter<>(defSelector4View);
		defSelector4View.setPresenter(defSelector4Pres);
        selectorsPanel.getDef4().add(defSelector4View);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(defSelector1Pres, defSelector2Pres, defSelector3Pres, defSelector4Pres), 
				List.of(
                	    new Defender("Sergio", "Ramos"),
                	    new Defender("Virgil", "van Dijk"),
                	    new Defender("Gerard", "Piqué"),
                	    new Defender("Thiago", "Silva"),
                	    new Defender("Giorgio", "Chiellini")
                	));
        
        
        // Midfielder selectors
        SwingSubPlayerSelector<Midfielder> midSelector1View = 
        		new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow);
		PlayerSelectorPresenter<Midfielder> midSelector1Pres = 
				new PlayerSelectorPresenter<>(midSelector1View);
		midSelector1View.setPresenter(midSelector1Pres);
        selectorsPanel.getMid1().add(midSelector1View);
        
        SwingSubPlayerSelector<Midfielder> midSelector2View = 
        		new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow);
		PlayerSelectorPresenter<Midfielder> midSelector2Pres = 
				new PlayerSelectorPresenter<>(midSelector2View);
		midSelector2View.setPresenter(midSelector2Pres);
        selectorsPanel.getMid2().add(midSelector2View);
        
        SwingSubPlayerSelector<Midfielder> midSelector3View = 
        		new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow);
		PlayerSelectorPresenter<Midfielder> midSelector3Pres = 
				new PlayerSelectorPresenter<>(midSelector3View);
		midSelector3View.setPresenter(midSelector3Pres);
        selectorsPanel.getMid3().add(midSelector3View);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(midSelector1Pres, midSelector2Pres, midSelector3Pres), 
				List.of(new Midfielder("Luka", "Modrić"), 
						new Midfielder("Kevin", "De Bruyne"),
						new Midfielder("N'Golo", "Kanté"), 
						new Midfielder("Andrés", "Iniesta"),
						new Midfielder("Toni", "Kroos")));
        
        // Forward selectors
		SwingSubPlayerSelector<Forward> forwSelector1View = 
				new SwingSubPlayerSelector<Forward>(reducedAvailableWindow);
		PlayerSelectorPresenter<Forward> forwSelector1Pres = 
				new PlayerSelectorPresenter<>(forwSelector1View);
		forwSelector1View.setPresenter(forwSelector1Pres);
        selectorsPanel.getForw1().add(forwSelector1View);
        
        SwingSubPlayerSelector<Forward> forwSelector2View = 
        		new SwingSubPlayerSelector<Forward>(reducedAvailableWindow);
		PlayerSelectorPresenter<Forward> forwSelector2Pres = 
				new PlayerSelectorPresenter<>(forwSelector2View);
		forwSelector2View.setPresenter(forwSelector2Pres);
        selectorsPanel.getForw2().add(forwSelector2View);

        SwingSubPlayerSelector<Forward> forwSelector3View = 
        		new SwingSubPlayerSelector<Forward>(reducedAvailableWindow);
		PlayerSelectorPresenter<Forward> forwSelector3Pres = 
				new PlayerSelectorPresenter<>(forwSelector3View);
		forwSelector3View.setPresenter(forwSelector3Pres);
        selectorsPanel.getForw3().add(forwSelector3View);
        
        OptionDealerGroupDriver.initializeDealing(
				Set.of(forwSelector1Pres, forwSelector2Pres, forwSelector3Pres), 
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
