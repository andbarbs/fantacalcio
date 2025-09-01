package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional = false)
    double team1Points;

    @Basic(optional = false)
    double team2Points;

    @Basic(optional = false)
    int team1Goals;

    @Basic(optional = false)
    int team2Goals;

    @OneToOne(optional = false)
	private Match match;

    protected Result() {}

    public Result(double team1Points, double team2Points, int team1Goals, int team2Goals, Match match) {
    	this.match = match;
        this.team1Points = team1Points;
        this.team2Points = team2Points;
        this.team1Goals = team1Goals;
        this.team2Goals = team2Goals;
    }

    public double getTeam1Points() {
        return team1Points;
    }

    public double getTeam2Points() {
        return team2Points;
    }

    public int getTeam1Goals() {
        return team1Goals;
    }

    public int getTeam2Goals() {
        return team2Goals;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Double.compare(team1Points, result.team1Points) == 0 && Double.compare(team2Points, result.team2Points) == 0 && team1Goals == result.team1Goals && team2Goals == result.team2Goals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team1Points, team2Points, team1Goals, team2Goals);
    }
}
