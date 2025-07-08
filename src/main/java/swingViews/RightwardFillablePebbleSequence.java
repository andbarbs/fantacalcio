package swingViews;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import swingViews.RightwardFillableSequenceDriver.RightwardFillable;

@SuppressWarnings("serial")
public class RightwardFillablePebbleSequence
	<FillablePebble extends JComponent & RightwardFillable<FillablePebble>>
		extends JPanel {
	
	/* 
	 * arranges suitable clients in a sequence such that the contents 
	 * of one client can be swapped with its left and right neighbors.
	 * 
	 * To this end, it
	 * 	> employs a SwappableSequenceDriver that services client swap requests
	 * 	> provides a graphical "selection" facility that allows the 
	 *    user to pick the argument for a client swap request
	 *  > provides buttons that allow the user to send a swap 
	 *    request to the driver concerning the selected client
	 */

	public RightwardFillablePebbleSequence(List<FillablePebble> clients) {

		// 1. Initializes driver to client instances received
		new RightwardFillableSequenceDriver<FillablePebble>(clients);

		// 3. Add slots to their container and wire selection mechanism
		JPanel pebblePanel = new JPanel(new FlowLayout());
		clients.forEach(pebblePanel::add);

		// 6. Lays out frame
		setLayout(new BorderLayout());
		add(pebblePanel, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("rightward text input game");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			RightwardFillablePebbleSequence<FillableTextField> sequence = 
					new RightwardFillablePebbleSequence<FillableTextField>(List.of(
							new FillableTextField(20),
							new FillableTextField(20),
							new FillableTextField(20),
							new FillableTextField(20)));
			frame.setContentPane(sequence);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
