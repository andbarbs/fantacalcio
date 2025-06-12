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

public class FantaTeamViewerTest {

	@Test
    public void testFantaTeamViewerExtractors() {
        // Create players for each role.

        // Goalkeepers
        Goalkeeper keeper1 = new Goalkeeper("Manuel", "Neuer");
        Goalkeeper keeper2 = new Goalkeeper("Alisson", "Becker");
        Goalkeeper keeper3 = new Goalkeeper("David", "de Gea");
        Goalkeeper keeper4 = new Goalkeeper("Ederson", "Motta");
        Goalkeeper keeper5 = new Goalkeeper("Jan", "Oblak");

        // Defenders
        Defender defender1 = new Defender("Virgil", "van Dijk");
        Defender defender2 = new Defender("Sergio", "Ramos");
        Defender defender3 = new Defender("Raphael", "Varane");
        Defender defender4 = new Defender("Gerard", "Piqué");
        Defender defender5 = new Defender("Thiago", "Silva");

        // Midfielders
        Midfielder midfielder1 = new Midfielder("Luka", "Modrić");
        Midfielder midfielder2 = new Midfielder("Andrés", "Iniesta");
        Midfielder midfielder3 = new Midfielder("Kevin", "De Bruyne");
        Midfielder midfielder4 = new Midfielder("N'Golo", "Kanté");
        Midfielder midfielder5 = new Midfielder("Toni", "Kroos");

        // Forwards
        Forward forward1 = new Forward("Lionel", "Messi");
        Forward forward2 = new Forward("Cristiano", "Ronaldo");
        Forward forward3 = new Forward("Neymar", "Jr");
        Forward forward4 = new Forward("Robert", "Lewandowski");
        Forward forward5 = new Forward("Kylian", "Mbappé");

        // Create an empty set for contracts.
        Set<Contract> contracts = new HashSet<>();

        // Create FantaTeam (with new League() and new User() for simplicity)
        FantaTeam team = new FantaTeam("Elite Team", new League(), 0, new User(), contracts);

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
        assertThat(viewer.Defenders())
            .containsExactlyInAnyOrder(defender1, defender2, defender3, defender4, defender5);

        // Midfielders
        assertThat(viewer.Midfielders())
            .containsExactlyInAnyOrder(midfielder1, midfielder2, midfielder3, midfielder4, midfielder5);

        // Forwards
        assertThat(viewer.Forwards())
            .containsExactlyInAnyOrder(forward1, forward2, forward3, forward4, forward5);
    }
}
