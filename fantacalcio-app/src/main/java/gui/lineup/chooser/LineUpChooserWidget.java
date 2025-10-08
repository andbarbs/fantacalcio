package gui.lineup.chooser;

import domain.LineUp;

/**
 * a type for a graphical component that can collaborate with a
 * {@link LineUpChooserController} according to the bidirectional scheme in the
 * <b>MVP pattern</b>
 */
public interface LineUpChooserWidget {
	
	/**
	 * enables the user to request to save a {@link LineUp}
	 * through the {@link LineUpChooserWidget}
	 */
	void enableSavingLineUp();

	/**
	 * forbids the user to request to save a {@link LineUp}
	 * through the {@link LineUpChooserWidget}
	 */
	void disableSavingLineUp();
	
	/**
	 * @param controller the {@link LineUpChooserController} that this
	 *                   {@link LineUpChooserWidget} is supposed to collaborate with
	 */
	void setController(LineUpChooserController controller);
}