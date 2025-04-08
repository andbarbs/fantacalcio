package businessLogic.abstractDAL;

import java.util.Iterator;

public interface NewspaperDAO {
    Iterator<NewspaperDAO> getAllNewspapers();
    void save();

}
