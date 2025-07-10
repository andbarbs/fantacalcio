package swingViews;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class CompetingComboBox<E> extends JComboBox<E> {
	
	private static final int COMPETING_CBOX_NO_CHOICE = -1;

	private Integer currentSelection;
	private List<Integer> mask;

	private List<E> contentPool;
	private Set<CompetingComboBox<E>> competitors = new HashSet<>();
			
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
		//this.setSelectedIndex(-1); // this does NOT trigger the Listener - it hasn't been attached yet!!

		// bookkeeping for user-triggered actions on the combo box
		
		/*
		 * an event is fired on a ComboBox under these circumstances:
		 * 		1) the user makes/updates their selection
		 * 		
		 * 		2) an entry is REMOVED from the underlying model AND no user selection exists
		 * 
		 * 		3) the selection is programmatically set to -1
		 * 
		 * */
	}
	
	@Override
	public void setSelectedIndex(int choice) {
		super.setSelectedIndex(choice);
		compete();
	}
	
	@Override
	public void setSelectedItem(Object choice) {
		super.setSelectedItem(choice);
		compete();
	}

	private void compete() {
		// a choice has been made on this CBox
		if (getSelectedIndex() != -1) {
			// pushes a previous choice back to competitors
			if (currentSelection != -1) { 
				competitors.forEach(competitor -> {
					if (competitor != CompetingComboBox.this)
						// this can generate an event, but it doesn't call setSelectedIndex()!!
						competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
				});
			}
			
			// updates bookkeeping to the current choice
			currentSelection = mask.get(getSelectedIndex());
			
			// removes the current choice from competitors
			competitors.forEach(competitor -> {
				if (competitor != CompetingComboBox.this) {
					competitor.evictFromComboBox(currentSelection);
				}
			});
		}
		
		// this CBox's choice has been cleared
		else if (getSelectedIndex() == -1 && currentSelection != null && currentSelection != COMPETING_CBOX_NO_CHOICE) {
			
			// pushes the cleared choice back to competitors
			competitors.forEach(competitor -> {
				if (competitor != CompetingComboBox.this)
					competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
			});
			
			// updates bookkeeping to no choice
			currentSelection = COMPETING_CBOX_NO_CHOICE;
		}
	}
	
	void setContents(List<E> contents) {		
		// initializes bookkeeping
		contentPool = List.copyOf(contents);
		currentSelection = -1;
		mask = new ArrayList<Integer>(
				IntStream.rangeClosed(1, contents.size()).boxed().collect(Collectors.toList()));

		// fills the cbox with initial contents
		setModel(new DefaultComboBoxModel<>(new Vector<>(contents)));
		
		// sets the cbox's starting selection to none
		setSelectedIndex(-1); // this WILL trigger the Listener! - it HAS already been attached!!
	}

	void setCompetitors(Set<CompetingComboBox<E>> competitors) {
		this.competitors = Objects.requireNonNull(competitors);
	}
	
	public static <T> void initializeCompetition(
			Set<CompetingComboBox<T>> competitors, List<T> contents) {
		competitors.forEach(compCombo -> {
			compCombo.setContents(contents);
			compCombo.setCompetitors(competitors);
		});
	}

}
