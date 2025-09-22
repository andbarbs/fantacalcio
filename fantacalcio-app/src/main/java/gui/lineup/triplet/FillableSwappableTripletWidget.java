package gui.lineup.triplet;

import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;

/**
 * a type for a graphical component that can collaborate with a
 * {@link FillableSwappableTripletController} according to the bidirectional
 * scheme in the <b>MVP pattern</b>
 */
public interface FillableSwappableTripletWidget {
	
	/**
	 * instructs a {@link FillableSwappableTripletWidget} to enable/disable the
	 * user's ability to request a swap between the first pair of
	 * {@link SubstituteSelectorDelegate}s in the {@link FillableSwappableSequence
	 * sequence}.
	 * 
	 * @param enabled a flag containing {@code true} if swapping is to be enabled,
	 *                false otherwise
	 */
	void setSwappingFirstPair(boolean enabled);
	
	/**
	 * instructs a {@link FillableSwappableTripletWidget} to enable/disable the
	 * user's ability to request a swap between the second pair of
	 * {@link SubstituteSelectorDelegate}s in the {@link FillableSwappableSequence
	 * sequence}.
	 * 
	 * @param enabled a flag containing {@code true} if swapping is to be enabled,
	 *                false otherwise
	 */
	void setSwappingSecondPair(boolean enabled);
	
	/**
	 * instructs a {@link FillableSwappableTripletWidget} to disable the
	 * user's ability to request a swap between both pairs of
	 * {@link SubstituteSelectorDelegate}s in the {@link FillableSwappableSequence
	 * sequence}.
	 */
	void resetSwapping();
}