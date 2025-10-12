package domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private MatchDay matchDay;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FantaTeam team1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FantaTeam team2;

    protected Match() {}

    public Match(MatchDay matchDay, FantaTeam team1, FantaTeam team2) {
        this.matchDay = matchDay;
        this.team1 = team1;
        this.team2 = team2;
    }

    // Getters

    public FantaTeam getTeam1() {
        return team1;
    }

    public FantaTeam getTeam2() {
        return team2;
    }
    
    public MatchDay getMatchDay() {
		return matchDay;
	}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(matchDay, match.matchDay) && Objects.equals(team1, match.team1) && Objects.equals(team2, match.team2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchDay, team1, team2);
    }
    
}

