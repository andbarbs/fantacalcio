package gui.lineup.chooser;

import java.util.Optional;

/**
 * a type for a component that 
 * <ul>
 * <li>defines a notion of <i><b>selection</b></i> involving an 
 * <i><b>option</b></i>
 * <li>allows clients to
 * 	<ol>
 * 		<li>select an option, or clear the selection
 * 		<li>query the selection
 * 		<li>be notified of events related to the selection
 * 	</ol>
 * </ul>
 * @param <T> the type for options that can be selected on this {@code Selector}
 */
public interface Selector<T> {
	
	/**
	 * an interface for clients wishing to be notified of <i>selection events</i>
	 * occurring on a {@link Selector} instance.
	 * 
	 * @param <Q> the type for options in the observed {@link Selector}
	 */
	public interface SelectorListener<T> {
		
		/**
		 * will be called on {@link SelectorListener}s when a selection
		 * has been made on an observed {@link Selector}.
		 * 
		 * @param selector the observed {@link Selector} instance which has
		 *                 received a selection
		 */
		void selectionMadeOn(Selector<T> selector);
		
		/**
		 * will be called on {@link SelectorListener}s when the selection
		 * on an observed {@link Selector} has been cleared.
		 * 
		 * @param selector the observed {@link Selector} instance whose
		 *                 selection has been cleared
		 */
		void selectionClearedOn(Selector<T> selector);
	}
	
	void attachListener(SelectorListener<T> listener);
	
	void removeListener(SelectorListener<T> listener);
	
	/**
	 * @return an {@code Optional} containing the option currently selected on this
	 *         {@code Selector}, or an empty one if the {@code Selector} has no
	 *         selection
	 */
	Optional<T> getSelection();

	/**
	 * @param option an {@code Optional} containing the option to be set on this
	 *               {@code Selector}, or an empty one if one wishes to clear the
	 *               {@code Selector}'s selection
	 */
	void setSelection(Optional<T> option);
	
}
