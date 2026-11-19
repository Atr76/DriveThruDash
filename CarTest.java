/**
 * Car class represents a car with a unique identifier and an optional ticket ID.
 */
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
public class CarTest {
    /**
     * Tests the creation of a Car object with valid parameters.
     */
    @Test 
    public void testCarCreationException() {
        assertThrows(IllegalArgumentException.class, () -> new Car(null));
        assertThrows(IllegalArgumentException.class, () -> new Car(""));
        assertThrows(IllegalArgumentException.class, () -> new Car("   "));
    }   
    /**
     * Tests the getCarId method of the Car class.
     */
    @Test 
    public void testGetCarId() {
        Car car = new Car("1");
        assertEquals("1", car.getCarId());
    }
    /**
     * Tests the toString method of the Car class.
     */
    @Test 
    public void testToString() {
        Car car = new Car("1");
        assertEquals("Car ID: 1, Ticket ID: null", car.toString());
        car.setTicketId(100);
        assertEquals("Car ID: 1, Ticket ID: 100", car.toString());
    }
    /**
     * Tests the equals method of the Car class.
     */
    @Test 
    public void testEquals() {
        Car car1 = new Car("1");
        Car car2 = new Car("1");
        Car car3 = new Car("2");
        assertTrue(car1.equals(car2));
        assertFalse(car1.equals(car3));
        assertFalse(car1.equals(null));
    }
    /**
     * Tests the hashCode method of the Car class.
     */
    @Test 
    public void testSetTicketId() {
        Car car = new Car("1");
        car.setTicketId(100);
        assertEquals(100, car.getTicketId());
    }
    /**
     * Tests that setting a ticket ID when one is already set throws an exception.
     */
    @Test 
    public void testSetTicketIdWhenAlreadySetThrowsException() {
        Car car = new Car("1");
        car.setTicketId(100);
        assertThrows(IllegalArgumentException.class, () -> car.setTicketId(200));
    }
    /**
     * Tests the getTicketId method of the Car class.
     */
    @Test 
    public void testGetTicketId() {
        Car car = new Car("1");
        assertNull(car.getTicketId());
        car.setTicketId(100);
        assertEquals(100, car.getTicketId());
    }
}
