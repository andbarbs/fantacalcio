package swingViews;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class CompetingComboBox<E> extends JComboBox<E> {

	private static final long serialVersionUID = 1L;

	private Integer currentSelection;
	private List<Integer> mask;

	private List<E> contentPool;
	private List<CompetingComboBox<E>> competitors;

	private void evictFromComboBox(Integer toBeEvicted) {
		int pos = mask.indexOf(toBeEvicted);
		mask.remove(pos);
		DefaultComboBoxModel<E> model = (DefaultComboBoxModel<E>) this.getModel();
		model.removeElementAt(pos);
	}

	private void insertIntoComboBox(Integer indexToBeInserted, E playerToBeInserted) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= indexToBeInserted)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, indexToBeInserted);
		DefaultComboBoxModel<E> model = (DefaultComboBoxModel<E>) this.getModel();
		model.insertElementAt(playerToBeInserted, insertionIndex);
	}

	public CompetingComboBox() {
		super();

		// sets up the Combo without any contents
		this.setModel(new DefaultComboBoxModel<E>());
		this.setSelectedIndex(-1); // this does NOT trigger the Listener - it hasn't been attached yet!!

		// bookkeeping for user-triggered actions on the combo box
		
		/*
		 * an event is fired on a ComboBox under these circumstances:
		 * 		1) the user makes/updates their selection
		 * 		
		 * 		2) an entry is REMOVED from the underlying model, and no user selection exists
		 * 
		 * 		3) the selection is programmatically set to -1
		 * 
		 * */
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("listener del cbox viene eseguito!");
				
				// this CBox has had a selection made
				if (isPopupVisible() && getSelectedIndex() > -1) { 
					
					System.out.println("siamo dentro if");
					
					// a previous selection also existed
					if (currentSelection != -1) { 
						for (CompetingComboBox<E> competitor : competitors) {
							if (competitor != CompetingComboBox.this) {
								competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
							}
						}
					}
					
					// registers the current selection
					currentSelection = mask.get(getSelectedIndex());
					
					// removes current selection from competing cboxes
					for (CompetingComboBox<E> competitor : competitors) {
						if (competitor != CompetingComboBox.this) {
							competitor.evictFromComboBox(currentSelection);
						}
					}
					
					System.out.println("\n\n");
				}
				
				// this CBox's selection has been reset
				else if (getSelectedIndex() == -1 && currentSelection != -1) {
					// adds cleared selection back into competing cboxes
					for (CompetingComboBox<E> competitor : competitors) {
						if (competitor != CompetingComboBox.this) {
							competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
						}
					}
					
					// clears the bookkeeping current selection
					currentSelection = -1;
				}
			}
		});
	}
	
	public void setContents(List<E> contents) {
		
		// initializes bookkeeping
		contentPool = List.copyOf(contents);
		currentSelection = -1;
		mask = new ArrayList<Integer>(
				IntStream.rangeClosed(1, contents.size()).boxed().collect(Collectors.toList()));

		// fills the cbox with initial contents
		setModel(new DefaultComboBoxModel<>(new Vector<>(contents)));
		setSelectedIndex(-1); // this WILL trigger the Listener! - it HAS already been attached!!
	}

	public void setCompetitors(List<CompetingComboBox<E>> competitors) {
		this.competitors = competitors;
	}

}
