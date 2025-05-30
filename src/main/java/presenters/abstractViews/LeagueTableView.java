package presenters.abstractViews;

import java.util.List;

import domainModel.FantaTeam;

public interface LeagueTableView extends View {

	void showLeagueTable(List<FantaTeam> teams);
}
