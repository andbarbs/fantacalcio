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
	private League league;

	protected Grade() {}
	
	public Grade(Player player, MatchDaySerieA matchDay, double mark) {
		this.player = player;
		this.matchDay = matchDay;
		this.mark = mark;
	}

	public double getMark() {
		return mark;
	}

	public Player getPlayer() {
		return player;
	}


    public MatchDaySerieA getMatchDay() {
		return matchDay;
	}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return Double.compare(mark, grade.mark) == 0 && Objects.equals(player, grade.player) && Objects.equals(matchDay, grade.matchDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, matchDay, mark);
    }
}
