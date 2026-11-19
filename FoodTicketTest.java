/**
 * This class tests the FoodTicket class.
 */
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
public class FoodTicketTest {
    private FoodTicket ticket;
    /**
     * Sets up a FoodTicket object for testing.
     */
    public void setUp() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
    }
    /**
     * Tests the creation of a FoodTicket object with valid parameters.
     */
    @Test 
    public void testFoodTicketCreationException() {
        assertThrows(IllegalArgumentException.class, () -> new FoodTicket(1, null));
        List<String> emptyList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new FoodTicket(1, emptyList));
    }
    /**
     * Tests the getTicketId method of the FoodTicket class.
     */
    @Test 
    public void testGetTicketId() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        assertEquals(1, ticket.getTicketId());
    }

    /**
     * Tests the setTicketId method of the FoodTicket class.
     */
    @Test 
    public void testSetTicketId() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        ticket.setTicketId(2);
        assertEquals(2, ticket.getTicketId());
    }
/**
     * Tests the getItems method of the FoodTicket class.
     */
    @Test 
    public void testGetItems() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        List<String> items = ticket.getItems();
        assertEquals(2, items.size());
        assertTrue(items.contains("Burger"));
        assertTrue(items.contains("Fries"));
    }
    /**
     * Tests the isPaid method of the FoodTicket class.
     */
    @Test
    public void testPaid() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        assertFalse(ticket.isPaid());
        ticket.markAsPaid();
        assertTrue(ticket.isPaid());
    }
    /**
     * Tests the isPrepared method of the FoodTicket class.
     */
    @Test 
    public void testPrepared() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        assertFalse(ticket.isPrepared());
        ticket.markAsPaid();
        ticket.markAsPrepared();
        assertTrue(ticket.isPrepared());
    }
    /**
     * Tests the markAsPaid method of the FoodTicket class when the ticket is already paid.
     */
    @Test 
    public void testMarkAsPaidException() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        ticket.markAsPaid();
        assertThrows(IllegalStateException.class, () -> ticket.markAsPaid());
    }
    /**
     * Tests the markAsPrepared method of the FoodTicket class when the ticket is already prepared.
     */
    @Test
    public void testMarkAsPreparedException() {
        ticket = new FoodTicket(1, List.of("Burger", "Fries"));
        assertThrows(IllegalStateException.class, () -> ticket.markAsPrepared());
    }
}
