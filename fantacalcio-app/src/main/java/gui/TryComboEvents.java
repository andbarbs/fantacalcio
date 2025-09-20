package gui;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSpinner;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TryComboEvents extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField textField;

	/**
	 * Create the panel.
	 */
	public TryComboEvents() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		String[] items = {"A", "B", "C"};
		comboBox.setModel(new DefaultComboBoxModel<String>(items));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(comboBox, gbc_comboBox);
		comboBox.setSelectedIndex(-1);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("inside combo listener");
			}
		});
		
		JSpinner selectionSpinner = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 0;
		gbc_spinner_1.gridy = 1;
		add(selectionSpinner, gbc_spinner_1);
		
		JButton btnSetComboSelection = new JButton("set combo selection");
		btnSetComboSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				comboBox.setSelectedIndex((int) selectionSpinner.getValue());
			}
		});
		GridBagConstraints gbc_btnRemoveComboEntry_1 = new GridBagConstraints();
		gbc_btnRemoveComboEntry_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveComboEntry_1.gridx = 1;
		gbc_btnRemoveComboEntry_1.gridy = 1;
		add(btnSetComboSelection, gbc_btnRemoveComboEntry_1);
		
		
		JSpinner deletionSpinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 0;
		gbc_spinner.gridy = 2;
		add(deletionSpinner, gbc_spinner);
		
		JButton btnRemoveComboEntry = new JButton("remove combo entry");
		btnRemoveComboEntry.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int choice = (int) deletionSpinner.getValue();
				System.out.printf("about to remove option %d from combo\n", choice);
				
				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
				model.removeElementAt(choice);  
			}
		});
		GridBagConstraints gbc_btnRemoveComboEntry = new GridBagConstraints();
		gbc_btnRemoveComboEntry.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemoveComboEntry.gridx = 1;
		gbc_btnRemoveComboEntry.gridy = 2;
		add(btnRemoveComboEntry, gbc_btnRemoveComboEntry);
		
		JButton btnAddComboEntry = new JButton("add combo entry");
		btnAddComboEntry.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String text = textField.getText();
				System.out.printf("about to add option %s combo\n", text);
				
				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
				model.addElement(text);
			}
		});
		GridBagConstraints gbc_btnAddComboEntry = new GridBagConstraints();
		gbc_btnAddComboEntry.gridx = 1;
		gbc_btnAddComboEntry.gridy = 3;
		add(btnAddComboEntry, gbc_btnAddComboEntry);

		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				btnAddComboEntry.setEnabled(!textField.getText().isEmpty());
			}
		});
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 3;
		add(textField, gbc_textField);
		textField.setColumns(10);
		

	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("Test Frame");
		f.setContentPane(new TryComboEvents());
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
