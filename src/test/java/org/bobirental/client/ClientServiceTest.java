package org.bobirental.client;

import org.bobirental.client.dto.ClientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest clientRequest;
    private Client mockClient;

    @BeforeEach
    public void setup() {
        clientRequest = new ClientRequest(
                "Jan",
                "Kowalski",
                "ul. Adresowa 5, 60-664 Poznań",
                "j.kowalski@mail.com"
        );

        setMockClient();
    }

    private void setMockClient() {
        mockClient = new Client();
        mockClient.setId(1);
        mockClient.setName("Jan");
        mockClient.setSurname("Kowalski");
        mockClient.setClientAddress("ul. Adresowa 5, 60-664 Poznań");
        mockClient.setClientMail("j.kowalski@mail.com");
    }

    // Test sprawdzający, czy poprawnie zapisano klienta do bazy danych
    // Asercja czy id nie jest null i czy jest równe 1
    // Weryfikacja interakcji z metodą save z obiektem Client
    @Test
    public void testSaveClient_ShouldReturnClientId_WithCorrectData() {
        // given zmockowany klient
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // when wywołanie saveClient()
        Integer result =  clientService.saveClient(clientRequest);

        // then asercja id
        assertNotNull(result);
        assertEquals(1, result);

        // sprawdzenie czy metoda save w client repository zapisała obiekt Client
        verify(clientRepository).save(any(Client.class));
    }

    // Test sprawdzający poprawność mapowania danych
    @Test
    public void testSaveClient_ShouldMapFieldsCorrectly() {
        // given zmockowany klient
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // when wywolanie metody saveClient()
        clientService.saveClient(clientRequest);

        // then sprawdzenie czy metoda save w client repository zapisuje obiekt Client z odpowiednimi argumentami
        verify(clientRepository).save(argThat(
                client ->
                    client.getName().equals("Jan") &&
                    client.getSurname().equals("Kowalski") &&
                    client.getClientAddress().equals("ul. Adresowa 5, 60-664 Poznań") &&
                    client.getClientMail().equals("j.kowalski@mail.com")
        ));
    }

    // Test sprawdzajacy czy klient moze zawrzec nowa umowe nie majac dlugu
    @Test
    public void tetsCanClientCreateAgreement_ShouldReturnTrue_WhenClientHasNoDebt() {
        // given klient nie ma zaległości i może utworzyć umowę
        when(clientRepository.canClientCreateAgreement(1)).thenReturn(true);

        // when wywolanie metody canClientCreateAgreement
        boolean canCreateAgreement = clientService.canClientCreateAgreement(1);

        // then sprawdzenie czy klient moze zawrzec umowe
        // oraz sprawdzenie wywolania metody
        assertTrue(canCreateAgreement);
        verify(clientRepository).canClientCreateAgreement(1);
    }

    // Test sprawdzajacy czy klient nie moze zawrzec nowej umowy majac dlug
    @Test
    public void testCanClientCreateAgreement_ShouldReturnFalse_WhenClientHasDebt() {
        // given klient nie ma zaległości i nie może utworzyć umowy
        when(clientRepository.canClientCreateAgreement(1)).thenReturn(false);

        // when wywolanie metody canClientCreateAgreement
        boolean canCreateAgreement = clientService.canClientCreateAgreement(1);

        // then sprawdzenie czy klient nie moze zawrzec umowy
        // oraz sprawdzenie wywolania metody
        assertFalse(canCreateAgreement);
        verify(clientRepository).canClientCreateAgreement(1);
    }

    // Test sprawdzajacy czy metoda getDebt zwraca poprawną wartosc dlugu
    @Test
    public void testGetDebt_ShouldReturnCorrectAmount_WhenClientHasDebt() {
        // Given
        BigDecimal expectedDebt = new BigDecimal("150.50");
        when(clientRepository.getDebtById(1)).thenReturn(expectedDebt);

        // When
        BigDecimal result = clientService.getDebt(1);

        // Then
        assertNotNull(result);
        assertEquals(expectedDebt, result);

        verify(clientRepository).getDebtById(1);
    }

    // Test sprawdzający czy metoda getDebt zwraca zero w przypadku braku dlugu
    @Test
    public void testGetDebt_ShouldReturnZero_WhenClientHasNoDebt() {
        // Given
        when(clientRepository.getDebtById(1)).thenReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = clientService.getDebt(1);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);

        verify(clientRepository).getDebtById(1);
    }

    // Test sprawdzajacy poprawnosc pobierania sumy oplat klienta
    @Test
    public void testGetFees_ShouldReturnSumOfFees() {
        // given
        BigDecimal expectedFees = new BigDecimal("150.50");
        when(clientRepository.getFeesById(1)).thenReturn(expectedFees);

        // when
        BigDecimal result = clientService.getFees(1);

        // then
        assertNotNull(result);
        assertEquals(expectedFees, result);

        verify(clientRepository).getFeesById(1);
    }

    @Test
    public void testGetFees_ShouldReturnZero_WhenClientHasNoFees() {
        // given
        when(clientRepository.getFeesById(1)).thenReturn(BigDecimal.ZERO);

        // when
        BigDecimal result = clientService.getFees(1);

        // then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);

        verify(clientRepository).getFeesById(1);
    }
}
