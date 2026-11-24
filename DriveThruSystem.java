import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a drive-thru system that manages a queue of customers.
 */
public class DriveThruSystem {
    private DriveThruQueue<Car> carQueue;
    private DriveThruQueue<FoodTicket> foodTicketQueue;
    private int nextTicketId;
    Set<String> menu = new HashSet<>();
    /**
     * Constructs a DriveThruSystem object with an empty car queue, an empty food ticket queue,
     * and initializes the next ticket ID to 1.
     */
    public DriveThruSystem() {
        carQueue = new DriveThruQueue<>();
        foodTicketQueue = new DriveThruQueue<>();
        nextTicketId =1;
        menu.add("Burger");
        menu.add("Fries");
        menu.add("Soda");
    }
    /**
     * Adds a car to the drive-thru queue.
     * @param car the Car object to be added to the queue.
     * @throws IllegalArgumentException if the car is null or already exists in the queue.
     */
    public void addCar(Car car) {
        if(car == null) {
            throw new IllegalArgumentException("Car cannot be null");
        }
        if (carQueue.contains(car)) {
            throw new IllegalArgumentException("Car with ID " + car.getCarId() + " already exists");
        }
        carQueue.enqueue(car);
    }

    /**
     * Creates a new food order for the specified car.
     * @param carId the ID of the car for which to create an order.
     * @param items the list of items to include in the order.
     * @return the created FoodTicket object.
     * @throws IllegalArgumentException if the items list is null or empty, or if any item is not on the menu.
     * @throws NoSuchElementException if the car with the specified ID is not found in the queue.
     * @throws IllegalStateException if the car with the specified ID is not at the front of the queue.
     */
    public FoodTicket createOrder(String carId, List<String> items) {
        if(items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be null or empty");
        }
        if(!carQueue.contains(new Car(carId))) {
            throw new NoSuchElementException("Car with ID " + carId + " not found");
        }
        if(foodTicketQueue.contains(new FoodTicket(nextTicketId, items))) {
            throw new IllegalArgumentException("FoodTicket with ID " + nextTicketId + " already exists");
        }
        if (!carId.equals(carQueue.peek().getCarId())) {
            throw new IllegalStateException("Car with ID " + carId + " is not at the front of the queue");
        }
        for(int i = 0; i < items.size(); i++) {
            if(!menu.contains(items.get(i))) {
                throw new IllegalArgumentException("Item at index " + i + " is not on the menu");
            }
        }
        FoodTicket foodTicket = new FoodTicket(nextTicketId, items);
        foodTicket.setTicketId(nextTicketId);
        Car car = findCar(carId);
        car.setTicketId(nextTicketId);
        foodTicketQueue.enqueue(foodTicket);
        nextTicketId++;
        return foodTicket;
    }
    /**
     * Returns the current menu of available items.
     * @return a Set containing the menu items.
     */
    public Set<String> getMenu(){
        return menu;
    }
    /**
     * Returns the next ticket ID to be assigned to a new food order.
     * @return the next ticket ID.
     */
    public void markPaid(int ticketId){
        FoodTicket foodTicket = findFoodTicket(ticketId);
        if (foodTicket != null) {
            foodTicket.markAsPaid();
        }
    }
    /**
     * Marks the specified food ticket as prepared.
     * @param ticketId the ID of the food ticket to be marked as prepared.
     * @throws NoSuchElementException if the food ticket with the specified ID is not found in the queue.
     */
    public void markPrepared(int ticketId){
        FoodTicket foodTicket = findFoodTicket(ticketId);
        if (foodTicket != null) {
            foodTicket.markAsPrepared();
        }
    }
    public void cancelOrder(String carId) {
        Car car = findCar(carId); // throws NoSuchElementException if unknown
        Integer ticketId = car.getTicketId();
        if (ticketId != null) {
            FoodTicket ticket = findFoodTicket(ticketId);
            if (ticket.isPrepared()) {
                throw new IllegalStateException("Order is already prepared");
            }
            foodTicketQueue.remove(ticket);
        }
        carQueue.remove(car);
    }
    /**
     * Delivers the order for the car at the front of the queue if the order is paid and prepared.
     * @return the Car object that received the order.
     * @throws IllegalStateException if the order is not ready for delivery.
     */
    public Car deliverOrder(){
        if(foodTicketQueue.peek().isPaid() && foodTicketQueue.peek().isPrepared()){
            foodTicketQueue.remove(foodTicketQueue.peek());
            Car car = carQueue.dequeue();
            return car ;
        }
        throw new IllegalStateException("Order is not ready for delivery");
    }
    /**
     * Returns a snapshot of the current cars in the drive-thru queue.
     * @return a List containing the Car objects in the queue.
     */
    public List<Car> getCarSnapshot(){
        return carQueue.toList();
    }
    /**
     * Returns a snapshot of the current food tickets in the drive-thru queue.
     * @return a List containing the FoodTicket objects in the queue.
     */
    public List<FoodTicket> getTicketSnapshot(){
        return foodTicketQueue.toList();
    }
    /**
     * Finds a car in the queue by its ID.
     * @param carId the ID of the car to find.
     * @return the Car object with the specified ID.
     * @throws NoSuchElementException if the car with the specified ID is not found in the queue.
     */
    private Car findCar(String carId){
        for (Car car : carQueue.toList()) {
            if (car.getCarId().equals(carId)) {
                return car;
            }
        }
        throw new NoSuchElementException("Car with ID " + carId + " not found");
    }
    /**
     * Finds a food ticket in the queue by its ID.
     * @param ticketId the ID of the food ticket to find.
     * @return the FoodTicket object with the specified ID.
     * @throws NoSuchElementException if the food ticket with the specified ID is not found in the queue.
     */
    private FoodTicket findFoodTicket(int ticketId){
        for (FoodTicket foodTicket : foodTicketQueue.toList()) {
            if (foodTicket.getTicketId() == ticketId) {
                return foodTicket;
            }
        }
        throw new NoSuchElementException("FoodTicket with ID " + ticketId + " not found");
    }
    /**
     * Displays the available options for the drive-thru system.
     */
    public void options(){
        System.out.println();
        System.out.println("Available options:");
        System.out.println("1. Add");
        System.out.println("2. Order");
        System.out.println("3. Pay");
        System.out.println("4. Prep");
        System.out.println("5. Deliver");
        System.out.println("6. Cancel order");
    }
    public static void main(String[] args) {
        DriveThruSystem system = new DriveThruSystem();
        DriveThruView view = new DriveThruView();
        DriveThruController controller = new DriveThruController(system, view);
        system.options();
        InputHandler inputHandler = new InputHandler(controller);
        inputHandler.readInput();
    }
}
