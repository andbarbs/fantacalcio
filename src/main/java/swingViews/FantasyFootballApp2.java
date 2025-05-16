package swingViews;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.util.*;

public class FantasyFootballApp2 extends JFrame {
	 private JComboBox<String> moduleSelection;
	    private JPanel pitchPanel; 
	    private JPanel benchContainerPanel;
	    private JButton saveFormationButton;

	    // Data structure for available players by role.
	    private Map<String, List<String>> availablePlayers = new HashMap<>();

	    // The 11 mandatory pitch placeholders for the chosen formation.
	    private FieldPlaceholder[] pitchPlaceholders = new FieldPlaceholder[11];

	    public FantasyFootballApp2() {
	        setTitle("Fantasy Football App");
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setSize(900, 700);
	        setLayout(new BorderLayout());

	        // Initialize available players (for demonstration purposes, some sample names).
	        availablePlayers.put("Goalkeeper", new ArrayList<>(Arrays.asList("Gigi Buffon", "Manuel Neuer")));
	        availablePlayers.put("Defender", new ArrayList<>(Arrays.asList("Sergio Ramos", "Virgil van Dijk", "Gerard Pique", "Thiago Silva", "Marcelo")));
	        availablePlayers.put("Midfielder", new ArrayList<>(Arrays.asList("Xavi", "Iniesta", "Luka Modric", "Kevin De Bruyne")));
	        availablePlayers.put("Forward", new ArrayList<>(Arrays.asList("Cristiano Ronaldo", "Lionel Messi", "Neymar", "Kylian Mbappe")));

	        // --------------------------------------------------------------------
	        // 1) Top panel: Formation/module selection.
	        JPanel topPanel = new JPanel();
	        topPanel.add(new JLabel("Select formation: "));
	        moduleSelection = new JComboBox<>(new String[] {"433", "452", "442", "4141"});
	        moduleSelection.addActionListener(e -> {
	            if (!"433".equals(moduleSelection.getSelectedItem())) {
	                JOptionPane.showMessageDialog(FantasyFootballApp2.this,
	                        "Only the 433 formation is supported in this demo.");
	                moduleSelection.setSelectedItem("433");
	            }
	        });
	        topPanel.add(moduleSelection);
	        add(topPanel, BorderLayout.NORTH);

	        // --------------------------------------------------------------------
	        // 2) Center panel: The pitch with 11 mandatory placeholders.
	        // We use a null layout to position our custom-painted placeholders.
	        pitchPanel = new JPanel(null);
	        pitchPanel.setPreferredSize(new Dimension(600, 450));
	        pitchPanel.setBackground(new Color(34, 139, 34)); // dark-green pitch
	        addPitchPlaceholders();
	        add(pitchPanel, BorderLayout.CENTER);

	        // --------------------------------------------------------------------
	        // 5) South panel: (Benchmarking bench area omitted for brevity) and Save Formation button.
	        JPanel southPanel = new JPanel(new BorderLayout());
	        // In this demo, we only add the Save Formation button.
	        saveFormationButton = new JButton("Save Formation");
	        saveFormationButton.setEnabled(false);
	        southPanel.add(saveFormationButton, BorderLayout.SOUTH);
	        add(southPanel, BorderLayout.SOUTH);

	        setVisible(true);
	    }

	    // --------------------------------------------------------------------
	    // Create the 11 pitch placeholders for a 433 formation using custom drawing.
	    private void addPitchPlaceholders() {
	        int placeholderWidth = 80, placeholderHeight = 100;
	        // For simplicity, positions are hardcoded for each row group.
	        // Forwards (indexes 0-2).
	        int baseX = 50;
	        int gapF = 120;
	        int yForwards = 40;
	        pitchPlaceholders[0] = new FieldPlaceholder("Forward", baseX, yForwards, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[1] = new FieldPlaceholder("Forward", baseX + gapF, yForwards, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[2] = new FieldPlaceholder("Forward", baseX + 2 * gapF, yForwards, placeholderWidth, placeholderHeight);

	        // Midfielders (indexes 3-5).
	        int yMid = 160;
	        pitchPlaceholders[3] = new FieldPlaceholder("Midfielder", baseX, yMid, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[4] = new FieldPlaceholder("Midfielder", baseX + gapF, yMid, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[5] = new FieldPlaceholder("Midfielder", baseX + 2 * gapF, yMid, placeholderWidth, placeholderHeight);

	        // Defenders (indexes 6-9).
	        int yDef = 280;
	        int gapD = 100;
	        pitchPlaceholders[6] = new FieldPlaceholder("Defender", baseX, yDef, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[7] = new FieldPlaceholder("Defender", baseX + gapD, yDef, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[8] = new FieldPlaceholder("Defender", baseX + 2 * gapD, yDef, placeholderWidth, placeholderHeight);
	        pitchPlaceholders[9] = new FieldPlaceholder("Defender", baseX + 3 * gapD, yDef, placeholderWidth, placeholderHeight);

	        // Goalkeeper (index 10).
	        int yGK = 400;
	        int xGK = 150;
	        pitchPlaceholders[10] = new FieldPlaceholder("Goalkeeper", xGK, yGK, placeholderWidth, placeholderHeight);

	        for (FieldPlaceholder fp : pitchPlaceholders) {
	            pitchPanel.add(fp);
	        }
	    }

	    // Updates the Save Formation button. It becomes enabled only if every mandatory placeholder is assigned.
	    private void updateSaveButton() {
	        boolean allAssigned = true;
	        for (FieldPlaceholder fp : pitchPlaceholders) {
	            if (!fp.isAssigned()) {
	                allAssigned = false;
	                break;
	            }
	        }
	        saveFormationButton.setEnabled(allAssigned);
	    }

	    // --------------------------------------------------------------------
	    // Inner class FieldPlaceholder.
	    // This custom component draws a human-figure–style placeholder:
	    // - In the unassigned state, it shows a grey “profile” (a circle for the head and an arc for the shoulders)
	    //   with the role abbreviation.
	    // - When a player is selected, it stores the player name and repaints in blue.
	    // - Clicking an unassigned placeholder brings up an inline combo box below it to pick the player.
	    // - For an assigned placeholder, hovering shows a little “x” button to cancel the selection.
	    private class FieldPlaceholder extends JPanel {
	        private String role;
	        private String assignedPlayer = null;
	        private JButton removeButton;
	        private boolean forBench = false; // making a distinction for bench items (not used in this demo)

	        public FieldPlaceholder(String role, int x, int y, int width, int height) {
	            this.role = role;
	            setBounds(x, y, width, height);
	            // We use non-opaque so that our custom paint handles full rendering.
	            setOpaque(false);
	            setPreferredSize(new Dimension(width, height));

	            // Remove button to cancel an assignment
	            removeButton = new JButton("x");
	            removeButton.setMargin(new Insets(0, 0, 0, 0));
	            removeButton.setBounds(width - 20, 0, 20, 20);
	            removeButton.setVisible(false);
	            removeButton.addActionListener(e -> removeAssignment());
	            setLayout(null);
	            add(removeButton);

	            addMouseListener(new MouseAdapter() {
	                @Override
	                public void mouseClicked(MouseEvent e) {
	                    if (!isAssigned()) {
	                        // Instead of a pop-up dialog, show an inline combo box.
	                        selectPlayerInline();
	                    }
	                }
	                @Override
	                public void mouseEntered(MouseEvent e) {
	                    if (isAssigned()) {
	                        removeButton.setVisible(true);
	                    }
	                }
	                @Override
	                public void mouseExited(MouseEvent e) {
	                    removeButton.setVisible(false);
	                }
	            });
	        }

	        // Returns true if a player has been assigned.
	        public boolean isAssigned() {
	            return assignedPlayer != null;
	        }

	        // Sets the assigned player, updates the formation save button state, and repaints.
	        public void setAssignedPlayer(String player) {
	            this.assignedPlayer = player;
	            updateSaveButton();
	            repaint();
	        }

	        // Returns the abbreviated role to display when unassigned.
	        private String getRoleAbbreviation() {
	            switch (role) {
	                case "Goalkeeper": return "GK";
	                case "Defender": return "DF";
	                case "Midfielder": return "MF";
	                case "Forward": return "FW";
	                default: return role;
	            }
	        }

	        // Brings up an inline combo box to select a player.
	        private void selectPlayerInline() {
	            List<String> avail = availablePlayers.get(role);
	            if (avail == null || avail.isEmpty()) {
	                JOptionPane.showMessageDialog(FantasyFootballApp2.this, "No available " + role + " players.");
	                return;
	            }
	            JComboBox<String> comboBox = new JComboBox<>(avail.toArray(new String[0]));
	            comboBox.setBounds(0, getHeight() - 25, getWidth(), 25);
	            // Remove any previous combo box instance.
	            for (Component comp : getComponents()) {
	                if (comp instanceof JComboBox) {
	                    remove(comp);
	                }
	            }
	            add(comboBox);
	            revalidate();
	            repaint();
	            comboBox.addActionListener(e -> {
	                String selected = (String) comboBox.getSelectedItem();
	                if (selected != null) {
	                    avail.remove(selected);
	                    setAssignedPlayer(selected);
	                    remove(comboBox);
	                    revalidate();
	                    repaint();
	                }
	            });
	        }

	        // Removes the current player assignment.
	        private void removeAssignment() {
	            if (assignedPlayer != null) {
	                availablePlayers.get(role).add(assignedPlayer);
	                assignedPlayer = null;
	                updateSaveButton();
	                repaint();
	            }
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            // Enable antialiasing.
	            Graphics2D g2 = (Graphics2D) g;
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            
	            int w = getWidth(), h = getHeight();
	            // Choose fill color based on assignment.
	            Color fillColor = isAssigned() ? Color.BLUE : Color.GRAY;
	            g2.setColor(fillColor);

	            // Draw a "head" as a circle in the upper center.
	            int headDiameter = w / 2;
	            int headX = (w - headDiameter) / 2;
	            int headY = 5;
	            g2.fillOval(headX, headY, headDiameter, headDiameter);

	            // Draw the "shoulders" as an arc starting just below the head.
	            int arcY = headY + headDiameter - 5;
	            int arcHeight = h - arcY - 5;
	            // The arc covers twice the arcHeight to give a semi-circular appearance.
	            g2.fillArc(0, arcY, w, arcHeight * 2, 0, -180);

	            // Draw an outline around the head and shoulders.
	            g2.setColor(Color.BLACK);
	            g2.drawOval(headX, headY, headDiameter, headDiameter);
	            g2.drawArc(0, arcY, w, arcHeight * 2, 0, -180);

	            // Draw text in the center: either the player's name or the role abbreviation.
	            g2.setColor(Color.WHITE);
	            String text = isAssigned() ? assignedPlayer : getRoleAbbreviation();
	            FontMetrics fm = g2.getFontMetrics();
	            int textWidth = fm.stringWidth(text);
	            int textX = (w - textWidth) / 2;
	            int textY = h / 2 + fm.getAscent() / 2;
	            g2.drawString(text, textX, textY);
	        }
	    }

	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> new FantasyFootballApp());
	    }
}

