import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

public class DriveThruSystemTest {
    /**
     * Tests the addition of a car to the drive-thru system.
     */
    private DriveThruSystem driveThruSystem;
    private Car car;
    private FoodTicket foodTicket;
    public void setUp() {
        driveThruSystem = new DriveThruSystem();
        car = new Car("CAR123");
        foodTicket = new FoodTicket(1, List.of("Burger", "Fries"));
    }
    /**
     * Tests the creation of a DriveThruSystem object.
     */
    @Test 
    public void testAddCar() {
        driveThruSystem = new DriveThruSystem();
        car = new Car("CAR123");
        driveThruSystem.addCar(car);
        assertEquals(1, driveThruSystem.getCarSnapshot().size());
        assertEquals("CAR123", driveThruSystem.getCarSnapshot().get(0).getCarId());
    }

    /**
     * Tests that adding a car with a null ID throws an exception.
     */
    @Test
    public void addCarWithNullThrowsException() {
        driveThruSystem = new DriveThruSystem();
        assertThrows(IllegalArgumentException.class, () -> driveThruSystem.addCar(null));
    }
    /**
     * Tests that adding a car with a duplicate ID throws an exception.
     */
    @Test
    public void addCarWithDuplicateIdThrowsException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        Car duplicateCar = new Car("CAR123");
        assertThrows(IllegalArgumentException.class, () -> driveThruSystem.addCar(duplicateCar));
    }
    /**
     * Tests the creation of a food order for a car in the drive-thru system.
     */
    @Test
    public void testCreateOrder() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        FoodTicket ticket = driveThruSystem.createOrder("CAR123", List.of("Burger", "Fries"));
        assertEquals(1, ticket.getTicketId());
        assertEquals(List.of("Burger", "Fries"), ticket.getItems());
    }
    /**
     * Tests that creating an order for a car not at the front of the queue throws an exception.
     */
    @Test 
    public void createOrderWithInvalidCarIdThrowsException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        assertThrows(NoSuchElementException.class, () -> driveThruSystem.createOrder("INVALID_ID", List.of("Burger")));
    }
    /**
     * Tests that creating an order for a car not at the front of the queue throws an exception.
     */
    @Test 
    public void createOrderWithEmptyItemsThrowsException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        assertThrows(IllegalArgumentException.class, () -> driveThruSystem.createOrder("CAR123", List.of()));
    }
    /**
     * Tests that creating an order for a car not at the front of the queue throws an exception.
     */
    @Test 
    public void createOrderWithItemNotOnMenuThrowsException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        assertThrows(IllegalArgumentException.class, () -> driveThruSystem.createOrder("CAR123", List.of("Pizza")));
    }
    /**
     * Tests that creating an order for a car not at the front of the queue throws an exception.
     */
    @Test 
    public void testCreateOrderWithCarNotAtFrontOfQueueThrowsException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        Car car2 = new Car("CAR456");
        driveThruSystem.addCar(car);
        driveThruSystem.addCar(car2);
        assertThrows(IllegalStateException.class, () -> driveThruSystem.createOrder("CAR456", List.of("Burger")));
    }
    /**
     * Tests the getTicketSnapshot method of the DriveThruSystem class.
     */
    @Test 
    public void testGetCarSnapshot() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        List<Car> carSnapshot = driveThruSystem.getCarSnapshot();
        assertEquals(1, carSnapshot.size());
        assertEquals("CAR123", carSnapshot.get(0).getCarId());
    }
    /**
     * Tests the getTicketSnapshot method of the DriveThruSystem class.
     */
    @Test
    public void testGetTicketSnapshot() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        driveThruSystem.createOrder("CAR123", List.of("Burger", "Fries"));
        List<FoodTicket> ticketSnapshot = driveThruSystem.getTicketSnapshot();
        assertEquals(1, ticketSnapshot.size());
        assertEquals(1, ticketSnapshot.get(0).getTicketId());
    }
    /**
     * Tests marking a food ticket as paid and prepared.
     */
    @Test
    public void markAsPaidAndPrepared() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        driveThruSystem.createOrder("CAR123", List.of("Burger", "Fries"));
        FoodTicket ticket = driveThruSystem.getTicketSnapshot().get(0);
        ticket.markAsPaid();
        ticket.markAsPrepared();
        assertTrue(ticket.isPaid());
        assertTrue(ticket.isPrepared());
    }
    /**
     * Tests that marking a food ticket as paid or prepared when it is already in that state throws an exception.
     */
    @Test 
    public void markAsPaidAndPreparedException() {
        driveThruSystem = new DriveThruSystem();
        Car car = new Car("CAR123");
        driveThruSystem.addCar(car);
        driveThruSystem.createOrder("CAR123", List.of("Burger", "Fries"));
        FoodTicket ticket = driveThruSystem.getTicketSnapshot().get(0);
        ticket.markAsPaid();
        assertThrows(IllegalStateException.class, () -> ticket.markAsPaid());
        ticket.markAsPrepared();
        assertThrows(IllegalStateException.class, () -> ticket.markAsPrepared());
    }

}
