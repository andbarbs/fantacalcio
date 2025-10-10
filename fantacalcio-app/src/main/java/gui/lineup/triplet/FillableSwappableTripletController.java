package gui.lineup.triplet;

import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;

/**
 * a type for a component that can collaborate with a
 * {@link FillableSwappableTripletWidget} according to the bidirectional scheme in the
 * <b>MVP pattern</b>
 */
public interface FillableSwappableTripletController {

	/**
	 * called by the {@link FillableSwappableTripletWidget} collaborator when the
	 * user requests to swap the first pair of {@link SubstituteSelectorDelegate}s
	 */
	void swapFirstPair();

	/**
	 * called by the {@link FillableSwappableTripletWidget} collaborator when the
	 * user requests to swap the second pair of {@link SubstituteSelectorDelegate}s
	 */
	void swapSecondPair();
}
