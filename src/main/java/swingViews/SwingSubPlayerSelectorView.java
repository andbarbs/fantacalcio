package swingViews;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import domainModel.Player;
import swingViews.SubstitutePlayerSelectorPresenter.SubstitutePlayerSelectorView;

@SuppressWarnings("serial")
public class SwingSubPlayerSelectorView<P extends Player> extends JPanel implements SubstitutePlayerSelectorView<P> {

	// path to the pngs for the Icons
	private static final String FIGURE_PNG_PATH = "/gui_images/player_figure_120x225.png";
	private static final String HEAD_PNG_PATH = "/gui_images/ronaldo_head_120x225.png";

	private JComboBox<P> comboBox;
	private JLabel figureLabel;
	private JButton resetButton;
	private JLabel headLabel;
	
	// WB-compatible constructor
	public SwingSubPlayerSelectorView() {
			initializeFromIcon(new ImageIcon(getClass().getResource(FIGURE_PNG_PATH)),
					new ImageIcon(getClass().getResource(HEAD_PNG_PATH)));
		}

	// rescaling-augmented constructor available to clients
	public SwingSubPlayerSelectorView(Dimension availableWindow) throws IOException {
			// 1. Load original images
			BufferedImage origFigure = ImageIO.read(getClass().getResourceAsStream(FIGURE_PNG_PATH));
			BufferedImage origHead = ImageIO.read(getClass().getResourceAsStream(HEAD_PNG_PATH));

			// 2. Compute target width & height, preserving original aspect ratio
			int ow = origFigure.getWidth(), oh = origFigure.getHeight();
			double scale = Math.min(availableWindow.width / (double) ow, availableWindow.height / (double) oh);
			int tw = (int) (ow * scale), th = (int) (oh * scale);

			// 3. Invoke Icon initializer using scaled instances
			initializeFromIcon(
					new ImageIcon(origFigure.getScaledInstance(tw, th, Image.SCALE_SMOOTH)),
					new ImageIcon(origHead.getScaledInstance(tw, th, Image.SCALE_SMOOTH)));
		}

	private void initializeFromIcon(ImageIcon figureIcon, ImageIcon headIcon) {
		setBackground(Color.RED);

		// sets the CompetingPlayerSelector Panel's own GridBagLayout
		GridBagLayout rootGBL = new GridBagLayout();
		rootGBL.columnWidths = new int[] { 1, 0 };
		rootGBL.rowHeights = new int[] { 1, 0 };
		rootGBL.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		rootGBL.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(rootGBL);

		// sets up the JLayeredPane overlaying labels and controls
		JLayeredPane layeredPane = new JLayeredPane();
		GridBagConstraints gbc_layeredPane = new GridBagConstraints();
		gbc_layeredPane.fill = GridBagConstraints.BOTH;
		gbc_layeredPane.gridx = 0;
		gbc_layeredPane.gridy = 0;
		add(layeredPane, gbc_layeredPane);
		GridBagLayout layeredPaneGBL = new GridBagLayout();
		layeredPaneGBL.columnWidths = new int[] { 1, 0 };
		layeredPaneGBL.rowHeights = new int[] { 1, 0 };
		layeredPaneGBL.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		layeredPaneGBL.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		layeredPane.setLayout(layeredPaneGBL);

		figureLabel = new JLabel("");
		figureLabel.setIcon(figureIcon);
		GridBagConstraints gbc_figureLabel = new GridBagConstraints();
		gbc_figureLabel.anchor = GridBagConstraints.NORTH;
		gbc_figureLabel.gridx = 0;
		gbc_figureLabel.gridy = 0;
		layeredPane.add(figureLabel, gbc_figureLabel);

		headLabel = new JLabel("");
		layeredPane.setLayer(headLabel, 1);
		GridBagConstraints gbc_headLabel = new GridBagConstraints();
		gbc_headLabel.anchor = GridBagConstraints.NORTH;
		gbc_headLabel.gridx = 0;
		gbc_headLabel.gridy = 0;
		layeredPane.add(headLabel, gbc_headLabel);
		headLabel.setIcon(headIcon);

		JPanel panel = new JPanel();
		layeredPane.setLayer(panel, 2);
		panel.setOpaque(false);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		layeredPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 27, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 5.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		comboBox = new JComboBox<P>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.SOUTH;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		panel.add(comboBox, gbc_comboBox);
		comboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
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

		resetButton = new JButton("Reset");
		resetButton.setEnabled(false);
		GridBagConstraints gbc_resetButton = new GridBagConstraints();
		gbc_resetButton.anchor = GridBagConstraints.NORTH;
		gbc_resetButton.gridx = 0;
		gbc_resetButton.gridy = 1;
		panel.add(resetButton, gbc_resetButton);
		
		addListeners();
	}
	
	private PlayerSelectorPresenter<P> presenter;

	public void setPresenter(PlayerSelectorPresenter<P> presenter) {
		this.presenter = presenter;
	}
	
	public PlayerSelectorPresenter<P> getPresenter() {
		return presenter;
	}
	
	private void addListeners() {
		comboBox.addActionListener(e -> {
			// combo -> button interaction
			resetButton.setEnabled(comboBox.getSelectedIndex() > -1);
			
			if (comboBox.isPopupVisible() && 
					comboBox.getSelectedIndex() > -1) {
				presenter.selectedOption(comboBox.getSelectedIndex());
			}
		});
		
		resetButton.addActionListener(e -> {
			// button -> combo interaction
			comboBox.setSelectedIndex(-1);
			resetButton.setEnabled(false);
			
			presenter.selectionCleared();
		});
	}

	@Override
	public void initOptions(List<P> options) {
		comboBox.setModel(  				// fills combo with initial contents
				new DefaultComboBoxModel<>(new Vector<>(options)));
		comboBox.setSelectedIndex(-1);      // must be re-done after setModel
	}

	@Override
	public void removeOptionAt(int pos) {
		DefaultComboBoxModel<P> model = (DefaultComboBoxModel<P>) comboBox.getModel();
		model.removeElementAt(pos);    // fires an event under conditions 3) and 4)
	}

	@Override
	public void insertOptionAt(P option, int insertionIndex) {
		DefaultComboBoxModel<P> model = (DefaultComboBoxModel<P>) comboBox.getModel();
		model.insertElementAt(option, insertionIndex);
	}

	@Override
	public void selectOptionAt(int pos) {
		comboBox.setSelectedIndex(pos);
	}

	@Override
	public void highlight() {
		setBorder(new LineBorder(Color.CYAN, 5));
	}

	@Override
	public void dehighlight() {
		setBorder(null);
	}

	@Override
	public void setControlsEnabled(boolean b) {
		List.of(comboBox, headLabel, figureLabel).forEach(t -> t.setEnabled(b));
	}
}
