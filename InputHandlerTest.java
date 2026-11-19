import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for InputHandler covering constructor validation,
 * normal command parsing (add, order, pay, prep, deliver, cancel, quit),
 * command aliases, and bad-input error handling.
 */
public class InputHandlerTest {

    private SpyView spyView;
    private SpyController spyController;

    /**
     * Minimal DriveThruView spy to capture error messages without console clutter.
     */
    private static class SpyView extends DriveThruView {
        final List<String> errors = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        @Override
        public void showError(String message) {
            errors.add(message);
        }

        @Override
        public void showMessage(String message) {
            messages.add(message);
        }

        @Override
        public void render(List<Car> cars, List<FoodTicket> tickets) {
            // No-op during InputHandler unit tests
        }
    }

    /**
     * DriveThruController spy that records which controller methods InputHandler invokes
     * and with what arguments, isolating InputHandler from Model state.
     */
    private static class SpyController extends DriveThruController {
        final List<String> recordedCalls = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        public SpyController(DriveThruSystem system, DriveThruView view) {
            super(system, view);
        }

        @Override
        public void addCar(String carId) {
            recordedCalls.add("addCar:" + carId);
        }

        @Override
        public void enterOrder(String carId, List<String> items) {
            recordedCalls.add("enterOrder:" + carId + ":" + items);
        }

        @Override
        public void markPaid(int ticketId) {
            recordedCalls.add("markPaid:" + ticketId);
        }

        @Override
        public void markPrepared(int ticketId) {
            recordedCalls.add("markPrepared:" + ticketId);
        }

        @Override
        public void deliver() {
            recordedCalls.add("deliver");
        }

        @Override
        public void showError(String message) {
            errors.add(message);
        }
    }

    @BeforeEach
    public void setUp() {
        spyView = new SpyView();
        spyController = new SpyController(new DriveThruSystem(), spyView);
    }

    /**
     * Helper method to build an InputHandler fed by a simulated multi-line CLI string.
     */
    private InputHandler createHandlerWithInput(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        return new InputHandler(spyController, in);
    }

    /**
     * Runs the given input and asserts that nothing reached the controller
     * and exactly one error was reported.
     */
    private void assertSingleError(String input) {
        createHandlerWithInput(input).readInput();
        assertTrue(spyController.recordedCalls.isEmpty(),
            "No controller method should be called for: " + input);
        assertEquals(1, spyController.errors.size(),
            "Exactly one error expected for: " + input);
    }

    // =========================================================================
    // Constructor Tests
    // =========================================================================

    @Test
    public void testConstructorNullControllerThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new InputHandler(null);
        });

        InputStream validStream = new ByteArrayInputStream("quit\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> {
            new InputHandler(null, validStream);
        });
    }

    @Test
    public void testConstructorNullInputStreamThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new InputHandler(spyController, null);
        });
    }

    @Test
    public void testSingleArgConstructorSucceeds() {
        assertDoesNotThrow(() -> new InputHandler(spyController));
    }

    // =========================================================================
    // Normal Command Tests
    // =========================================================================

    @Test
    public void testAddCarCommandAndAliases() {
        String input = String.join("\n",
            "add C101",
            "addcar C102",
            "newcar C103",
            "CAR C104",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "addCar:C101",
            "addCar:C102",
            "addCar:C103",
            "addCar:C104"
        ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testEnterOrderCommandAndAliases() {
        String input = String.join("\n",
            "order C101 Burger Fries Soda",
            "enterorder C102 Burger",
            "makeorder C103 Fries Soda",
            "neworder C104 Soda",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "enterOrder:C101:[Burger, Fries, Soda]",
            "enterOrder:C102:[Burger]",
            "enterOrder:C103:[Fries, Soda]",
            "enterOrder:C104:[Soda]"
        ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testMarkPaidCommandAndAliases() {
        String input = String.join("\n",
            "pay 1",
            "markpaid 2",
            "payorder 3",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "markPaid:1",
            "markPaid:2",
            "markPaid:3"
        ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testMarkPreparedCommandAndAliases() {
        String input = String.join("\n",
            "prep 1",
            "prepare 2",
            "markprepared 3",
            "prepareorder 4",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "markPrepared:1",
            "markPrepared:2",
            "markPrepared:3",
            "markPrepared:4"
        ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testDeliverCommandAndAlias() {
        String input = String.join("\n",
            "deliver",
            "deliverorder",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "deliver",
            "deliver"
        ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testCommandsAreCaseInsensitiveAndWhitespaceTolerant() {
        String input = String.join("\n",
            "   ADD    C101   ",
            "Pay 5",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of("addCar:C101", "markPaid:5"), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testBlankLinesAreIgnored() {
        createHandlerWithInput("\n   \nadd C1\n\nquit\n").readInput();

        assertEquals(List.of("addCar:C1"), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    // =========================================================================
    // Quit / Exit / End-of-input Tests
    // =========================================================================

    @Test
    public void testQuitStopsProcessing() {
        createHandlerWithInput("add C1\nquit\nadd C2\n").readInput();

        assertEquals(List.of("addCar:C1"), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }

    @Test
    public void testExitStopsProcessing() {
        createHandlerWithInput("add C1\nEXIT\nadd C2\n").readInput();

        assertEquals(List.of("addCar:C1"), spyController.recordedCalls);
    }

    @Test
    public void testEndOfInputWithoutQuitStopsCleanly() {
        assertDoesNotThrow(() -> createHandlerWithInput("add C1").readInput());
        assertEquals(List.of("addCar:C1"), spyController.recordedCalls);
    }

    // =========================================================================
    // Bad-Input Tests
    // =========================================================================

    @Test
    public void testUnknownCommandReportsError() {
        assertSingleError("foo\nquit");
    }

    @Test
    public void testCancelIsNoLongerACommand() {
        assertSingleError("cancel C101\nquit");
    }

    @Test
    public void testAddWithoutCarIdReportsError() {
        assertSingleError("add\nquit");
    }

    @Test
    public void testAddWithExtraArgumentReportsError() {
        assertSingleError("add C1 C2\nquit");
    }

    @Test
    public void testOrderWithoutArgumentsReportsError() {
        assertSingleError("order\nquit");
    }

    @Test
    public void testOrderWithoutItemsReportsError() {
        assertSingleError("order C1\nquit");
    }

    @Test
    public void testPayWithoutIdReportsError() {
        assertSingleError("pay\nquit");
    }

    @Test
    public void testPayWithNonNumericIdReportsError() {
        assertSingleError("pay abc\nquit");
    }

    @Test
    public void testPayWithTooManyArgumentsReportsError() {
        assertSingleError("pay 1 2\nquit");
    }

    @Test
    public void testPayWithOverflowingIdReportsError() {
        assertSingleError("pay 99999999999\nquit");
    }

    @Test
    public void testPrepWithoutIdReportsError() {
        assertSingleError("prep\nquit");
    }

    @Test
    public void testPrepWithNonNumericIdReportsError() {
        assertSingleError("prep abc\nquit");
    }

    @Test
    public void testDeliverWithExtraArgumentReportsError() {
        assertSingleError("deliver now\nquit");
    }

    @Test
    public void testBadInputDoesNotStopTheLoop() {
        String input = String.join("\n",
            "foo",
            "pay abc",
            "add C1",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(2, spyController.errors.size());
        assertEquals(List.of("addCar:C1"), spyController.recordedCalls);
    }
}