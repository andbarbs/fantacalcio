package businessLogic.repositories;

import domainModel.LineUp;

import java.util.List;

public interface LineUpRepository {
    List<LineUp> getLineUps();
    void saveLineUp(LineUp lineUp);
    void deleteLineUp(LineUp lineUp);
}
