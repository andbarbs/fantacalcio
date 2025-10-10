package gui.lineup.chooser;

/**
 * a type for a component that can collaborate with a
 * {@link LineUpChooserWidget} according to the bidirectional scheme in the
 * <b>MVP pattern</b>
 */
public interface LineUpChooserController {

	/**
	 * called by the {@link LineUpChooserWidget} collaborator when the user
	 * requests to save the {@link LineUp} on the {@code Widget}
	 */
	public void saveLineUp();
	
	/**
	 * @param widget the {@link LineUpChooserWidget} that this
	 *               {@link LineUpChooserController} is supposed to collaborate with
	 */
	void setWidget(LineUpChooserWidget widget);
}
