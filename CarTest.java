import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
public class CarTest {
    @Test 
    public void testCarCreationException() {
        assertThrows(IllegalArgumentException.class, () -> new Car(null));
        assertThrows(IllegalArgumentException.class, () -> new Car(""));
        assertThrows(IllegalArgumentException.class, () -> new Car("   "));
    }   

    @Test 
    public void testGetCarId() {
        Car car = new Car("1");
        assertEquals("1", car.getCarId());
    }

    @Test 
    public void testToString() {
        Car car = new Car("1");
        assertEquals("Car ID: 1, Ticket ID: null", car.toString());
        car.setTicketId(100);
        assertEquals("Car ID: 1, Ticket ID: 100", car.toString());
    }

    @Test 
    public void testEquals() {
        Car car1 = new Car("1");
        Car car2 = new Car("1");
        Car car3 = new Car("2");
        assertTrue(car1.equals(car2));
        assertFalse(car1.equals(car3));
        assertFalse(car1.equals(null));
    }
    @Test 
    public void testSetTicketId() {
        Car car = new Car("1");
        car.setTicketId(100);
        assertEquals(100, car.getTicketId());
    }

    @Test 
    public void testSetTicketIdWhenAlreadySetThrowsException() {
        Car car = new Car("1");
        car.setTicketId(100);
        assertThrows(IllegalArgumentException.class, () -> car.setTicketId(200));
    }

    @Test 
    public void testGetTicketId() {
        Car car = new Car("1");
        assertNull(car.getTicketId());
        car.setTicketId(100);
        assertEquals(100, car.getTicketId());
    }
}
