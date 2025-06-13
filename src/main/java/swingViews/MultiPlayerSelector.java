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
        selectorsPanel.setLayout(gbl_selectorsPanel);

        // Give the panel a preferred size so that GridBagLayout knows the space available.
        selectorsPanel.setPreferredSize(new Dimension(800, 600));
        

        CompetingComboBoxPool<Player> competingCboxes = new CompetingComboBoxPool<Player>();
        
        CompetingPlayerSelector selector1 = new CompetingPlayerSelector();
        competingCboxes.add(selector1.getComboBox());
        // Create and add selectors with updated GridBagConstraints:
        GridBagConstraints gbc_selector1 = new GridBagConstraints();
        gbc_selector1.gridx = 0;
        gbc_selector1.gridy = 0;
        gbc_selector1.fill = GridBagConstraints.NONE;   // or BOTH if you want them to expand
        gbc_selector1.anchor = GridBagConstraints.CENTER;
        gbc_selector1.weightx = 1.0;   // allow horizontal growth
        gbc_selector1.weighty = 1.0;   // allow vertical growth
        gbc_selector1.insets = new Insets(10, 10, 10, 10); // optional padding
        selectorsPanel.add(selector1, gbc_selector1);


        CompetingPlayerSelector selector2 = new CompetingPlayerSelector();
        competingCboxes.add(selector2.getComboBox());
        GridBagConstraints gbc_selector2 = new GridBagConstraints();
        gbc_selector2.gridx = 1;
        gbc_selector2.gridy = 0;
        gbc_selector2.fill = GridBagConstraints.NONE;
        gbc_selector2.anchor = GridBagConstraints.CENTER;
        gbc_selector2.weightx = 1.0;
        gbc_selector2.weighty = 1.0;
        gbc_selector2.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector2, gbc_selector2);


        CompetingPlayerSelector selector3 = new CompetingPlayerSelector();
        competingCboxes.add(selector3.getComboBox());
        GridBagConstraints gbc_selector3 = new GridBagConstraints();
        gbc_selector3.gridx = 0;
        gbc_selector3.gridy = 1;
        gbc_selector3.fill = GridBagConstraints.NONE;
        gbc_selector3.anchor = GridBagConstraints.CENTER;
        gbc_selector3.weightx = 1.0;
        gbc_selector3.weighty = 1.0;
        gbc_selector3.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector3, gbc_selector3);


        CompetingPlayerSelector selector4 = new CompetingPlayerSelector();
        competingCboxes.add(selector4.getComboBox());
        GridBagConstraints gbc_selector4 = new GridBagConstraints();
        gbc_selector4.gridx = 1;
        gbc_selector4.gridy = 1;
        gbc_selector4.fill = GridBagConstraints.NONE;
        gbc_selector4.anchor = GridBagConstraints.CENTER;
        gbc_selector4.weightx = 1.0;
        gbc_selector4.weighty = 1.0;
        gbc_selector4.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector4, gbc_selector4);
        
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
