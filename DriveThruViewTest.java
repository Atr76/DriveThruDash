import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Edge case tests for DriveThruView.
 * Adjust the two factory helpers to match your Car / FoodTicket constructors.
 */
class DriveThruViewTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;
    private DriveThruView view;

    private static Car car(int carId, Integer ticketId) {
        Car car = new Car(String.valueOf(carId));
        car.setTicketId(ticketId);
        return car;
    }

    private static FoodTicket ticket(Integer ticketId, boolean paid, boolean prepared) {
        FoodTicket ticket = new FoodTicket(ticketId, List.of("Burger"));
        if (paid) {
            ticket.markAsPaid();
        }
        if (prepared) {
            if (!paid) {
                ticket.markAsPaid();
            }
            ticket.markAsPrepared();
        }
        return ticket;
    }

    @BeforeEach
    void setUp() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        view = new DriveThruView();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String out() {
        return captured.toString().replace("\r\n", "\n");
    }

    private static List<Car> cars(Car... cs) {
        return new ArrayList<>(List.of(cs));
    }

    private static List<FoodTicket> tickets(FoodTicket... ts) {
        return new ArrayList<>(List.of(ts));
    }

    @Test
    void nullCarsThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> view.render(null, new ArrayList<>()));
        assertEquals("cars is null", e.getMessage());
    }

    @Test
    void nullTicketsThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> view.render(new ArrayList<>(), null));
        assertEquals("tickets is null", e.getMessage());
    }

    @Test
    void emptyQueuesRenderEmptyState() {
        view.render(new ArrayList<>(), new ArrayList<>());
        String expected =
                "Window 1: No cars waiting to order\n" +
                "Window 2: No cars waiting\n" +
                "Waiting to order: 0\n" +
                "~~~~~ Car queue (0) ~~~~~\n" +
                "empty\n" +
                "~~~~~ Ticket queue (0) ~~~~~\n" +
                "empty\n";
        assertEquals(expected, out());
    }

    @Test
    void window1PicksFirstCarWithoutTicketNotFirstInQueue() {
        view.render(cars(car(1, 10), car(2, null), car(3, null)),
                tickets(ticket(10, true, false)));
        assertTrue(out().contains("Window 1: car 2 is ordering"));
    }

    @Test
    void window1NoCarsWaitingWhenAllHaveTickets() {
        view.render(cars(car(1, 10), car(2, 11)),
                tickets(ticket(10, true, true), ticket(11, false, false)));
        assertTrue(out().contains("Window 1: No cars waiting to order"));
        assertTrue(out().contains("Waiting to order: 0"));
    }

    @Test
    void window2NoTicketWhenHeadHasNullTicketId() {
        view.render(cars(car(1, null), car(2, 10)), tickets(ticket(10, true, true)));
        assertTrue(out().contains("Window 2: car 1\nNo ticket\n"));
    }

    @Test
    void window2TicketNotFound() {
        view.render(cars(car(1, 99)), tickets(ticket(10, true, true)));
        assertTrue(out().contains("Window 2: car 1\nTicket not found\n"));
    }

    @Test
    void window2FindsMatchingTicketNotAtFrontOfList() {
        view.render(cars(car(1, 12)),
                tickets(ticket(10, false, false), ticket(12, true, false)));
        assertTrue(out().contains("Window 2: car 1\npaid: true prepared: false\n"));
    }

    @Test
    void duplicateTicketIdsUseFirstMatch() {
        view.render(cars(car(1, 10)),
                tickets(ticket(10, true, false), ticket(10, false, true)));
        assertTrue(out().contains("Window 2: car 1\npaid: true prepared: false\n"));
    }

    @Test
    void waitingCountOnlyCountsCarsWithoutTickets() {
        view.render(cars(car(1, 10), car(2, null), car(3, 11), car(4, null)),
                tickets(ticket(10, true, true), ticket(11, true, true)));
        assertTrue(out().contains("Waiting to order: 2\n"));
    }

    @Test
    void queueListingsAreOneIndexedAndShowTicketInfo() {
        view.render(cars(car(4, 10), car(9, null)),
                tickets(ticket(10, true, false), ticket(77, false, false)));
        String o = out();
        assertTrue(o.contains("1. car 4 (ticket 10)\n"));
        assertTrue(o.contains("2. car 9 (No ticket yet)\n"));
        assertTrue(o.contains("1. ticket 10 | paid: true prepared: false\n"));
        assertTrue(o.contains("2. ticket 77 | paid: false prepared: false\n"));
    }

    @Test
    void emptyCarQueueWithNonEmptyTicketQueue() {
        view.render(cars(), tickets(ticket(10, true, false)));
        String o = out();
        assertTrue(o.contains("~~~~~ Car queue (0) ~~~~~\nempty\n"));
        assertTrue(o.contains("~~~~~ Ticket queue (1) ~~~~~\n1. ticket 10"));
    }

    @Test
    void renderDoesNotMutateInputs() {
        List<Car> cs = cars(car(1, 10), car(2, null));
        List<FoodTicket> ts = tickets(ticket(10, true, false));
        List<Car> csCopy = new ArrayList<>(cs);
        List<FoodTicket> tsCopy = new ArrayList<>(ts);
        view.render(cs, ts);
        assertEquals(csCopy, cs);
        assertEquals(tsCopy, ts);
    }

    @Test
    void showErrorPrefixesMessage() {
        view.showError("bad input");
        assertEquals("Error: bad input\n", out());
    }

    @Test
    void showMessagePrintsExactly() {
        view.showMessage("hello");
        assertEquals("hello\n", out());
    }
    @Test
    void renderOrderIsCorrectAfterInterleavedEnqueuesAndDequeues() {
        List<Car> cs = cars(car(1, null));
        List<FoodTicket> ts = tickets();

        cs.add(car(2, null));            // enqueue car 2
        cs.get(0).setTicketId(10);       // car 1 orders
        ts.add(ticket(10, false, false));
        cs.remove(0);                    // dequeue car 1
        cs.add(car(3, null));            // enqueue car 3

        view.render(cs, ts);
        String o = out();
        assertTrue(o.contains("1. car 2 (No ticket yet)\n"));
        assertTrue(o.contains("2. car 3 (No ticket yet)\n"));
        assertTrue(o.contains("Window 1: car 2 is ordering"));
        assertTrue(o.contains("Window 2: car 2\nNo ticket\n"));
        assertTrue(o.contains("Waiting to order: 2\n"));
    }
}
