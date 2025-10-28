package gui.lineup.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gui.lineup.sequence.FillableSwappableSequence.*;

/**
 * drives a {@code List} of implementors of {@linkplain FillableSwappable} in
 * such a way as to realize a <i>fillable-swappable sequence</i>.
 * 
 * <p>Envisaging sequence members as being laid out left-to-right, a
 * fillable-swappable sequence
 * <ol>
 * 	<li>only permits content in the sequence to <i>grow left-to-right</i>,
 * 		plugging the gap that may arise when a member loses its content via a
 * 		<i>collapse operation</i>
 *	 <li>allows clients to swap the contents of two adjacent members that
 * 		both hold content
 * 	<li>identifies as the <i>"next-fillable"</i> the empty member that can be
 * 		filled next, if existing
 * </ol>
 *
 * <p>In order to realize a fillable-swappable sequence, members must collaborate
 * with the {@code FillableSwappableSequence} as specified under their
 * interface.
 *
 * <p><h1>Limitations</h1>
 * <ul>
 * 	<li>Not thread‐safe
 * 	<li>Vulnerable to feedback loops from members
 * </ul>
 *
 * @param <F> the type for members in the sequence; must implement
 *            {@link FillableSwappable FillableSwappable&lt;F&gt;}
 */
public class FillableSwappableSequence<F extends FillableSwappable<F>> {
	
	/*
	 * consists of:
	 * 		1. interface for Sequence members
	 * 		2. internal bookkeeping
	 * 		3. notification points for Sequence members
	 * 		4. Listener interface and Subject infrastructure 
	 * 		5. Sequence-member swapping API for clients
	 */

	// 1. interface for Sequence members

	/**
	 * an interface for a type that wants to be part of a
	 * {@linkplain FillableSwappableSequence <i>fillable-swappable sequence</i>}.
	 * 
	 * <p>Implicitly, a {@link FillableSwappable} is supposed to provide means for
	 * clients to:
	 * <ul>
	 * 	<li><i>fill</i> it with <i>content</i>
	 * 	<li><i>clear</i> its <i>content</i>
	 * </ul>
	 * 
	 * <p>A fillable-swappable sequence will then result from an implementor
	 * additionally satisfying the following <i><b>behavioral requirements</i></b>:
	 * <ol>
	 * 	<li>reporting content being entered and cleared to the
	 * 		{@linkplain FillableSwappableSequence} driver, respectively, through the
	 * 		latter's {@linkplain FillableSwappableSequence#contentAdded
	 * 		<i>content-entered</i>} and
	 * 		{@linkplain FillableSwappableSequence#contentRemoved <i>content-cleared</i>}
	 * 		notification points
	 * 	<li>ensuring that driver requests to {@linkplain #discardContent discard} its
	 * 		content, {@linkplain #acquireContentFrom acquire} that of, and
	 * 		{@linkplain #swapContentWith swap} its with that of a fellow implementor, do
	 * 		<b>not</b> produce a notification back to the driver
	 * </ol>
	 * 
	 * @param <T> intended as a self-bound type; must implement
	 *            {@link FillableSwappable FillableSwappable&lt;T&gt;}
	 */
	public interface FillableSwappable<T extends FillableSwappable<T>> {

		/**
		 * encourages a {@linkplain FillableSwappable} to initialize a composed
		 * {@linkplain FillableSwappableSequence} reference, as part of the
		 * establishment of a fillable-swappable sequence.
		 * 
		 * @apiNote effectively a mandated {@code setter}
		 * @param driver the driver for the sequence being established
		 */
		void attachDriver(FillableSwappableSequence<T> driver);

		/**
		 * requires a {@linkplain FillableSwappable} to discard its content, as part of
		 * a <i>collapsing operation</i> within its fillable-swappable sequence.
		 * 
		 * <p> {@code This} instance is guaranteed to have content.
		 * 
		 * <p><h1>Driver feedback avoidance</h1> 
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 */
		void discardContent();

		/**
		 * requires a {@linkplain FillableSwappable} to acquire the content of a fellow
		 * sequence member, as part of a <i>collapsing operation</i> within their
		 * fillable-swappable sequence.
		 * 
		 * <p> The {@code other} instance is guaranteed to have content.
		 * 
		 * <p><h1>Driver feedback avoidance</h1> 
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 * 
		 * @param other the fellow sequence member whose content {@code this} must
		 *              acquire
		 */
		void acquireContentFrom(T other);

		/**
		 * requires a {@linkplain FillableSwappable} to swap its content with that of a
		 * fellow sequence member, as part of a <i>swap operation</i> within their
		 * fillable-swappable sequence.
		 * 
		 * <p> Both {@code this} and {@code other} instances are guaranteed to have content.
		 * 
		 * <p><h1>Driver feedback avoidance</h1> 
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 * 
		 * @param other the fellow sequence member {@code this} must swap contents with
		 */
		void swapContentWith(T other);

		/**
		 * requires a {@linkplain FillableSwappable} to enable/disable the ability for
		 * clients to enter content into it, as part of <i>access management</i> within
		 * its fillable-swappable sequence.
		 * 
		 * @param flag true requests enabling, false requests disabling
		 */
		void setFillingEnabled(boolean flag);

		/**
		 * allows a {@linkplain FillableSwappable} to advertise its status as the
		 * <i>next-fillable</i> member of its fillable-swappable sequence, as part of
		 * <i>access management</i> within the sequence.
		 * 
		 * @param flag true requests taking on, false requests relinquishing
		 *             <i>next-fillable</i> status
		 */
		void setNextFillable(boolean flag);

	}

	// 2. internal bookkeeping

	/**
	 * an internal wrapper for sequence member instances. Allows the containing
	 * instance of {@linkplain FillableSwappableSequence} to 
	 * <ul>
	 * 	<li>query the status of a member within the sequence in a fluent style
	 * 	<li>avoid List.indexOf() calls thanks to the index field
	 * </ul>
	 * 
	 * @implNote an instance of {@linkplain FillableSwappableSequence} is the sole
	 *           instantiator of this type
	 */
	private class MemberWrapper {

		final F member;
		final int index;

		MemberWrapper(F member, int index) {
			this.member = member;
			this.index = index;
		}

		F unwrap() {
			return member;
		}

		int index() {
			return index;
		}

		boolean isRightmostFillable() {
			return index == rightmostFillableIndex;
		}

		boolean isFillable() {
			return index <= rightmostFillableIndex || rightmostFillableIndex == RF_OVERFLOW;
		}

		boolean hasContent() {
			return index < rightmostFillableIndex || rightmostFillableIndex == RF_OVERFLOW;
		}

		boolean canSwapRight() {
			return hasContent() && index < sequence.size() - 1 && sequence.get(index + 1).hasContent();
		}
	}

	/**
	 * allows a {@linkplain FillableSwappableSequence
	 * FillableSwappableSequence&lt;F&gt;} to enquire about membership of an
	 * instance of {@linkplain F} within the sequence.
	 * 
	 * @param client instance of {@linkplain F} whose membership is being queried
	 * @return an {@code Optional} containing the wrapper for the client, or an
	 *         empty one if the client is not found within this sequence
	 */
	private Optional<MemberWrapper> wrapperOf(F client) {
		return sequence.stream().filter(w -> w.unwrap().equals(client)).findFirst();
	}
	
	private F memberAt(int nextRightmostFillableIndex) {
		return sequence.get(nextRightmostFillableIndex).unwrap();
	}

	/*
	 * package-private bookkeeping & constructor aid in the set-up phase of unit tests
	 */

	private List<MemberWrapper> sequence; 		// contains wrappers for members of the sequence
	int rightmostFillableIndex = RF_OVERFLOW;   // contains next-fillable's index within the sequence
	static final int RF_OVERFLOW = -1;

	FillableSwappableSequence(List<F> fillableSwappables) {
		this.sequence = IntStream.range(0, fillableSwappables.size())
				.mapToObj(i -> new MemberWrapper(fillableSwappables.get(i), i))
				.collect(Collectors.toList());
	}

	/**
	 * A compact API for initializing a {@linkplain FillableSwappableSequence
	 * <i>fillable-swappable sequence</i>}.
	 * 
	 * <p>Will <b>not</b> empty out members upon sequence establishment.
	 * 
	 * @param <T>     the type for members of the sequence; must implement
	 *                {@link FillableSwappable FillableSwappable&lt;T&gt;}
	 * @param members the {@code List} of {@code FillableSwappable}s to include in
	 *                the sequence
	 * @return the instance of {@linkplain FillableSwappableSequence} orchestrating
	 *         the new sequence
	 */
	public static <T extends FillableSwappable<T>> FillableSwappableSequence<T> 
				createSequence(List<T> members) {
		FillableSwappableSequence<T> driver = new FillableSwappableSequence<T>(members);
		driver.sequence.forEach(w -> {
			w.unwrap().attachDriver(driver);
			w.unwrap().setFillingEnabled(false);
		});
		driver.sequence.get(0).unwrap().setFillingEnabled(true);
		driver.updateRightmostFillable(0);
		return driver;
	}

	// 3. notification points for Sequence members
	
	/**
	 * notification point for <i>content-removed</i> events on members in a
	 * {@linkplain FillableSwappableSequence <i>fillable-swappable sequence</i>}.
	 * 
	 * @param emptied the {@linkplain FillableSwappable} for which the event is
	 *                being reported
	 * @throws IllegalArgumentException if the provided {@code FillableSwappable} is
	 *                                  not part of the sequence managed by this
	 *                                  {@code driver}
	 * @throws IllegalStateException    if the {@code driver} did not expect this
	 *                                  event to occur on the provided sequence
	 *                                  member
	 */
	public void contentRemoved(F emptied) {
		wrapperOf(emptied).ifPresentOrElse(wrapper -> {
			if (!wrapper.isFillable())
				throw new IllegalStateException(String.format(
						"FillableSwappableSequence.contentRemoved: Illegal Sequence State\n"
						+ "Content removal reported for member %s "
						+ "for which filling should have been disabled\n", wrapper.unwrap()));
			
			if (wrapper.isRightmostFillable())
				throw new IllegalStateException(String.format(
						"FillableSwappableSequence.contentRemoved: Illegal Sequence State\n"
						+ "Content removal reported for member %s "
						+ "for which no content addition had been reported\n", wrapper.unwrap()));

			int nextRightmostFillableIndex = (rightmostFillableIndex == RF_OVERFLOW) ? 
					sequence.size() - 1 : rightmostFillableIndex - 1;
			
			// collapses content
			IntStream.range(wrapper.index(), nextRightmostFillableIndex)
				.forEach(i -> memberAt(i).acquireContentFrom(memberAt(i + 1)));

			// empties out the nextRightmostFillable
			if (wrapper.index() != nextRightmostFillableIndex)	// avoids discarding if emptied == nextRF
				memberAt(nextRightmostFillableIndex).discardContent();
			listeners.forEach(l -> l.becameEmpty(memberAt(nextRightmostFillableIndex)));
			
			// updates rightmostFillableIndex
			if (rightmostFillableIndex != RF_OVERFLOW)
				memberAt(rightmostFillableIndex).setFillingEnabled(false);			
			updateRightmostFillable(nextRightmostFillableIndex);
		}, () -> {
			throw new IllegalArgumentException(String.format(
					"FillableSwappableSequence.contentRemoved: Illegal Argument\n"
					+ "Content removal reported for FillableSwappable %s "
					+ "that is not a member of this sequence\n", emptied));
		});

	}

	/**
	 * notification point for <i>content-added</i> events on members in a
	 * {@linkplain FillableSwappableSequence <i>fillable-swappable sequence</i>}.
	 * 
	 * @param filled the {@linkplain FillableSwappable} for which the event is being
	 *               reported
	 * @throws IllegalArgumentException if the provided {@code FillableSwappable} is
	 *                                  not part of the sequence managed by this
	 *                                  {@code driver}
	 * @throws IllegalStateException    if the {@code driver} did not expect this
	 *                                  event to occur on the provided sequence
	 *                                  member
	 */
	public void contentAdded(F filled) {
		wrapperOf(filled).ifPresentOrElse(wrapper -> {
			if (!wrapper.isFillable())
				throw new IllegalStateException(String.format(
						"FillableSwappableSequence.contentAdded: Illegal Sequence State\n"
						+ "Content addition reported for member %s "
						+ "for which filling should have been disabled\n", wrapper.unwrap()));

			// advances sequence on RF being filled	
			if (wrapper.isRightmostFillable()) {			
				if (rightmostFillableIndex < sequence.size() - 1)
					memberAt(updateRightmostFillable(rightmostFillableIndex + 1)).setFillingEnabled(true);
				else
					updateRightmostFillable(RF_OVERFLOW);
				listeners.forEach(l -> l.becameFilled(filled));
			}
		}, () -> {
			throw new IllegalArgumentException(String.format(
					"FillableSwappableSequence.contentAdded: Illegal Argument\n"
					+ "Content addition reported for FillableSwappable %s "
					+ "that is not a member of this sequence\n", filled));
		});

	}

	/**
	 * updates the next-fillable index consistently with internal
	 * bookkeeping and next-fillable status advertisement.
	 * 
	 * @param newValue the value to set {@linkplain rightmostFillableIndex} to
	 * @return the updated value of {@linkplain rightmostFillableIndex}
	 */
	private int updateRightmostFillable(int newValue) {
		
		if (rightmostFillableIndex != RF_OVERFLOW)	// the current rightmostFillable exists
			memberAt(rightmostFillableIndex).setNextFillable(false);

		rightmostFillableIndex = newValue;
		
		if (rightmostFillableIndex != RF_OVERFLOW)	// the updated rightmostFillable exists
			memberAt(rightmostFillableIndex).setNextFillable(true);

		return rightmostFillableIndex;
	}

	// 4. Listener interface and Subject infrastructure 
	
	/**
	 * an interface for clients wishing to be notified of the emptying/filling of
	 * members of a {@linkplain FillableSwappableSequence}.
	 * 
	 * @param <M> the type for members of the sequence; must implement
	 *            {@link FillableSwappable FillableSwappable&lt;M&gt;}
	 */
	public interface FillableSwappableSequenceListener<M extends FillableSwappable<M>> {
		
		/**
		 * will be called on {@linkplain FillableSwappableSequenceListener listener}s
		 * when a <i>previously filled</i> member of the sequence becomes empty.
		 * 
		 * <p>This event reflects the state of the sequence 
		 * <b>after</b> the collapsing operation has taken place.
		 * 
		 * @param emptiedMember the newly-empty member of the observed sequence
		 */
		void becameEmpty(M emptiedMember);
		
		/**
		 * will be called on {@linkplain FillableSwappableSequenceListener listener}s
		 * when a <i>previously empty</i> member of the sequence becomes filled.
		 * 
		 * @param filledMember the newly-filled member of the observed sequence
		 */
		void becameFilled(M filledMember);
	}

	List<FillableSwappableSequenceListener<F>> listeners = new ArrayList<FillableSwappableSequenceListener<F>>();

	public void attachListener(FillableSwappableSequenceListener<F> listener) {
		listeners.add(listener);
	}

	// 5. Sequence-member swapping API for clients

	/**
	 * allows clients to request a content swap for a sequence member and its
	 * right neighbor.
	 * 
	 * @param swappee the sequence member for which a right swap is requested
	 * @throws IllegalArgumentException if the swap operation does not involve two
	 *                                  members that hold content. Clients should
	 *                                  register themselves as listeners in order to
	 *                                  monitor the filling of sequence members
	 */
	public void swapRight(F swappee) {
		wrapperOf(swappee).ifPresentOrElse(wrapper -> {
			if (!wrapper.canSwapRight())
				throw new IllegalArgumentException(String.format(
						"FillableSwappableSequence.swapRight: Illegal Argument\n"
						+ "Right-swapping requested for member %s "
						+ "that is unable to swap right\n", wrapper.unwrap()));
			
			MemberWrapper rightNeighbor = sequence.get(wrapper.index() + 1);
			wrapper.unwrap().swapContentWith(rightNeighbor.unwrap());
		}, () -> {
			throw new IllegalArgumentException(String.format(
					"FillableSwappableSequence.swapRight: Illegal Argument\n"
					+ "Right-swapping requested for member %s "
					+ "that is not a member of this sequence\n", swappee));
		});
	}

	// TODO this is not tested!!!
	public Optional<F> nextFillable() {
		if (rightmostFillableIndex != RF_OVERFLOW)
			return Optional.of(memberAt(rightmostFillableIndex));
		else return Optional.empty();
	}
}
