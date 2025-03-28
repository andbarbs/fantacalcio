package domainModel;

public class Match {
    private MatchDaySerieA matchDay;
    private String squadra1;
    private String squadra2;
    private String risultato;
    private String moduloSquadra1;
    private String moduloSquadra2;    

    public Match(MatchDaySerieA matchDay, String squadra1, String squadra2, String risultato, String moduloSquadra1,
			String moduloSquadra2) {
		this.matchDay = matchDay;
		this.squadra1 = squadra1;
		this.squadra2 = squadra2;
		this.risultato = risultato;
		this.moduloSquadra1 = moduloSquadra1;
		this.moduloSquadra2 = moduloSquadra2;
	}

    public String getSquadra1() {
        return squadra1;
    }

    public String getSquadra2() {
        return squadra2;
    }

    public String getRisultato() {
        return risultato;
    }

    public String getModuloSquadra1() {
        return moduloSquadra1;
    }

    public String getModuloSquadra2() {
        return moduloSquadra2;
    }

	public MatchDaySerieA getMatchDay() {
		return matchDay;
	}
}

