package gui.lineup.starter;

import domainModel.Scheme;

/**
 * a type for a graphical component that can collaborate with a
 * {@link StarterLineUpChooserController} according to the bidirectional
 * scheme in the <b>MVP pattern</b>
 */
public interface StarterLineUpChooserWidget {
	
	/**
	 * instructs a {@link StarterLineUpChooserWidget} to make the provided
	 * {@link Scheme} the current one.
	 * 
	 * @param scheme the {@link Scheme} instance to be made current
	 */
	void switchTo(Scheme scheme);
}