package domainModel;

import java.util.Objects;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Grade {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Player player;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private MatchDaySerieA matchDay;
	
	@Basic(optional=false)
	private double mark;
	
	@Basic(optional=false)
	private int goals;
	
	@Basic(optional=false)
	private int assists;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private NewsPaper newsPaper;

	public Grade() {}
	
	public Grade(Player player, MatchDaySerieA matchDay, double mark, int goals, int assists, NewsPaper newsPaper) {
		this.player = player;
		this.matchDay = matchDay;
		this.mark = mark;
		this.goals = goals;
		this.assists = assists;
		this.newsPaper = newsPaper;
	}

	public double getMark() {
		return mark;
	}

	public Player getGiocatore() {
		return player;
	}

	public int getGoals() {
		return goals;
	}

	public int getAssists() {
		return assists;
	}

	public NewsPaper getNewsPaper() {
		return newsPaper;
	}

	@Override
	public int hashCode() {
		return Objects.hash(assists, player, goals, mark, matchDay);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grade other = (Grade) obj;
		return assists == other.assists && Objects.equals(player, other.player) && goals == other.goals
				&& Double.doubleToLongBits(mark) == Double.doubleToLongBits(other.mark)
				&& Objects.equals(matchDay, other.matchDay);
	}	
	
}
