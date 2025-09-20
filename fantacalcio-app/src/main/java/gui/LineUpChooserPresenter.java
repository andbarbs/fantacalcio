package gui;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.FantaUser;

public class LineUpChooserPresenter {	
	
	public interface LineUpChooserView {
		void initChoice(FantaTeam team);
	}
	
	private LineUpChooserView view;

	public LineUpChooserPresenter(LineUpChooserView view) {
		this.view = view;
	}

	
	public void initializeChoice(FantaUser user, League currentLeague) {
		
		// 1) TODO gets the user's team under the current league
		FantaTeam team = null;
		
		// 2) calls into the view
		view.initChoice(team);
	}

}
