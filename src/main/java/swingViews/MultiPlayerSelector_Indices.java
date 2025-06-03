package swingViews;

import javax.swing.*;

import domainModel.Player;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiPlayerSelector_Indices extends JFrame {
	private static final long serialVersionUID = 1L;

	// The global pool of players
	private final List<Player> allPlayers = List.of(
			new Player.Forward("Lionel", "Messi"), 
			new Player.Forward("Cristiano", "Ronaldo"), 
			new Player.Goalkeeper("Gigi", "Buffon"));


	private class CompetingComboState {
		Integer currentSelection;
		List<Integer> mask;
		SwingPlayerSelector playerSelector;
		
		void evictFromComboBox(Integer toBeEvicted) {
			// operates only on boxes where toBeEvicted is not selected
			if (currentSelection != toBeEvicted) {
				int pos = mask.indexOf(toBeEvicted);
				mask.remove(pos);
				DefaultComboBoxModel<Player> model = 
						(DefaultComboBoxModel<Player>)playerSelector.getComboBox().getModel();
				model.removeElementAt(pos);
			}
		}
		
		void insertIntoComboBox(Integer indexToBeInserted, Player playerToBeInserted) {
			// operates only on boxes from which toBeInserted is missing
			if (!mask.contains(indexToBeInserted)) {
				int insertionIndex = IntStream.range(0, mask.size())
						.filter(k -> mask.get(k) >= indexToBeInserted)
						.findFirst().orElse(mask.size());
				mask.add(insertionIndex, indexToBeInserted);
				DefaultComboBoxModel<Player> model = 
						(DefaultComboBoxModel<Player>)playerSelector.getComboBox().getModel();
				model.insertElementAt(playerToBeInserted, insertionIndex);
			}
		}

		CompetingComboState(SwingPlayerSelector playerSelector) {
			// creates current cbox mask
			this.currentSelection = -1;
			this.mask = new ArrayList<Integer>(IntStream.rangeClosed(1, allPlayers.size()).boxed().collect(Collectors.toList()));

			// Create a combo box and immediately update its model
			this.playerSelector = playerSelector;	
			JComboBox<Player> cbox = this.playerSelector.getComboBox();
			JButton resetButton = this.playerSelector.getResetButton();
			
			// fills the cbox with initial contents
			cbox.setModel(new DefaultComboBoxModel<>(new Vector<>(allPlayers)));
			cbox.setSelectedIndex(-1);

			// bookkeeping for user-triggered actions on the combo box
			cbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					// a selection exists
					if (cbox.getSelectedIndex() > -1) { 
						
						// a previous selection also existed
						if (currentSelection != -1) { 
							for (CompetingComboState compState : compStates) {
								compState.insertIntoComboBox(currentSelection, allPlayers.get(currentSelection - 1));
							}
						}
						
						// registers the current selection
						currentSelection = mask.get(cbox.getSelectedIndex());
						
						// removes current selection from competing cboxes
						for (int i = 0; i < compStates.size(); i++) {
							System.out.printf("about to call evict(%d) with i = %d\n", currentSelection, i);
							compStates.get(i).evictFromComboBox(currentSelection);
						}
					}
				}
			});

			// bookkeeping for user interactions with the reset button
			resetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// adds cleared selection back into competing cboxes
					for (CompetingComboState compState : compStates) {
						compState.insertIntoComboBox(currentSelection, allPlayers.get(currentSelection - 1));
					}
					
					// clears the bookkeeping current selection
					currentSelection = -1;
				}
			});
		}
	}

	private List<CompetingComboState> compStates = new ArrayList<>(3);

	public MultiPlayerSelector_Indices() {
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

        // Create and add selectors with updated GridBagConstraints:
        SwingPlayerSelector selector1 = new SwingPlayerSelector();
        compStates.add(new CompetingComboState(selector1));
        GridBagConstraints gbc_selector1 = new GridBagConstraints();
        gbc_selector1.gridx = 0;
        gbc_selector1.gridy = 0;
        gbc_selector1.fill = GridBagConstraints.NONE;   // or BOTH if you want them to expand
        gbc_selector1.anchor = GridBagConstraints.CENTER;
        gbc_selector1.weightx = 1.0;   // allow horizontal growth
        gbc_selector1.weighty = 1.0;   // allow vertical growth
        gbc_selector1.insets = new Insets(10, 10, 10, 10); // optional padding
        selectorsPanel.add(selector1, gbc_selector1);

        SwingPlayerSelector selector2 = new SwingPlayerSelector();
        compStates.add(new CompetingComboState(selector2));
        GridBagConstraints gbc_selector2 = new GridBagConstraints();
        gbc_selector2.gridx = 1;
        gbc_selector2.gridy = 0;
        gbc_selector2.fill = GridBagConstraints.NONE;
        gbc_selector2.anchor = GridBagConstraints.CENTER;
        gbc_selector2.weightx = 1.0;
        gbc_selector2.weighty = 1.0;
        gbc_selector2.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector2, gbc_selector2);

        SwingPlayerSelector selector3 = new SwingPlayerSelector();
        compStates.add(new CompetingComboState(selector3));
        GridBagConstraints gbc_selector3 = new GridBagConstraints();
        gbc_selector3.gridx = 0;
        gbc_selector3.gridy = 1;
        gbc_selector3.fill = GridBagConstraints.NONE;
        gbc_selector3.anchor = GridBagConstraints.CENTER;
        gbc_selector3.weightx = 1.0;
        gbc_selector3.weighty = 1.0;
        gbc_selector3.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector3, gbc_selector3);

        SwingPlayerSelector selector4 = new SwingPlayerSelector();
        compStates.add(new CompetingComboState(selector4));
        GridBagConstraints gbc_selector4 = new GridBagConstraints();
        gbc_selector4.gridx = 1;
        gbc_selector4.gridy = 1;
        gbc_selector4.fill = GridBagConstraints.NONE;
        gbc_selector4.anchor = GridBagConstraints.CENTER;
        gbc_selector4.weightx = 1.0;
        gbc_selector4.weighty = 1.0;
        gbc_selector4.insets = new Insets(10, 10, 10, 10);
        selectorsPanel.add(selector4, gbc_selector4);

        
        layeredPane.add(selectorsPanel, Integer.valueOf(1)); // higher layer over background
        // Finally add the layeredPane to the frame.
        getContentPane().add(layeredPane);
        
        // Pack and display.
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiPlayerSelector_Indices());
    }
}
