package domain;

import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class MatchDay {
	public static final int MATCH_DAYS_IN_LEAGUE = 20;
	public enum Status {PAST, PRESENT, FUTURE}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Basic(optional = false)
	private String name;
	
	@Basic(optional = false)
	private int number;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	private League league;
	
	protected MatchDay() {
	}
	
	public MatchDay(String name, int number, Status status, League league) {
		if(number < 0 || number > MATCH_DAYS_IN_LEAGUE)
			throw new IllegalArgumentException("number out of range");
		this.name = name;
		this.number = number;
		this.status = status;
		this.league = league;
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return number;
	}
	
	public League getLeague() {
		return league;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		MatchDay that = (MatchDay) o;
		return number == that.number && Objects.equals(name, that.name) && status == that.status && Objects.equals(league, that.league);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, number, status, league);
	}
}
