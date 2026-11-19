import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class DriveThruControllerTest {

    private DriveThruSystem system;
    private TestView view;
    private DriveThruController controller;

    private static final List<String> VALID_ITEMS =
        Arrays.asList("Burger", "Fries");

    /**
     * A View that records calls instead of printing to the console.
     */
    private static class TestView extends DriveThruView {

        int renderCount;
        int errorCount;
        int messageCount;

        List<Car> lastCars;
        List<FoodTicket> lastTickets;

        String lastError;
        String lastMessage;

        @Override
        public void render(List<Car> cars, List<FoodTicket> tickets) {
            renderCount++;
            lastCars = cars;
            lastTickets = tickets;
        }

        @Override
        public void showError(String message) {
            errorCount++;
            lastError = message;
        }

        @Override
        public void showMessage(String message) {
            messageCount++;
            lastMessage = message;
        }
    }

    /**
     * System that throws an exception when adding a car.
     */
    private static class ThrowingAddCarSystem extends DriveThruSystem {

        @Override
        public void addCar(Car car) {
            throw new IllegalArgumentException("Test exception");
        }
    }

    /**
     * System that throws an IllegalStateException when creating an order.
     */
    private static class ThrowingOrderSystem extends DriveThruSystem {

        @Override
        public FoodTicket createOrder(String carId, List<String> items) {
            throw new IllegalStateException("Test exception");
        }
    }

    /**
     * System that throws an IllegalStateException when delivering.
     */
    private static class ThrowingDeliverSystem extends DriveThruSystem {

        @Override
        public Car deliverOrder() {
            throw new IllegalStateException("Test exception");
        }
    }

    @Before
    public void setUp() {
        system = new DriveThruSystem();
        view = new TestView();
        controller = new DriveThruController(system, view);
    }

    // ----------------------------------------------------------
    // Constructor Tests

    @Test
    public void testConstructor() {
        assertNotNull(controller);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSystem() {
        new DriveThruController(null, view);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullView() {
        new DriveThruController(system, null);
    }

    // ----------------------------------------------------------
    // addCar Tests

    @Test
    public void testAddCar() {
        controller.addCar("Car1");

        assertEquals(1, system.getCarSnapshot().size());
        assertEquals("Car1",
            system.getCarSnapshot().get(0).getCarId());

        assertEquals(1, view.renderCount);
        assertEquals(0, view.errorCount);
    }


    @Test
    public void testAddCarSystemThrowsException() {
        DriveThruSystem throwingSystem = new ThrowingAddCarSystem();

        DriveThruController testController =
            new DriveThruController(throwingSystem, view);

        testController.addCar("Car1");

        assertEquals(1, view.errorCount);
        assertEquals("Invalid or duplicate car ID.", view.lastError);
        assertEquals(0, view.renderCount);
    }

    // ----------------------------------------------------------
    // enterOrder Tests

    @Test
    public void testEnterOrder() {
        controller.addCar("Car1");

        int previousRenderCount = view.renderCount;

        controller.enterOrder("Car1", VALID_ITEMS);

        assertEquals(1, system.getTicketSnapshot().size());

        FoodTicket ticket = system.getTicketSnapshot().get(0);

        assertEquals(1, ticket.getTicketId());
        assertEquals(VALID_ITEMS, ticket.getItems());

        assertFalse(ticket.isPaid());
        assertFalse(ticket.isPrepared());

        assertEquals(1, view.messageCount);
        assertEquals(
            "Order created successfully. Ticket ID: 1",
            view.lastMessage
        );

        assertEquals(previousRenderCount + 1, view.renderCount);
    }

    @Test
    public void testEnterOrderCarNotFound() {
        controller.enterOrder("MissingCar", VALID_ITEMS);

        assertEquals(0, system.getTicketSnapshot().size());

        assertEquals(1, view.errorCount);
        assertEquals("No ticket is created.", view.lastError);

        assertEquals(1, view.messageCount);
        assertTrue(view.lastMessage.contains("Available menu:"));

        assertEquals(0, view.renderCount);
    }

    @Test
    public void testEnterOrderEmptyItems() {
        controller.addCar("Car1");

        int previousRenderCount = view.renderCount;

        controller.enterOrder("Car1", Arrays.asList());

        assertEquals(0, system.getTicketSnapshot().size());
        assertEquals(1, view.errorCount);
        assertEquals(1, view.messageCount);

        assertEquals(previousRenderCount, view.renderCount);
    }

    @Test
    public void testEnterOrderNullItems() {
        controller.addCar("Car1");

        controller.enterOrder("Car1", null);

        assertEquals(0, system.getTicketSnapshot().size());
        assertEquals(1, view.errorCount);
        assertEquals(1, view.messageCount);
    }


    @Test
    public void testEnterOrderIllegalStateException() {
        DriveThruSystem throwingSystem = new ThrowingOrderSystem();

        DriveThruController testController =
            new DriveThruController(throwingSystem, view);

        testController.enterOrder("Car1", VALID_ITEMS);

        assertEquals(1, view.errorCount);
        assertEquals("No ticket is created.", view.lastError);

        assertEquals(1, view.messageCount);
        assertTrue(view.lastMessage.contains("Available menu:"));
    }

    // ----------------------------------------------------------
    // markPaid Tests

    @Test
    public void testMarkPaid() {
        controller.addCar("Car1");
        controller.enterOrder("Car1", VALID_ITEMS);

        int previousRenderCount = view.renderCount;

        controller.markPaid(1);

        FoodTicket ticket = system.getTicketSnapshot().get(0);

        assertTrue(ticket.isPaid());
        assertFalse(ticket.isPrepared());

        assertEquals(previousRenderCount + 1, view.renderCount);
        assertEquals(0, view.errorCount);
    }

    @Test
    public void testMarkPaidTicketNotFound() {
        controller.markPaid(999);

        assertEquals(1, view.errorCount);
        assertEquals("Ticket not found.", view.lastError);
        assertEquals(0, view.renderCount);
    }

    // ----------------------------------------------------------
    // markPrepared Tests

    @Test
    public void testMarkPrepared() {
        controller.addCar("Car1");
        controller.enterOrder("Car1", VALID_ITEMS);

        controller.markPaid(1);

        int previousRenderCount = view.renderCount;

        controller.markPrepared(1);

        FoodTicket ticket = system.getTicketSnapshot().get(0);

        assertTrue(ticket.isPaid());
        assertTrue(ticket.isPrepared());

        assertEquals(previousRenderCount + 1, view.renderCount);
        assertEquals(0, view.errorCount);
    }

    @Test
    public void testMarkPreparedTicketNotFound() {
        controller.markPrepared(999);

        assertEquals(1, view.errorCount);
        assertEquals("Ticket not found.", view.lastError);
        assertEquals(0, view.renderCount);
    }

    @Test(expected = IllegalStateException.class)
    public void testMarkPreparedBeforePayment() {
        controller.addCar("Car1");
        controller.enterOrder("Car1", VALID_ITEMS);

        controller.markPrepared(1);
    }

    // ----------------------------------------------------------
    // deliver Tests


    @Test
    public void testDeliverIllegalStateException() {
        DriveThruSystem throwingSystem = new ThrowingDeliverSystem();

        DriveThruController testController =
            new DriveThruController(throwingSystem, view);

        testController.deliver();

        assertEquals(1, view.errorCount);

        assertTrue(view.lastError.contains("There are no orders"));
        assertEquals(0, view.renderCount);
    }

    @Test(expected = NoSuchElementException.class)
    public void testDeliverEmptyQueue() {
        controller.deliver();
    }

    @Test(expected = NullPointerException.class)
    public void testDeliverOrderNotReady() {
        controller.addCar("Car1");
        controller.enterOrder("Car1", VALID_ITEMS);

        controller.deliver();
    }

    // ----------------------------------------------------------
    // showError Tests

    @Test
    public void testShowError() {
        controller.showError("Test error message");

        assertEquals(1, view.errorCount);
        assertEquals("Test error message", view.lastError);
    }

    @Test
    public void testShowErrorMultipleTimes() {
        controller.showError("Error 1");
        controller.showError("Error 2");

        assertEquals(2, view.errorCount);
        assertEquals("Error 2", view.lastError);
    }

    // ----------------------------------------------------------
    // Refresh / View Interaction Tests

    @Test
    public void testRefreshAfterMultipleOperations() {
        controller.addCar("Car1");

        assertEquals(1, view.renderCount);
        assertEquals(1, view.lastCars.size());
        assertEquals(0, view.lastTickets.size());

        controller.enterOrder("Car1", VALID_ITEMS);

        assertEquals(2, view.renderCount);
        assertEquals(1, view.lastCars.size());
        assertEquals(1, view.lastTickets.size());

        controller.markPaid(1);

        assertEquals(3, view.renderCount);

        controller.markPrepared(1);

        assertEquals(4, view.renderCount);
    }

    @Test
    public void testFailedOperationDoesNotRefresh() {
        controller.addCar("Car1");

        int previousRenderCount = view.renderCount;

        controller.markPaid(999);

        assertEquals(previousRenderCount, view.renderCount);
    }
    
    @Test
    public void testDeliverShowsMessageAndRefreshesView() {
        controller.addCar("Car1");
        controller.enterOrder("Car1", VALID_ITEMS);
        controller.markPaid(1);
        controller.markPrepared(1);

        int previousMessageCount = view.messageCount;
        int previousRenderCount = view.renderCount;

        controller.deliver();

        // Check showMessage()
        assertEquals(previousMessageCount + 1, view.messageCount);
        assertEquals("Delivered car #Car1", view.lastMessage);

        // Check refresh()
        assertEquals(previousRenderCount + 1, view.renderCount);
        assertEquals(0, view.lastCars.size());
        assertEquals(0, view.lastTickets.size());
    }
}