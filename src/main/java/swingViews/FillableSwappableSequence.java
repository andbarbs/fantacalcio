package swingViews;

import java.util.ArrayList;
import java.util.List;
import swingViews.FillableSwappableSequence.*;

/**
 * Manages a sequence of gadgets in such a way that content in the sequence 
 * grows left‐to‐right, ensuring no gaps are left when a gadget loses its content,
 * and allows swapping the content of two adjacent gadgets.
 *
 * <p><h1>Contracts</h1>
 * In order to be driven by {@code FillableSwappableSequenceDriver}, gadgets must
 * <ul>
 * 	<li>fulfill the contract in {@link FillableSwappableGadget}&lt;T&gt; which ensures they are able to
 *   	<ul>
 *   		 <li> toggle their ability to receive user input
 *  	 	 <li> take over the content of a fellow gadget
 *   		 <li> swap contents with a fellow gadget
 *  	 	 <li> attach an instance of {@code FillableSwappableSequenceDriver} 
 *  				for sending content-related notifications
 *   	</ul></li>
 *   <li>notify the attached {@code FillableSwappableSequenceDriver} instance of changes to their content,
 *   passing their instance to methods {@link FillableSwappableSequence#contentAdded(FillableSwappableGadget)} and 
 *   {@link FillableSwappableSequence#contentRemoved(FillableSwappableGadget)} of the driver. 
 *   Gadgets should take care that programmatic addition/removal of their content, such as that induced by the driver, 
 *   be <i>not</i> notified back to the driver
 * </ul>
 *
 * <p><h1>Limitations</h1>
 * <ul>
 *   <li>Not thread‐safe</li>
 *   <li>Does not defend itself against event feedback loops</li>
 * </ul>
 *
 * @param <G> the client gadget type; must implement
 *           {@link FillableSwappableGadget}&lt;T&gt;
 * @see FillableSwappableGadget
 * @apiNote This driver is GUI-agnostic and relieves clients of using class inheritance
 */
public class FillableSwappableSequence<G extends FillableSwappableGadget<G>> {

	private interface RightwardFillable<P> {
		void acquireContentFrom(P other);
		
		/**
		 * is called on a 
		 * as part of a <i>collapse operation</i> orchestrated by the
		 * {@linkplain FillableSwappableSequence}
		 */
		void discardContent();
		void enableFilling();
		void disableFilling();

		// allow gadgets to customize rightmost-fillable status
		void highlight();
		void dehighlight();
	}

	private interface Swappable<Q> {
		void swapContentWith(Q other);
	}
	
	/**
	 * public interface gadgets must implement 
	 * so that a {@code FillableSwappableSequenceDriver} can drive them
	 * 
	 * <p><h1>Limitations</h1>
	 * no mechanism is in place to ensure a gadget correctly
	 * binds &lt;R&gt; to its own type</p>
	 *
	 * @param <R> the client type; must implement
	 *           {@link FillableSwappableGadget}&lt;R&gt;
	 * @see FillableSwappableSequence
	 * @implNote a collapsed interface for various private gadget interfaces
	 */
	public interface FillableSwappableGadget<R extends FillableSwappableGadget<R>>
			extends RightwardFillable<R>, Swappable<R> {
		void attachDriver(FillableSwappableSequence<R> driver);
	}
	
	/**
	 * public interface a visual must implement 
	 * so that a {@code FillableSwappableSequenceDriver} can update it
	 *
	 * @param <S> the gadget type; must implement
	 *           {@link FillableSwappableGadget}&lt;S&gt;
	 * @see FillableSwappableSequence
	 */
	public interface FillableSwappableSequenceListener<S extends FillableSwappableGadget<S>> {
		void becameEmpty(S emptiedGadget);
		void becameFilled(S filledGadget);
	}

	// internal bookkeeping
	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	// and make utility methods more intuitive
	private List<G> sequence;
	private static final int RIGHTMOST_FILLABLE_OVERFLOW = -1;
	int rightmostFillablePosition = RIGHTMOST_FILLABLE_OVERFLOW;
	List<FillableSwappableSequenceListener<G>> listeners = 
			new ArrayList<FillableSwappableSequenceListener<G>>();
	
	// internal utility methods for querying gadget status
	private boolean isRightmostFillable(G client) {
		return sequence.contains(client) && sequence.indexOf(client) == rightmostFillablePosition;
	}

	private boolean isFillable(G client) {
		return sequence.indexOf(client) <= rightmostFillablePosition || 
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW;
	}
	
	// useful to unit tests
	FillableSwappableSequence(List<G> gadgets) {
		this.sequence = gadgets;
	}
	
	// public static factory method
	public static <T extends FillableSwappableGadget<T>> FillableSwappableSequence<T> 
			createSequence(List<T> gadgets) {
		FillableSwappableSequence<T>  driver = new FillableSwappableSequence<T>(gadgets);
		driver.sequence.forEach(c -> {
			c.attachDriver(driver);
			c.disableFilling();
		});
		driver.sequence.getFirst().enableFilling();
		driver.updateRightmostFillable(0);
		return driver;
	}
	
	public void attachListener(FillableSwappableSequenceListener<G> listener) {
		listeners.add(listener);
	}

	// notification methods: update sequence state and collapse
	public void contentRemoved(G emptied) {
		if (!isFillable(emptied))
			throw new IllegalStateException("Content removal reported for gadget for which filling should have been disabled");
		if (isRightmostFillable(emptied))
			throw new IllegalStateException("Content removal reported for gadget for which filling had not been reported");
		if (!sequence.contains(emptied))
			throw new IllegalArgumentException("Content removal reported for a gadget that is not a member of this sequence");

		// 1) collapses content in the sequence
		int nextRightmostFillable = (rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW) ? 
				sequence.size() - 1 : rightmostFillablePosition - 1;
		for (int i = sequence.indexOf(emptied); i < nextRightmostFillable; i++) {
			sequence.get(i).acquireContentFrom(sequence.get(i + 1));
		}
		
		// 2) shifts rightmostFillable client backwards
		
		// avoids emptying if emptied == nextRightmostFillable
		if (sequence.indexOf(emptied) != nextRightmostFillable)
			sequence.get(nextRightmostFillable).discardContent();
		listeners.forEach(l -> l.becameEmpty(sequence.get(nextRightmostFillable)));
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).disableFilling();
		updateRightmostFillable(nextRightmostFillable);
	}

	public void contentAdded(G client) {
		if (!sequence.contains(client))
			throw new IllegalArgumentException("Content addition reported for a gadget that is not a member of this sequence");
		
		if (!isFillable(client))
			throw new IllegalStateException("Content addition reported for a gadget beyond the next-fillable");

		// updates sequence status moving rightmostFillable rightwards
		if (isRightmostFillable(client)) {
			if (rightmostFillablePosition < sequence.size() - 1)
				sequence.get(updateRightmostFillable(rightmostFillablePosition + 1)).enableFilling();
			else
				updateRightmostFillable(RIGHTMOST_FILLABLE_OVERFLOW); // signals RF overflow
			listeners.forEach(l -> l.becameFilled(client));
		}
	}

	// triggers gadget highlighting for rightmost-fillable status
	private int updateRightmostFillable(int newValue) {
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).dehighlight();
		rightmostFillablePosition = newValue;
		if (newValue != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).highlight();
		return newValue;
	}

	// swapping gadget content
	public boolean hasContent(G client) {
		return sequence.indexOf(client) < rightmostFillablePosition ||
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW;
	}
	
	public boolean canSwapLeft(G client) {
		return hasContent(client) && sequence.indexOf(client) != 0;
	}

	public boolean canSwapRight(G client) {
		return hasContent(client) && 
				sequence.indexOf(client) < sequence.size() - 1 &&
					hasContent(sequence.get(sequence.indexOf(client) + 1));
	}

	// TODO insert checks using canSwap (like Iterator does: next() -> hasNext())
	public void swapLeft(G client) {
		if (!sequence.contains(client))
			throw new IllegalArgumentException("Left-swapping requested for gadget that is not a member of this sequence");
		if (!canSwapLeft(client))
			throw new IllegalArgumentException("Left-swapping requested for gadget that is unable to swap left");
		client.swapContentWith(sequence.get(sequence.indexOf(client) - 1));
	}

	public void swapRight(G client) {		
		if (!sequence.contains(client))
			throw new IllegalArgumentException("Right-swapping requested for gadget that is not a member of this sequence");
		if (!canSwapRight(client))
			throw new IllegalArgumentException("Right-swapping requested for gadget that is unable to swap right");
		client.swapContentWith(sequence.get(sequence.indexOf(client) + 1));
	}

}
