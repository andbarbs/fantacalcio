package business.ports.repository;

import java.util.List;

import domain.Fielding;
import domain.LineUp;

public interface FieldingRepository {
	
    List<Fielding> getAllFieldings(LineUp lineUp);

}
