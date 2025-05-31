package swingViews;

import javax.swing.*;

import java.awt.Component;
import java.awt.event.*;

public class playerSelector extends JFrame {
    
    private static final long serialVersionUID = 1L;

	public playerSelector() {
        // Configure the frame
        setTitle("Football Player Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 150);
        setLocationRelativeTo(null); // Center on screen

        // Create the combo box with a placeholder and player options
        String[] players = {"Messi", "Ronaldo", "Yamal"};
        JComboBox<String> comboBox = new JComboBox<>(players);
        comboBox.setBounds(27, 5, 175, 27);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1 && value == null) {
                    label.setText("");
                } else {
                    label.setText(value == null ? "" : value.toString());
                }
                return label;
            }
        });
        comboBox.setSelectedIndex(-1);

        
        // Create the reset button and disable it initially
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(207, 5, 67, 27);
        resetButton.setEnabled(false);

        // Add an ActionListener to the combo box to enable/disable the button
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Enable button only if a valid player (non-placeholder) is selected
            	resetButton.setEnabled(comboBox.getSelectedIndex() > -1);
            }
        });

        // Add an ActionListener to the button to reset the combo box selection
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBox.setSelectedIndex(-1);  // Reset to the placeholder
                resetButton.setEnabled(false); // Disable button again
            }
        });

        // Add components to the frame using a simple panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.add(comboBox);
        panel.add(resetButton);
        getContentPane().add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        // Start the GUI in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new playerSelector());
    }
}
