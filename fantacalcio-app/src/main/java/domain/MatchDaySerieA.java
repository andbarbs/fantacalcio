package domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MatchDaySerieA {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Basic(optional = false)
	private String name;

	@Basic(optional = false)
	private LocalDate date;

	@Basic(optional = false)
	private int number;

	protected MatchDaySerieA() {
	}

	public MatchDaySerieA(String name, LocalDate date, int number) {
		this.name = name;
		this.date = date;
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(date, name);
	}

	public int getNumber() {
		return number;
	}

	//TODO aggiungere number nell'equals e aggiustare test se necessario
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchDaySerieA other = (MatchDaySerieA) obj;
		return Objects.equals(date, other.date) && Objects.equals(name, other.name);
	}

}
