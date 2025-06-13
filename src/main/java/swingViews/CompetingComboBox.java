package swingViews;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	private List<CompetingComboBox<E>> competitors = new ArrayList<>();

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
		 * 		2) an entry is REMOVED from the underlying model AND no user selection exists
		 * 
		 * 		3) the selection is programmatically set to -1
		 * 
		 * */
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("listener del cbox viene eseguito!");
				
				// this CBox has been given a selection
				if (isPopupVisible() && getSelectedIndex() > -1) { 
					
					System.out.println("siamo dentro if");
					
					// adds back a previous selection to competitors
					if (currentSelection != -1) { 
						competitors.forEach(competitor -> {
							if (competitor != CompetingComboBox.this) {
								competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
							}
						});
					}
					
					// registers the bookkeeping current selection
					currentSelection = mask.get(getSelectedIndex());
					
					// removes current selection from competitors
					competitors.forEach(competitor -> {
						if (competitor != CompetingComboBox.this) {
							competitor.evictFromComboBox(currentSelection);
						}
					});
					
					System.out.println("\n\n");
				}
				
				// this CBox's selection has been dropped
				else if (getSelectedIndex() == -1 && currentSelection != -1) {
					// adds cleared selection back to competitors
					competitors.forEach(competitor -> {
						if (competitor != CompetingComboBox.this) {
							competitor.insertIntoComboBox(currentSelection, contentPool.get(currentSelection - 1));
						}
					});
					
					// clears the bookkeeping current selection
					currentSelection = -1;
				}
			}
		});
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

	void setCompetitors(List<CompetingComboBox<E>> competitors) {
		this.competitors = Objects.requireNonNull(competitors);
	}
	
	public static <T> void initializeCompetition(
			List<CompetingComboBox<T>> competitors, List<T> contents) {
		competitors.forEach(compCombo -> {
			compCombo.setContents(contents);
			compCombo.setCompetitors(competitors);
		});
	}

}
