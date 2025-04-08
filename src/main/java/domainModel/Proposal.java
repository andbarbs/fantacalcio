package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Proposal {
    public static enum Status {PENDING, ACCEPTED, REJECTED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract offedredContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract requestedContract;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public Proposal() {}

    public Proposal(Contract offedredContract, Contract requestedContract, Status status) {
        this.offedredContract = offedredContract;
        this.requestedContract = requestedContract;
        this.status = status;
    }

    public Contract getOffedredContract() {
        return offedredContract;
    }

    public Contract getRequestedContract() {
        return requestedContract;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(offedredContract, proposal.offedredContract) && Objects.equals(requestedContract, proposal.requestedContract) && status == proposal.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offedredContract, requestedContract, status);
    }
}

