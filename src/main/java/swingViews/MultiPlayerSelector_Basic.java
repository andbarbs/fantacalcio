package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MultiPlayerSelector_Basic extends JFrame {

    private static final long serialVersionUID = 1L;

    // The global pool of players
    private final String[] allPlayers = {"Messi", "Ronaldo", "Yamal"};
    // A list to hold all our combo boxes' models so that we can update them together.
    private final List<DefaultComboBoxModel<String>> comboBoxModels = new ArrayList<>();
    
    private List<String> availablePlayers = new ArrayList<String>(List.of(allPlayers));

    public MultiPlayerSelector_Basic() {
        // Configure the frame
        setTitle("Football Player Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 200);
        setLocationRelativeTo(null);
        // Use a null layout for simplicity (you might also use layout managers)
        JPanel panel = new JPanel();
        panel.setLayout(null);
        
        // Create three instances of (combo box + reset button)
        for (int i = 0; i < 3; i++) {
            int ycoord = 10 + i * 40;
            
            // Create a combo box and immediately update its model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(allPlayers);
            JComboBox<String> combo = new JComboBox<>();
            combo.setModel(model);
            combo.setBounds(27, ycoord, 175, 27);
            // Set our custom renderer (same as before, but here with no placeholder text)
            combo.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    // For the field display (index == -1) and null value, just show an empty string.
                    if (index == -1 && value == null) {
                        label.setText("");
                    } else {
                        label.setText(value == null ? "" : value.toString());
                    }
                    return label;
                }
            });
            // Start with no selection
            combo.setSelectedIndex(-1);
            // Add the combo box to our global tracking list.
            comboBoxModels.add(model);
            panel.add(combo);
            
            
            
            // Create the reset button for this combo
            JButton resetButton = new JButton("Reset");
            resetButton.setBounds(207, ycoord, 67, 27);
            resetButton.setEnabled(false);
            panel.add(resetButton);

            // Add an ActionListener to the combo box:
            // Every time a selection occurs, update the associated reset button and update all combo models.
            combo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Enable reset button if a selection exists
					if (combo.getSelectedIndex() > -1) {
						if (resetButton.isEnabled()) {
							String oldSelection = null;
							for (int j = 0; j < combo.getModel().getSize(); j++) {
								if (!availablePlayers.contains(combo.getModel().getElementAt(j))) {
										oldSelection = combo.getModel().getElementAt(j);
										break;
								}
							}
							availablePlayers.add(oldSelection);
							for (DefaultComboBoxModel<String> model : comboBoxModels) {
								if (model != combo.getModel()) {
									model.addElement(oldSelection);
								}
							}
						}
						resetButton.setEnabled(true);
						availablePlayers.remove(combo.getSelectedItem());
							for (DefaultComboBoxModel<String> model : comboBoxModels) {
								if (model != combo.getModel()) {
									model.removeElement(combo.getSelectedItem());
								}
							}
					}
                }
            });

            // The reset button simply clears the selection and updates the models.
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	String oldSelection = (String) combo.getSelectedItem();
                    combo.setSelectedIndex(-1);
                    resetButton.setEnabled(false);
//                    updateAllComboModels();
                    for (DefaultComboBoxModel<String> model : comboBoxModels) {
                    	if (model != combo.getModel()) {
                    		model.addElement(oldSelection);
                    	}                    		
					}
                }
            });
        }

        getContentPane().add(panel);
        setVisible(true);
        // Ensure initial update so every combo has full options
//        updateAllComboModels();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiPlayerSelector_Basic());
    }
}
