package jpaRepositories;

import java.util.Optional;

import businessLogic.repositories.ResultsRepository;
import domainModel.Match;
import domainModel.Result;
import jakarta.persistence.EntityManager;

public class JpaResultsRepository extends BaseJpaRepository implements ResultsRepository {

	public JpaResultsRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<Result> getResult(Match match) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public void saveResult(Result result) {
		// TODO Auto-generated method stub

	}

}
