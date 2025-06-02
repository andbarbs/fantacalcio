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
		JComboBox<Player> cbox;
		JButton resetButton;
		
		void evictFromComboBox(Integer toBeEvicted) {
			if (currentSelection != toBeEvicted) {
				int pos = mask.indexOf(toBeEvicted);
				mask.remove(toBeEvicted);
				DefaultComboBoxModel<Player> model = (DefaultComboBoxModel<Player>)cbox.getModel();
				model.removeElementAt(pos);
			}
		}
		void insertIntoComboBox(Integer indexToBeInserted, Player playerToBeInserted) {
			if (!mask.contains(indexToBeInserted)) {
				int insertionIndex = IntStream.range(0, mask.size())
						.filter(k -> mask.get(k) >= indexToBeInserted)
						.findFirst().orElse(mask.size());
				mask.add(insertionIndex, indexToBeInserted);
				DefaultComboBoxModel<Player> model = (DefaultComboBoxModel<Player>)
						cbox.getModel();
				model.insertElementAt(playerToBeInserted, insertionIndex);
			}
		}

		CompetingComboState(int ycoord) {
			// creates current cbox mask
			this.currentSelection = -1;
			this.mask = new ArrayList<Integer>(IntStream.rangeClosed(1, allPlayers.size()).boxed().collect(Collectors.toList()));

			// Create a combo box and immediately update its model
			this.cbox = new JComboBox<Player>(new DefaultComboBoxModel<>(new Vector<>(allPlayers)));

			// Sets up the current cbox
			this.cbox.setBounds(27, ycoord, 175, 27);
			this.cbox.setRenderer(new DefaultListCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
					// For the field display (index == -1) and null value, just show an empty string
					if (index == -1 && value == null)
						label.setText("");
					else {
						Player p = (Player) value;
						label.setText(p.getName() + " " + p.getSurname());
					}

					return label;
				}
			});
			// Start with no selection
			this.cbox.setSelectedIndex(-1);

			// Create the reset button for this combo
			this.resetButton = new JButton("Reset");
			this.resetButton.setBounds(207, ycoord, 67, 27);
			this.resetButton.setEnabled(false);			

			// Add an ActionListener to the combo box:
			// Every time a selection occurs, update the associated reset button and update
			// all combo models.
			this.cbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					// a selection exists
					if (cbox.getSelectedIndex() > -1) { 
						
						// a previous selection also existed
						if (resetButton.isEnabled()) { 
							for (int i = 0; i < 3; i++) {
								compStates.get(i).insertIntoComboBox(currentSelection, allPlayers.get(currentSelection - 1));
							}
						}
						
						// registers the current selection
						currentSelection = mask.get(cbox.getSelectedIndex());
						resetButton.setEnabled(true);

						// removes current selection from competing cboxes
						for (int i = 0; i < 3; i++) {
							compStates.get(i).evictFromComboBox(currentSelection);
						}
					}
				}
			});

			// The reset button simply clears the selection and updates the models.
			this.resetButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int clearedSelection = currentSelection;
					currentSelection = -1;
					cbox.setSelectedIndex(-1);
					resetButton.setEnabled(false);

					// adds erased selection back into competing cboxes
					for (int i = 0; i < 3; i++) {
						compStates.get(i).insertIntoComboBox(clearedSelection, allPlayers.get(clearedSelection - 1));
					}
				}
			});
		}
	}

	private List<CompetingComboState> compStates = new ArrayList<>(3);

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
			CompetingComboState compState = new CompetingComboState(ycoord);
			compStates.add(compState);

			panel.add(compState.cbox);
			panel.add(compState.resetButton);
		}

		getContentPane().add(panel);
		setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MultiPlayerSelector_Indices());
	}
}
