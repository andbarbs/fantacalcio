package swingViews;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.*;

public class FantasyFootballApp extends JFrame {
    private static final long serialVersionUID = 1L;
	private JComboBox<String> moduleSelection;
    private JPanel pitchPanel; 
    private JPanel benchContainerPanel;
    private JButton saveFormationButton;

    // Bench container panels for each role
    private JPanel benchDefendersPanel;
    private JPanel benchMidfieldersPanel;
    private JPanel benchForwardsPanel;

    // Data structure for available players by role.
    private Map<String, List<String>> availablePlayers = new HashMap<>();

    // The 11 mandatory pitch placeholders for the chosen formation.
    private FieldPlaceholder[] pitchPlaceholders = new FieldPlaceholder[11];

    public FantasyFootballApp() {
        setTitle("Fantasy Football App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());

        // Initialize available players. (For simplicity we use a few examples per role.)
        availablePlayers.put("Goalkeeper", new ArrayList<>(Arrays.asList("Gigi Buffon", "Manuel Neuer")));
        availablePlayers.put("Defender", new ArrayList<>(Arrays.asList("Sergio Ramos", "Virgil van Dijk", "Gerard Pique", "Thiago Silva", "Marcelo")));
        availablePlayers.put("Midfielder", new ArrayList<>(Arrays.asList("Xavi", "Iniesta", "Luka Modric", "Kevin De Bruyne")));
        availablePlayers.put("Forward", new ArrayList<>(Arrays.asList("Cristiano Ronaldo", "Lionel Messi", "Neymar", "Kylian Mbappe")));

        // --------------------------------------------------------------------
        // 1) Top panel: Formation/Module selection.
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select formation: "));
        moduleSelection = new JComboBox<>(new String[] {"433", "452", "442", "4141"});
        moduleSelection.addActionListener(e -> {
            // For now we only support 433.
            if (!"433".equals(moduleSelection.getSelectedItem())) {
                JOptionPane.showMessageDialog(FantasyFootballApp.this,
                        "Only the 433 formation is supported in this demo.");
                moduleSelection.setSelectedItem("433");
            }
        });
        topPanel.add(moduleSelection);
        add(topPanel, BorderLayout.NORTH);

        // --------------------------------------------------------------------
        // 2) Center panel: Pitch with mandatory 11 placeholders.
        // Create a pitch panel with a null layout to freely position placeholders.
        pitchPanel = new JPanel(null);
        pitchPanel.setPreferredSize(new Dimension(600, 450));
        pitchPanel.setBackground(new Color(34, 139, 34)); // dark-green pitch
        addPitchPlaceholders();
        add(pitchPanel, BorderLayout.CENTER);

        // --------------------------------------------------------------------
        // 5) South panel: Bench area (with add buttons) and Save Formation button.
        JPanel southPanel = new JPanel(new BorderLayout());
        benchContainerPanel = new JPanel(new GridLayout(1, 3));

        // Create three bench columns (for Defender, Midfielder and Forward).
        benchDefendersPanel = createBenchColumn("Defender");
        benchMidfieldersPanel = createBenchColumn("Midfielder");
        benchForwardsPanel = createBenchColumn("Forward");

        benchContainerPanel.add(benchDefendersPanel);
        benchContainerPanel.add(benchMidfieldersPanel);
        benchContainerPanel.add(benchForwardsPanel);
        southPanel.add(benchContainerPanel, BorderLayout.CENTER);

        // "Save Formation" button becomes enabled only when all 11 mandatory positions are filled.
        saveFormationButton = new JButton("Save Formation");
        saveFormationButton.setEnabled(false);
        southPanel.add(saveFormationButton, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --------------------------------------------------------------------
    // Sets up the 11 pitch placeholders for the 433 formation.
    // Positions are calculated so that:
    // • Forwards: 3 positions (“FW”) in the top row.
    // • Midfielders: 3 positions (“MF”) in the middle.
    // • Defenders: 4 positions (“DF”) in the next row.
    // • Goalkeeper: 1 position (“GK”) centered at the bottom.
    private void addPitchPlaceholders() {
        // Use a fixed size for each placeholder.
        int boxW = 80, boxH = 50;
        int pitchWidth = 600;
        
        // Forwards: row at y = 40. Calculate gaps to evenly spread three boxes.
        int gapF = (500 - 3 * boxW) / 2;  // available width (500=600-100 margin)
        int f1x = 50;
        int f2x = f1x + boxW + gapF;       // 50 + 80 + gapF
        int f3x = f2x + boxW + gapF;
        int fY = 40;
        pitchPlaceholders[0] = new FieldPlaceholder("Forward", f1x, fY, boxW, boxH);
        pitchPlaceholders[1] = new FieldPlaceholder("Forward", f2x, fY, boxW, boxH);
        pitchPlaceholders[2] = new FieldPlaceholder("Forward", f3x, fY, boxW, boxH);
        
        // Midfielders: row at y = 140.
        int gapM = gapF;
        int m1x = 50;
        int m2x = m1x + boxW + gapM;
        int m3x = m2x + boxW + gapM;
        int mY = 140;
        pitchPlaceholders[3] = new FieldPlaceholder("Midfielder", m1x, mY, boxW, boxH);
        pitchPlaceholders[4] = new FieldPlaceholder("Midfielder", m2x, mY, boxW, boxH);
        pitchPlaceholders[5] = new FieldPlaceholder("Midfielder", m3x, mY, boxW, boxH);
        
        // Defenders: row at y = 240; four positions.
        int totalDefenders = 4;
        int gapD = (500 - totalDefenders * boxW) / (totalDefenders - 1); // e.g. (500-320)/3
        int d1x = 50;
        int d2x = d1x + boxW + gapD;
        int d3x = d2x + boxW + gapD;
        int d4x = d3x + boxW + gapD;
        int dY = 240;
        pitchPlaceholders[6] = new FieldPlaceholder("Defender", d1x, dY, boxW, boxH);
        pitchPlaceholders[7] = new FieldPlaceholder("Defender", d2x, dY, boxW, boxH);
        pitchPlaceholders[8] = new FieldPlaceholder("Defender", d3x, dY, boxW, boxH);
        pitchPlaceholders[9] = new FieldPlaceholder("Defender", d4x, dY, boxW, boxH);
        
        // Goalkeeper: centered at y = 340.
        int gX = (pitchWidth - boxW) / 2;
        int gY = 340;
        pitchPlaceholders[10] = new FieldPlaceholder("Goalkeeper", gX, gY, boxW, boxH);
        
        // Add all placeholders to the pitchPanel.
        for (FieldPlaceholder fp : pitchPlaceholders) {
            pitchPanel.add(fp);
        }
    }

    // --------------------------------------------------------------------
    // Creates a bench column panel for a given role.
    // Each column consists of a header, a container (FlowLayout) for bench placeholders,
    // and a button which, when clicked, lets the user add a substitute.
    private JPanel createBenchColumn(String role) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(role + " Bench", SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);
        
        // Bench container where assigned substitutes will appear.
        JPanel benchPanel = new JPanel(new FlowLayout());
        benchPanel.setPreferredSize(new Dimension(200, 80));
        panel.add(benchPanel, BorderLayout.CENTER);
        
        // "Add" button pops up a selection dialog for available players.
        JButton addButton = new JButton("Add " + role);
        addButton.addActionListener(e -> addBenchPlayer(role, benchPanel));
        panel.add(addButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Adds a bench player of the given role to the specified bench panel.
    private void addBenchPlayer(String role, JPanel benchPanel) {
        List<String> avail = availablePlayers.get(role);
        if (avail == null || avail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available " + role + " players.");
            return;
        }
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select a " + role + " player:",
                "Player Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                avail.toArray(),
                avail.get(0)
        );
        if (selected != null) {
            // Remove the chosen player from the available pool.
            avail.remove(selected);
            // Create a bench placeholder—note the call to setForBench(true) so it behaves slightly differently.
            FieldPlaceholder benchPlaceholder = new FieldPlaceholder(role, 0, 0, 80, 50);
            benchPlaceholder.setAssignedPlayer(selected);
            benchPlaceholder.setForBench(true);
            benchPanel.add(benchPlaceholder);
            benchPanel.revalidate();
            benchPanel.repaint();
        }
    }

    // Updates the Save Formation button state. It becomes enabled only if all mandatory (pitch) placeholders are assigned.
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
    // Inner class representing a field placeholder (either on the pitch or the bench).
    // • When not assigned, it displays the role abbreviation on a gray background.
    // • When clicked (if unassigned), it opens a dialog for the user to choose a player.
    // • When a player is selected, it removes that player from the available pool,
    //   sets its background to blue, and updates the displayed text.
    // • When hovered (if assigned), a small "x" button appears that lets the user remove the assignment.
    private class FieldPlaceholder extends JPanel {
        private static final long serialVersionUID = 1L;
		private String role;
        private String assignedPlayer = null;
        private JLabel label;
        private JButton removeButton;
        private boolean forBench = false; // distinguishes bench placeholders from pitch (mandatory) ones

        public FieldPlaceholder(String role, int x, int y, int width, int height) {
            this.role = role;
            setLayout(null);
            setBounds(x, y, width, height);
            setBackground(Color.GRAY);
            
            // Display the role abbreviation (GK, DF, MF, FW) when unassigned.
            label = new JLabel(getRoleAbbreviation(), SwingConstants.CENTER);
            label.setBounds(0, 0, width, height);
            add(label);

            // Remove button (shown on hover when assigned).
            removeButton = new JButton("x");
            removeButton.setMargin(new Insets(0, 0, 0, 0));
            removeButton.setBounds(width - 20, 0, 20, 20);
            removeButton.setVisible(false);
            add(removeButton);
            removeButton.addActionListener(e -> removeAssignment());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!isAssigned()) {
                        assignPlayer();
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

        public void setForBench(boolean bench) {
            this.forBench = bench;
        }
        
        public boolean isAssigned() {
            return assignedPlayer != null;
        }
        
        // Sets the assigned player, updates display and (for pitch placeholders) updates the Save button.
        public void setAssignedPlayer(String player) {
            this.assignedPlayer = player;
            label.setText(player);
            setBackground(Color.BLUE);
            if (!forBench) {
                updateSaveButton();
            }
        }
        
        // Opens a dialog for selecting a player for this role.
        private void assignPlayer() {
            List<String> avail = availablePlayers.get(role);
            if (avail == null || avail.isEmpty()) {
                JOptionPane.showMessageDialog(FantasyFootballApp.this,
                        "No available " + role + " players.");
                return;
            }
            String selected = (String) JOptionPane.showInputDialog(
                    FantasyFootballApp.this,
                    "Select a " + role + " player:",
                    "Player Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    avail.toArray(),
                    avail.get(0)
            );
            if (selected != null) {
                // Remove the selected player from the available pool and set this placeholder.
                avail.remove(selected);
                setAssignedPlayer(selected);
            }
        }
        
        // Removes the current assignment, adds the player back to the available pool, and resets the display.
        private void removeAssignment() {
            if (assignedPlayer != null) {
                availablePlayers.get(role).add(assignedPlayer);
            }
            assignedPlayer = null;
            label.setText(getRoleAbbreviation());
            setBackground(Color.GRAY);
            if (!forBench) {
                updateSaveButton();
            } else {
                // For bench placeholders, remove the component from its parent.
                Container parent = getParent();
                if (parent != null) {
                    parent.remove(this);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        }
        
        // Returns role abbreviations.
        private String getRoleAbbreviation() {
            switch (role) {
                case "Goalkeeper": return "GK";
                case "Defender": return "DF";
                case "Midfielder": return "MF";
                case "Forward": return "FW";
                default: return role;
            }
        }
    }
    
    // --------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FantasyFootballApp());
    }
}

