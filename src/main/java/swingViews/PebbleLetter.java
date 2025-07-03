package swingViews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PebbleLetter extends JPanel {
    private static final long serialVersionUID = 1L;
	private char letter;
    private boolean selected = false;
    private PebbleLetter leftNeighbor, rightNeighbor;

    // Factory to create and wire a chain of pebbles
    public static java.util.List<PebbleLetter> createChain(char[] letters) {
        java.util.List<PebbleLetter> chain = new java.util.ArrayList<>();
        for (char c : letters) {
            chain.add(new PebbleLetter(c));
        }
        for (int i = 0; i < chain.size(); i++) {
            if (i > 0)  chain.get(i).setLeftNeighbor(chain.get(i - 1));
            if (i < chain.size() - 1) chain.get(i).setRightNeighbor(chain.get(i + 1));
        }
        return chain;
    }

    public PebbleLetter(char letter) {
        this.letter = letter;
        setPreferredSize(new Dimension(50, 50));
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(true);
            }
        });
    }

    public void setLeftNeighbor(PebbleLetter neighbor) {
        this.leftNeighbor = neighbor;
    }

    public void setRightNeighbor(PebbleLetter neighbor) {
        this.rightNeighbor = neighbor;
    }
    
    /** 
     * Expose neighbors so the frame can shift selection 
     */
    public PebbleLetter getLeftNeighbor() {
        return leftNeighbor;
    }

    public PebbleLetter getRightNeighbor() {
        return rightNeighbor;
    }

    public boolean canSwapLeft() {
        return leftNeighbor != null;
    }

    public boolean canSwapRight() {
        return rightNeighbor != null;
    }

    public void swapLeft() {
        if (!canSwapLeft()) return;
        char tmp = letter;
        letter = leftNeighbor.letter;
        leftNeighbor.letter = tmp;
        repaint();
        leftNeighbor.repaint();
    }

    public void swapRight() {
        if (!canSwapRight()) return;
        char tmp = letter;
        letter = rightNeighbor.letter;
        rightNeighbor.letter = tmp;
        repaint();
        rightNeighbor.repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean sel) {
        boolean old = this.selected;
        this.selected = sel;
        repaint();
        firePropertyChange("selected", old, sel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();

        // draw pebble circle
        g2.setColor(selected ? Color.ORANGE : Color.LIGHT_GRAY);
        g2.fillOval(5, 5, w - 10, h - 10);

        // draw letter
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        FontMetrics fm = g2.getFontMetrics();
        String s = String.valueOf(letter);
        int sw = fm.stringWidth(s);
        int sh = fm.getAscent();
        g2.drawString(s, (w - sw) / 2, (h + sh) / 2 - 4);

        g2.dispose();
    }
}
