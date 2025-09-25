package gui.lineup.starter;

import domainModel.Scheme;

/**
 * a type for a component that can collaborate with a
 * {@link StarterLineUpChooserWidget} according to the bidirectional scheme in
 * the <b>MVP pattern</b>
 */
public interface StarterLineUpChooserController {

	/**
	 * called by the {@link StarterLineUpChooserWidget} collaborator when the user
	 * requests to shift to a new {@link Scheme}.
	 * 
	 * @param newScheme the {@link Scheme} the user wants to shift to
	 */
	void switchToScheme(Scheme newScheme);
}