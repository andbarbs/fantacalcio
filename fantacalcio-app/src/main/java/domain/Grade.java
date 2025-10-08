package domain;

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

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private NewsPaper newsPaper;

	protected Grade() {}
	
	public Grade(Player player, MatchDaySerieA matchDay, double mark, NewsPaper newsPaper) {
		this.player = player;
		this.matchDay = matchDay;
		this.mark = mark;
		this.newsPaper = newsPaper;
	}

	public double getMark() {
		return mark;
	}

	public Player getPlayer() {
		return player;
	}

	public NewsPaper getNewsPaper() {
		return newsPaper;
	}

	public MatchDaySerieA getMatchDay() {
		return matchDay;
	}

	@Override
	public int hashCode() {
		return Objects.hash(player, mark, matchDay);
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
		return Objects.equals(player, other.player)
				&& Double.doubleToLongBits(mark) == Double.doubleToLongBits(other.mark)
				&& Objects.equals(matchDay, other.matchDay);
	}	
	
}
