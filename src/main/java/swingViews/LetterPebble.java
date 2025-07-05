package swingViews;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import swingViews.BidirectionalPebbleChain.Highlightable;
import swingViews.BidirectionalPebbleChain.Swappable;

public class LetterPebble extends JPanel implements Swappable<LetterPebble>, Highlightable{
	private static final long serialVersionUID = 1L;
	
	// 1) content
	protected char letter;
	
	@Override
	public void swapContentWith(LetterPebble other) {
		char temp = letter;
		letter = other.letter;
		other.letter = temp;
	}

    private LetterPebble(char letter) {
        this.letter = letter;
        setPreferredSize(new Dimension(50, 50));
        setOpaque(false);
    }
    
    // static factory method that parses a list of LetterPebble instances from a string
    public static List<LetterPebble> fromString(String initialLetters) {
    	return initialLetters
				.chars()
				.mapToObj(c -> (char) c)
				.map(t -> new LetterPebble(t))
				.collect(Collectors.toList());
    }
	
	// 2) highlighting
	private boolean highlighted;

	@Override
	public void highlight() {
		highlighted = true;
		repaint();		
	}

	@Override
	public void dehighlight() {
		highlighted = false;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth(), h = getHeight();
		Graphics2D g2 = (Graphics2D) g.create();

		// draw pebble circle
		g2.setColor(highlighted ? Color.ORANGE : Color.LIGHT_GRAY);
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
