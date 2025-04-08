package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional = false)
    double Team1Points;

    @Basic(optional = false)
    double Team2Points;

    @Basic(optional = false)
    int Team1Goals;

    @Basic(optional = false)
    int Team2Goals;

    public Result() {}

    public Result(double team1Points, double team2Points, int team1Goals, int team2Goals) {
        Team1Points = team1Points;
        Team2Points = team2Points;
        Team1Goals = team1Goals;
        Team2Goals = team2Goals;
    }

    public double getTeam1Points() {
        return Team1Points;
    }

    public double getTeam2Points() {
        return Team2Points;
    }

    public int getTeam1Goals() {
        return Team1Goals;
    }

    public int getTeam2Goals() {
        return Team2Goals;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Double.compare(Team1Points, result.Team1Points) == 0 && Double.compare(Team2Points, result.Team2Points) == 0 && Team1Goals == result.Team1Goals && Team2Goals == result.Team2Goals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Team1Points, Team2Points, Team1Goals, Team2Goals);
    }
}
