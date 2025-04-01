package businessLogic.abstractDAL;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.Player;

import java.util.Iterator;

public interface ContractsDAO {
    Iterator<Contract> getSquad(FantaTeam team);
    Iterator<Contract> getContracts(FantaTeam team, Player.Role role);
    void save(FantaTeam team, Player player);
    void delete(Contract contract);
}
