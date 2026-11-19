import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;


public class DriveThruQueueTest {
    @Test 
    public void testEnqueueAndDequeue() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        Car car1 = new Car("1");
        Car car2 = new Car("2");
        carQueue.enqueue(car1);
        carQueue.enqueue(car2);
        assertEquals(car1, carQueue.dequeue());
        assertEquals(car2, carQueue.dequeue());
    }

    @Test
    public void testDequeueEmptyQueueThrowsException() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        assertThrows(NoSuchElementException.class, () -> carQueue.dequeue());
    }

    @Test 
    public void testEmptyQueue() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        assertTrue(carQueue.isEmpty());
        Car car1 = new Car("1");
        carQueue.enqueue(car1);
        assertFalse(carQueue.isEmpty());
    }

    @Test
    public void testPeek() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        Car car1 = new Car("1");
        carQueue.enqueue(car1);
        assertEquals(car1, carQueue.peek());
        assertEquals(car1, carQueue.dequeue());
        assertThrows(NoSuchElementException.class, () -> carQueue.peek());
    }

    @Test 
    public void testGetSize() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        assertEquals(0, carQueue.getSize());
        Car car1 = new Car("1");
        carQueue.enqueue(car1);
        assertEquals(1, carQueue.getSize());
        carQueue.dequeue();
        assertEquals(0, carQueue.getSize());
    }

    @Test
    public void testContains() {
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        Car car1 = new Car("1");
        Car car2 = new Car("2");
        carQueue.enqueue(car1);
        carQueue.enqueue(car2);
        assertTrue(carQueue.contains(car1));
        assertTrue(carQueue.contains(car2));
        assertFalse(carQueue.contains(new Car("3")));
    }

    @Test
    public void testToList(){
        DriveThruQueue<Car> carQueue = new DriveThruQueue<>();
        Car car1 = new Car("1");
        Car car2 = new Car("2");
        carQueue.enqueue(car1);
        carQueue.enqueue(car2);
        assertEquals(List.of(car1, car2), carQueue.toList());
    }
    @Test
    public void testRemoveFront() {
        DriveThruQueue<Car> q = new DriveThruQueue<>();
        Car a = new Car("1"), b = new Car("2"), c = new Car("3");
        q.enqueue(a); q.enqueue(b); q.enqueue(c);
        q.remove(a);
        assertEquals(List.of(b, c), q.toList());
        assertEquals(2, q.getSize());
    }

    @Test
    public void testRemoveMiddle() {
        DriveThruQueue<Car> q = new DriveThruQueue<>();
        Car a = new Car("1"), b = new Car("2"), c = new Car("3");
        q.enqueue(a); q.enqueue(b); q.enqueue(c);
        q.remove(b);
        assertEquals(List.of(a, c), q.toList());
        assertEquals(2, q.getSize());
    }

    @Test
    public void testRemoveRearThenEnqueueKeepsOrder() {
        DriveThruQueue<Car> q = new DriveThruQueue<>();
        Car a = new Car("1"), b = new Car("2"), c = new Car("3"), d = new Car("4");
        q.enqueue(a); q.enqueue(b); q.enqueue(c);
        q.remove(c);
        q.enqueue(d);
        assertEquals(List.of(a, b, d), q.toList());
    }

    @Test
    public void testRemoveMissingOrNullThrows() {
        DriveThruQueue<Car> q = new DriveThruQueue<>();
        assertThrows(NoSuchElementException.class, () -> q.remove(new Car("1")));
        q.enqueue(new Car("2"));
        assertThrows(NoSuchElementException.class, () -> q.remove(new Car("9")));
        assertThrows(IllegalArgumentException.class, () -> q.remove(null));
    }

    @Test
    public void testOrderAfterInterleavedEnqueueDequeueRemove() {
        DriveThruQueue<Car> q = new DriveThruQueue<>();
        Car a = new Car("1"), b = new Car("2"), c = new Car("3"),
            d = new Car("4"), e = new Car("5");
        q.enqueue(a); q.enqueue(b); q.enqueue(c);
        assertEquals(a, q.dequeue());
        q.enqueue(d);
        q.remove(c);
        q.enqueue(e);
        assertEquals(List.of(b, d, e), q.toList());
        assertEquals(3, q.getSize());
    }
}
