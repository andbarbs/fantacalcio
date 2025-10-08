package domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public abstract class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract offeredContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract requestedContract;

    protected Proposal() {}

    protected Proposal(Contract offeredContract, Contract requestedContract) {
        this.offeredContract = offeredContract;
        this.requestedContract = requestedContract;
    }

    public Contract getOfferedContract() {
        return offeredContract;
    }

    public Contract getRequestedContract() {
        return requestedContract;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(offeredContract, proposal.offeredContract) && Objects.equals(requestedContract, proposal.requestedContract) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offeredContract, requestedContract);
    }

    @Entity
    public static class PendingProposal extends Proposal {
        public PendingProposal() {}
        public PendingProposal(Contract offeredContract, Contract requestedContract) {
            super(offeredContract, requestedContract);
        }
    }

    @Entity
    public static class RejectedProposal extends Proposal {
        public RejectedProposal() {}
        public RejectedProposal(Contract offeredContract, Contract requestedContract) {
            super(offeredContract, requestedContract);
        }
    }
}

