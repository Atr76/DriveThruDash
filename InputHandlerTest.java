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
        public void cancelOrder(String carId) {
            recordedCalls.add("cancelOrder:" + carId);
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
    public void testCancelCommandAndAliases() {
        String input = String.join("\n",
            "cancel C101",
            "cancelorder C102",
            "removecar C103",
            "driveoff C104",
            "quit"
        );

        createHandlerWithInput(input).readInput();

        assertEquals(List.of(
            "cancelOrder:C101",
            "cancelOrder:C102",
            "cancelOrder:C103",
            "cancelOrder:C104"
                ), spyController.recordedCalls);
        assertTrue(spyController.errors.isEmpty());
    }
}