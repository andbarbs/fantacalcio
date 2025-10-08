package presenters.abstractViews;

import java.util.List;

import domain.FantaTeam;

public interface LeagueTableView extends View {

	void showLeagueTable(List<FantaTeam> teams);
}
