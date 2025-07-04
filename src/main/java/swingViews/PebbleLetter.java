package swingViews;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PebbleLetter extends JPanel {
	private static final long serialVersionUID = 1L;
	
	protected char letter;
    private PebbleLetter leftNeighbor, rightNeighbor;

	// factory method to wire a chain of pebbles
    public static void chain(List<? extends PebbleLetter> unwired) {
        for (int i = 0; i < unwired.size(); i++) {
            if (i > 0)  unwired.get(i).setLeftNeighbor(unwired.get(i - 1));
            if (i < unwired.size() - 1) unwired.get(i).setRightNeighbor(unwired.get(i + 1));
        }
    }

    public PebbleLetter(char letter) {
        this.letter = letter;
        setPreferredSize(new Dimension(50, 50));
        setOpaque(false);
    }    

    // must be protected to allow chaining of List<? extends PebbleLetter>
    protected void setLeftNeighbor(PebbleLetter neighbor) {
        this.leftNeighbor = neighbor;
    }

    protected void setRightNeighbor(PebbleLetter neighbor) {
        this.rightNeighbor = neighbor;
    }

    public boolean canSwapLeft() {
        return leftNeighbor != null;
    }

    public boolean canSwapRight() {
        return rightNeighbor != null;
    }

	public void swapLeft() {
        if (!canSwapLeft()) return;
        leftNeighbor.pullFrom(this);;
    }

    public void swapRight() {
        if (!canSwapRight()) return;
        rightNeighbor.pullFrom(this);;
    }
    
    // allows subclasses to peek into swapping
    protected void pullFrom(PebbleLetter sender) {
    	char tmp = letter;
    	letter = sender.letter;
    	sender.letter = tmp;		
    }
}
