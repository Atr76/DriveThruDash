# cs2114-project3: DriveThruDash (Team 42), Prototype 2

A command-line drive-thru simulator built as Model / View / Controller. Cars join a queue, the front car places an order, the order is paid and prepared, and the front car is delivered. The Deliverable 4 change feature is **Order Cancellation / Drive-Off** (`cancel <carId>`).

## How to Compile and Run
Run in vs code by clicking the run arrow in DriveThruSystem and then inputting the users response in the terminal.

Or:
Requires JDK 17+. All source files are in the project root (default package).

```bash
mkdir -p out
javac -d out Car.java FoodTicket.java DriveThruQueue.java DriveThruSystem.java \
      DriveThruView.java DriveThruController.java InputHandler.java
java -cp out DriveThruController
```

Commands: `add <carId>`, `order <carId> <item> ...` (menu: Burger, Fries, Soda), `pay <ticketId>`, `prep <ticketId>`, `deliver`, `cancel <carId>`, `quit`.

## How to Run the Tests

JUnit is bundled in `lib/`.

```bash
javac -d out -cp lib/junit-platform-console-standalone-6.1.3.jar *.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar -cp out --scan-classpath
```

Run them from the VS Code Testing sidebar (`.vscode/settings.json` already points at `lib/`). Tests target the model (`CarTest`, `FoodTicketTest`, `DriveThruQueueTest`, `DriveThruSystemTest`), the controller (`DriveThruControllerTest`), and the input and view layers (`InputHandlerTest`, `DriveThruViewTest`). `InputHandler` accepts an `InputStream`, so commands are tested without blocking on `System.in`.

---

## Reflection

### How did our design absorb the change?

**Classes the cancel feature actually touched:**

- `DriveThruQueue<T>`: added `remove(T data)`, which unlinks a node from the front, middle, or rear while keeping `front`, `rear`, and `size` correct. It throws `IllegalArgumentException` for `null` and `NoSuchElementException` if the element is missing.
- `DriveThruSystem`: added `cancelOrder(String carId)`. It finds the car, removes its ticket from `foodTicketQueue` (unless the ticket is already prepared), and removes the car from `carQueue`. `deliverOrder()` now also uses `remove` for the ticket.
- `DriveThruController`: added `cancelOrder(String carId)`, which shows a confirmation and refreshes the view on success, and shows an error for an unknown car or an already-prepared order.
- `InputHandler`: added the `cancel <carId>` command (aliases `cancelorder`, `removecar`, `driveoff`) with argument validation.

**What matched the D3 prediction:** every class we predicted would change did, in the role we predicted, and `Car`, `FoodTicket`, and `DriveThruView` did not change at all. `Car.getTicketId()` and `FoodTicket.isPrepared()` already gave `cancelOrder` everything it needed to find the linked ticket and decide whether cancelling is allowed.

**What surprised us:**

- The model piece (`remove`) was easy; the wiring was the hard part. `InputHandler` can't call `controller.cancelOrder` until the system and controller have it, so the layers could not be merged independently. We briefly pulled the command out of the input handler to keep the build compiling.
- Building the demo flow exposed an old bug in `createOrder`, which compared car IDs with `!=` (String reference comparison). IDs typed on separate command lines would be rejected, so ordering from the CLI was broken. We changed it to `.equals`.
- `remove` turned out to be reusable: `deliverOrder` uses it too.
- Adding `Car.equals` on `carID` earlier meant `remove(car)` and duplicate detection worked without extra search code.

### Did the change cross the model/view line?

No. `DriveThruView` is unchanged and learned nothing new about the model, because it only renders the `List<Car>` and `List<FoodTicket>` snapshots the controller passes in, so cancelled cars and tickets disappear from the display automatically. No model class references `InputHandler`, `DriveThruView`, or `DriveThruController`.

### What changed from our D3 plan?

- **Feature:** no switch. We implemented cancellation as planned.
- **`DriveThruQueue.remove`:** it returns `void` and throws `NoSuchElementException` if the element is missing, rather than returning a boolean, so a miss can't be silently ignored.
- **`cancelOrder` validation:** an unknown car throws `NoSuchElementException` (via `findCar`), and a prepared ticket throws `IllegalStateException`, as planned. A car that has not ordered yet can also be cancelled, which D3 did not spell out.
- **MVP fixes from D3 that are done:** `Car.setTicketId` checks the field and the constructor rejects blank IDs (we kept `IllegalArgumentException` instead of the planned `IllegalStateException`), `addCar` rejects duplicate car IDs, `deliverOrder` throws `IllegalStateException` for an unready order, `createOrder` rejects items not on the menu, and `createOrder` now compares IDs with `.equals`.
- **Tests:** added `DriveThruSystemTest` and `FoodTicketTest`, five new `DriveThruQueueTest` tests for `remove` (including an interleaved enqueue/dequeue/remove order check), three `DriveThruSystemTest` tests for `cancelOrder` (normal, unknown car, already-prepared order), two controller tests for cancel, and two input-handler tests for the `cancel` command. We also fixed `testDeliverOrderNotReady`, which still expected the old `NullPointerException`.

### What doesn't work yet?

- **`createOrder` only lets the front car order.** The D3 rule was "the first car with `ticketId == null`", so a car behind the front car cannot order until the cars ahead of it are delivered or cancelled.
- **No 7-item order cap** in `createOrder`.
- **Delivering with empty queues** throws `NoSuchElementException`, which `DriveThruController.deliver()` doesn't catch, so the user sees a generic "Unknown command." `testDeliverEmptyQueue` currently locks in that behavior.
- **Multi-word menu items** are unsupported because commands are split on whitespace.
- **Mixed JUnit versions:** `DriveThruControllerTest` and `FoodTicketTest` use JUnit 4 imports while the other test classes use JUnit 5.

### What design change would make the next feature cheaper?

- **Queue helpers that take a predicate** (`find(Predicate<T>)` and `removeIf(Predicate<T>)`), so features like cancel, lookup by ID, or "move to back of the line" become one-line calls in `DriveThruSystem` instead of new linked-list code.
- **One shared "next car to order" lookup**, used by `createOrder` and by any future ordering rule, so that rule lives in one place instead of being repeated.
- **A command table in `InputHandler`** (`Map<String, Command>`), so adding a command is one entry instead of editing a `switch`, a usage message, and its tests.

---

## Next Steps

### What is left for the final build (in order)

1. **Fix `createOrder`:** allow the first car without a ticket to order, and enforce the 7-item cap.
2. **Fix empty delivery:** catch `NoSuchElementException` in `deliver()` so it shows the same friendly error as an unready order, and update `testDeliverEmptyQueue`.
3. **Final check:** run the full test suite and the demo flow end to end, and rehearse the demo against the 3-minute plan below.

### Stretch goals

- **Committing to:** none beyond the list above. The D3 stretch goal (showing the full queues below the Window 1 and Window 2 status) is already done in `DriveThruView.render` and covered by `DriveThruViewTest`.
- **Dropping:** a graphical UI. We stay CLI-only to focus on the change feature and test coverage.

### 3-minute demo plan

| Time | Step | Commands |
|---|---|---|
| 0:00–0:20 | Intro: MVC layout and where `cancel` fits | (slide) |
| 0:20–0:50 | Add three cars; show queues | `add C101`, `add C102`, `add C103` |
| 0:50–1:20 | First car orders; show Window 1 and Window 2 | `order C101 Burger Fries` |
| 1:20–1:55 | **New feature:** a waiting car drives off; queues update | `cancel C102` |
| 1:55–2:15 | Bad input survives | `cancel C999`, `order C103 Burger`, `pay abc` |
| 2:15–2:45 | Finish the first order | `pay 1`, `prep 1`, `deliver` |
| 2:45–3:00 | Show passing tests and quit | `quit` |

### Who does what

| Remaining item | Owner |
|---|---|
| `createOrder` fixes (first car without a ticket, 7-item cap) | Aaron |
| Empty-delivery fix in `DriveThruController` and its test update | Golden |
| End-to-end run, demo script and timing, final README check | Julia |
| Confirm the view after cancel (removed cars and tickets disappear) and add a view test | Cassidy |

## Attribution

| Class | Name | Contribution Details |
|---|---|---|
| DriveThruView | Cassidy | Drafted the new functions on my own. Then I used GenAI to find any inconsistencies with the draft and asked for design suggestions. Then I fixed a bug and had GenAI check over it again. |
| DriveThruViewTest | Cassidy | I had GenAI draft edge case tests, and then I used only the necessary ones once I reviewed the GenAI output. |
| DriveThruSystem | Aaron |Add edge cases to catch bad input. And fix errors within the code.|
| DriveThruSystemTest | Aaron | Tested all methods and edge cases in the DriveThruSystem and fixed any errors in DriveThruSystem that I found inside the code. Used VS Code Copilot to help write the test. |
| DriveThruQueue<T> | Aaron | Added the remove method. |
| DriveThruQueueTest | Aaron | Tested all methods and edge cases in the DriveThruQueue and fixed any errors in DriveThruQueue. Used VS Code Copilot to help write the test.  Edited errors found in classes.|
| FoodTicket + Car | Aaron | Fixed errors within the car method. |
| FoodTicket + Car tests | Aaron | Tested all methods and edge cases in each class. Used VS Code Copilot to help write the test. |
| DriveThruControllerTest | Golden | Used AI to generate an initial draft of the tests, then reviewed and revised them by myself. |
| ImputHandler | Julia | Implemented the InputHandler class (readInput, command tokenization, input-layer validation, and InputStream testing constructor) with GenAI assistance to adhere to strict MVC boundaries, and authored the Reflection and Next Steps sections of the README.md |
