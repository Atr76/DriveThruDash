import java.util.List;
import java.util.Objects;

/**
 * The DriveThruView class renders the state of both queues, window statuses, the waiting count, and displays 
 * an error message for invalid input or sequence violations and regular messages.
 */
public class DriveThruView {
    /**
     * Renders the state of both queues and active window statuses.
     */
    public void render(List<Car> cars, List<FoodTicket> tickets) {
        if (cars == null) {
            throw new IllegalArgumentException("cars is null");
        }
        if (tickets == null) {
            throw new IllegalArgumentException("tickets is null");
        }
        Car window1 = null;
        for (Car car : cars) {
            if (car.getTicketId() == null) {
                window1 = car;
                break;
            }
        }
        Car window2 = null;
        if (!cars.isEmpty()) {
            window2 = cars.get(0);
        }
        if (window1 == null) {
            System.out.println("Window 1: No cars waiting to order");
        }
        else {
            System.out.println("Window 1: car " + window1.getCarId() + " is ordering");
        }
        if (window2 == null) {
            System.out.println("Window 2: No cars waiting");
        }
        else {
            System.out.println("Window 2: car " + window2.getCarId());
            if (window2.getTicketId() == null) {
                System.out.println("No ticket");
            }
            else {
                boolean found = false;
                for (FoodTicket ticket : tickets) {
                    if (Objects.equals(ticket.getTicketId(), window2.getTicketId())) {
                        System.out.println("paid: " + ticket.isPaid() + " prepared: " + ticket.isPrepared());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Ticket not found");
                }
            }
        }
        int waitingCount = 0;
        for (Car car : cars) {
            if (car.getTicketId() == null) {
                waitingCount++;
            }
        }
        System.out.println("Waiting to order: " + waitingCount);
        System.out.println("~~~~~ Car queue (" + cars.size() + ") ~~~~~");
        if (cars.isEmpty()) {
            System.out.println("empty");
        }
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            String ticketInfo;
            if (car.getTicketId() == null) {
                ticketInfo = "No ticket yet";
            }
            else {
                ticketInfo = "ticket " + car.getTicketId();
            }
            System.out.println((i+1) + ". car " + car.getCarId() +" (" +
            ticketInfo + ")");
        }
        System.out.println("~~~~~ Ticket queue (" + tickets.size() + ") ~~~~~");
        if (tickets.isEmpty()) {
            System.out.println("empty");
        }
        for (int i = 0; i <tickets.size(); i++) {
            FoodTicket ticket = tickets.get(i);
            System.out.println((i+1) + ". ticket " + ticket.getTicketId() 
        + " | paid: " + ticket.isPaid() + " prepared: " + ticket.isPrepared());     
       }
    }
    /**
     * Shows the error message.
     */
    public void showError(String message) {
            System.out.println("Error: " + message);
    }
    /**
     * Shows the regular message.
     */
    public void showMessage(String message) {
            System.out.println(message);
    }
}
