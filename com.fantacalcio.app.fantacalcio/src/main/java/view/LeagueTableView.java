package view;

import java.util.List;

import model.Team;

public interface LeagueTableView extends View {

	void showLeagueTable(List<Team> teams);
}
