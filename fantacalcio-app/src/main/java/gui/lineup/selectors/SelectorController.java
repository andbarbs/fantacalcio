package gui.lineup.selectors;

public interface SelectorController {
	
	/**
	 * Allows the {@code OrderedDealerView} collaborator to notify its
	 * {@link SelectorController} that an option has been selected from the
	 * {@code View}'s current option list.
	 * 
	 * <p>
	 * <h1>Event-feedback avoidance</h1> This notifications should <i>not</i> take
	 * place for mutations induced on the View by the {@code SelectorController}
	 * itself: see notes to {@link OrderedDealerView}
	 *
	 * @param position the position of the option having been selected relative to
	 *                 the {@code OrderedDealerView}'s current option list
	 */
	public void selectedOption(int position);
	
	/**
	 * Allows the {@code OrderedDealerView}
	 * to notify its {@link SelectorController} that the View's previously existing
	 * selection has been cleared.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code SelectorController} itself: 
	 * see notes to {@link OrderedDealerView}
	 */
	public void selectionCleared();

}
