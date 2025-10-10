package business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import business.ports.repository.FantaUserRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.FantaUser;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
class LoginServiceTest {

    private TransactionManager transactionManager;
    private TransactionContext context;
    private LoginService service;

    private FantaUserRepository fantaUserRepository;
    private NewsPaperRepository newsPaperRepository;

    @BeforeEach
    void setUp() {
        transactionManager = mock(TransactionManager.class);
        context = mock(TransactionContext.class);
        
		// For inTransaction
		doAnswer(invocation -> {
			Consumer<TransactionContext> code = invocation.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		// For fromTransaction
		when(transactionManager.fromTransaction(any())).thenAnswer(invocation -> {
			Function<TransactionContext, Object> code = invocation.getArgument(0);
			return code.apply(context);
		});

        // Mock repositories
        fantaUserRepository = mock(FantaUserRepository.class);
        newsPaperRepository = mock(NewsPaperRepository.class);

        when(context.getFantaUserRepository()).thenReturn(fantaUserRepository);
        when(context.getNewspaperRepository()).thenReturn(newsPaperRepository);

        service = new LoginService(transactionManager);
    }

    @Test
    void testRegisterFantaUser_SavesWhenNotExists() {
        when(fantaUserRepository.getUser("mail", "pswd")).thenReturn(Optional.empty());

        service.registerFantaUser("mail", "pswd");

        verify(fantaUserRepository).saveFantaUser(argThat(user ->
                user.getEmail().equals("mail") && user.getPassword().equals("pswd")));
    }

    @Test
    void testRegisterFantaUser_ThrowsWhenAlreadyExists() {
        when(fantaUserRepository.getUser("mail", "pswd"))
                .thenReturn(Optional.of(new FantaUser("mail", "pswd")));

        assertThatThrownBy(() -> service.registerFantaUser("mail", "pswd"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("already registered");

        verify(fantaUserRepository, never()).saveFantaUser(any());
    }

    @Test
    void testRegisterNewsPaper_SavesWhenNotExists() {
        when(newsPaperRepository.getNewspaper("Gazzetta")).thenReturn(Optional.empty());

        service.registerNewsPaper("Gazzetta");

        verify(newsPaperRepository).saveNewsPaper(argThat(np -> np.getName().equals("Gazzetta")));
    }

    @Test
    void testRegisterNewsPaper_DoesNothingWhenAlreadyExists() {
        when(newsPaperRepository.getNewspaper("Gazzetta"))
                .thenReturn(Optional.of(new NewsPaper("Gazzetta")));

        assertThatThrownBy(() -> service.registerNewsPaper("Gazzetta"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("already registered");
        
        verify(newsPaperRepository, never()).saveNewsPaper(any());
    }

    @Test
    void testLoginFantaUser_ReturnsTrueWhenPresent() {
        when(fantaUserRepository.getUser("mail", "pswd"))
                .thenReturn(Optional.of(new FantaUser("mail", "pswd")));

        assertThat(service.loginFantaUser("mail", "pswd")).isTrue();
    }

    @Test
    void testLoginFantaUser_ReturnsFalseWhenNotPresent() {
        when(fantaUserRepository.getUser("mail", "pswd"))
                .thenReturn(Optional.empty());

        assertThat(service.loginFantaUser("mail", "pswd")).isFalse();
    }

    @Test
    void testLoginNewsPaper_ReturnsTrueWhenPresent() {
        when(newsPaperRepository.getNewspaper("Gazzetta"))
                .thenReturn(Optional.of(new NewsPaper("Gazzetta")));

        assertThat(service.loginNewsPaper("Gazzetta")).isTrue();
    }

    @Test
    void testLoginNewsPaper_ReturnsFalseWhenNotPresent() {
        when(newsPaperRepository.getNewspaper("Gazzetta"))
                .thenReturn(Optional.empty());

        assertThat(service.loginNewsPaper("Gazzetta")).isFalse();
    }
}
