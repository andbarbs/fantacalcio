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
    public static enum Status {PAST, PRESENT, FUTURE}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Basic(optional = false)
	private String name;

	@Basic(optional = false)
	private int number;

    @Basic(optional = false)
    private Status status;

	protected MatchDaySerieA() {
	}

    //TODO devo controllare che number sia compreso tra 1 e 20?
	public MatchDaySerieA(String name, int number, Status status) {
		this.name = name;
		this.number = number;
        this.status = status;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MatchDaySerieA that = (MatchDaySerieA) o;
        return number == that.number && Objects.equals(name, that.name) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number, status);
    }
}
