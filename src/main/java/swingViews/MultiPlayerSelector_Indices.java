package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiPlayerSelector_Indices extends JFrame {

    private static final long serialVersionUID = 1L;

    // The global pool of players
    private final String[] allPlayers = {"Messi", "Ronaldo", "Yamal"};    
    private List<String> availablePlayers = new ArrayList<String>(List.of(allPlayers));
    
    private List<JComboBox<String>> cboxes = new ArrayList<>(3);
    
    private List<List<Integer>> masks = new ArrayList<>(3);
    

    public MultiPlayerSelector_Indices() {
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
            
            // creates current cbox mask
            masks.add(i, IntStream.rangeClosed(1, allPlayers.length).boxed().collect(Collectors.toList()));
            
            // Create a combo box and immediately update its model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(allPlayers);
            cboxes.add(i, new JComboBox<>(model));            
            JComboBox<String> combo = cboxes.get(i);
            
            // Sets up the current cbox
            combo.setBounds(27, ycoord, 175, 27);
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
					if (combo.getSelectedIndex() > -1) {   // a selection exists						
						if (resetButton.isEnabled()) {     // a previous selection also existed
							int pos = 0;
							for (; pos < combo.getModel().getSize(); pos++) {
								if (!availablePlayers.contains(combo.getModel().getElementAt(pos)))
									break;
							}
							availablePlayers.add(combo.getModel().getElementAt(pos)); // reinserts old
							Integer prevSelection = Arrays.asList(allPlayers).indexOf(combo.getModel().getElementAt(pos)) + 1;
							for (int i = 0; i < 3; i++) {
								if (cboxes.get(i) != combo) {
									List<Integer> mask = masks.get(i);
									int insertionIndex = IntStream.range(0, mask.size())
							                .filter(k -> mask.get(k) >= prevSelection)
							                .findFirst()
							                .orElse(mask.size());
									mask.add(insertionIndex, prevSelection);
									DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cboxes.get(i).getModel();
									model.insertElementAt(allPlayers[prevSelection-1], insertionIndex);
								}
							}
						}
						resetButton.setEnabled(true);
						Integer curSelection = Arrays.asList(allPlayers).indexOf(combo.getSelectedItem()) + 1;
						availablePlayers.remove(combo.getSelectedItem());
						
						for (int i = 0; i < 3; i++) {
							if (cboxes.get(i) != combo) {
								List<Integer> mask = masks.get(i);
								mask.remove(curSelection);
								DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cboxes.get(i).getModel();
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
                	int pos = combo.getSelectedIndex();
                    combo.setSelectedIndex(-1);
                    resetButton.setEnabled(false);
//                    updateAllComboModels();
                    
                    availablePlayers.add(combo.getModel().getElementAt(pos)); // reinserts old
					Integer prevSelection = Arrays.asList(allPlayers).indexOf(combo.getModel().getElementAt(pos)) + 1;
					for (int i = 0; i < 3; i++) {
						if (cboxes.get(i) != combo) {
							List<Integer> mask = masks.get(i);
							int insertionIndex = IntStream.range(0, mask.size())
					                .filter(k -> mask.get(k) >= prevSelection)
					                .findFirst()
					                .orElse(mask.size());
							mask.add(insertionIndex, prevSelection);
							DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cboxes.get(i).getModel();
							model.insertElementAt(allPlayers[prevSelection-1], insertionIndex);
						}
					}
                }
            });
        }

        getContentPane().add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiPlayerSelector_Indices());
    }
}
