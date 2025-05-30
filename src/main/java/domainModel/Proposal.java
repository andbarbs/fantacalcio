package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public abstract class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract offedredContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY )
    private Contract requestedContract;

    protected Proposal() {}

    protected Proposal(Contract offedredContract, Contract requestedContract) {
        this.offedredContract = offedredContract;
        this.requestedContract = requestedContract;
    }

    public Contract getOffedredContract() {
        return offedredContract;
    }

    public Contract getRequestedContract() {
        return requestedContract;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Objects.equals(offedredContract, proposal.offedredContract) && Objects.equals(requestedContract, proposal.requestedContract) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offedredContract, requestedContract);
    }

    @Entity
    public static class PendingProposal extends Proposal {
        public PendingProposal() {}
        public PendingProposal(Contract offedredContract, Contract requestedContract) {
            super(offedredContract, requestedContract);
        }
    }

    @Entity
    public static class RejectedProposal extends Proposal {
        public RejectedProposal() {}
        public RejectedProposal(Contract offedredContract, Contract requestedContract) {
            super(offedredContract, requestedContract);
        }
    }
}

