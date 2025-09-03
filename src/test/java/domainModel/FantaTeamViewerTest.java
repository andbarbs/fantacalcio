package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import domainModel.Player.Club;

public class FantaTeamViewerTest {

	@Test
    public void testFantaTeamViewerExtractors() {
        // Create players for each role.

        // Goalkeepers
        Goalkeeper keeper1 = new Goalkeeper("Manuel", "Neuer", Club.JUVENTUS);
        Goalkeeper keeper2 = new Goalkeeper("Alisson", "Becker", Club.JUVENTUS);
        Goalkeeper keeper3 = new Goalkeeper("David", "de Gea", Club.JUVENTUS);
        Goalkeeper keeper4 = new Goalkeeper("Ederson", "Motta", Club.JUVENTUS);
        Goalkeeper keeper5 = new Goalkeeper("Jan", "Oblak", Club.JUVENTUS);

        // Defenders
        Defender defender1 = new Defender("Virgil", "van Dijk", Club.JUVENTUS);
        Defender defender2 = new Defender("Sergio", "Ramos", Club.JUVENTUS);
        Defender defender3 = new Defender("Raphael", "Varane", Club.JUVENTUS);
        Defender defender4 = new Defender("Gerard", "Piqué", Club.JUVENTUS);
        Defender defender5 = new Defender("Thiago", "Silva", Club.JUVENTUS);

        // Midfielders
        Midfielder midfielder1 = new Midfielder("Luka", "Modrić", Club.TORINO);
        Midfielder midfielder2 = new Midfielder("Andrés", "Iniesta", Club.TORINO);
        Midfielder midfielder3 = new Midfielder("Kevin", "De Bruyne", Club.TORINO);
        Midfielder midfielder4 = new Midfielder("N'Golo", "Kanté", Club.TORINO);
        Midfielder midfielder5 = new Midfielder("Toni", "Kroos", Club.TORINO);

        // Forwards
        Forward forward1 = new Forward("Lionel", "Messi", Club.TORINO);
        Forward forward2 = new Forward("Cristiano", "Ronaldo", Club.TORINO);
        Forward forward3 = new Forward("Neymar", "Jr", Club.TORINO);
        Forward forward4 = new Forward("Robert", "Lewandowski", Club.TORINO);
        Forward forward5 = new Forward("Kylian", "Mbappé", Club.TORINO);

        // Create an empty set for contracts.
        Set<Contract> contracts = new HashSet<>();

        // Create FantaTeam (with new League() and new User() for simplicity)
        FantaTeam team = new FantaTeam("Elite Team", new League(), 0, new FantaUser(), contracts);

        // Add contracts.
        contracts.addAll(Stream.of(
        				keeper1, keeper2, keeper3, keeper4, keeper5,
        				defender1, defender2, defender3, defender4, defender5,
        				midfielder1, midfielder2, midfielder3, midfielder4, midfielder5,
        				forward1, forward2, forward3, forward4, forward5)
        	    .map(player -> new Contract(team, player))
        	    .collect(Collectors.toSet()));

        // Create the FantaTeamViewer (our SUT)
        FantaTeamViewer viewer = new FantaTeamViewer(team);

        // --- Assert that each extractor returns the proper players ---

        // Goalkeepers
        assertThat(viewer.goalkeepers())
            .containsExactlyInAnyOrder(keeper1, keeper2, keeper3, keeper4, keeper5);

        // Defenders
        assertThat(viewer.defenders())
            .containsExactlyInAnyOrder(defender1, defender2, defender3, defender4, defender5);

        // Midfielders
        assertThat(viewer.midfielders())
            .containsExactlyInAnyOrder(midfielder1, midfielder2, midfielder3, midfielder4, midfielder5);

        // Forwards
        assertThat(viewer.forwards())
            .containsExactlyInAnyOrder(forward1, forward2, forward3, forward4, forward5);
    }
}
