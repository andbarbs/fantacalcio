package repository;

import model.FantaCoach;
import model.League;

public interface LeagueRepository {

	League getLeagueByName(String leagueName);

	void add(League league);

	boolean isLeagueFull(League league);

	void addFantaCoach(League league, FantaCoach fantaCoach);

	void setAdmin(FantaCoach fantaCoach);

}
