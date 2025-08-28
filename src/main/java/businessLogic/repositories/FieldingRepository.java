package businessLogic.repositories;

import domainModel.Fielding;
import domainModel.LineUp;

import java.util.List;

public interface FieldingRepository {
	
    List<Fielding> getAllFieldings(LineUp lineUp);

}
