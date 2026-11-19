# cs2114-project3: DriveThruDash (Team 42), Prototype 2

A command-line drive-thru simulator built as Model / View / Controller. Cars join a queue, the next car places an order, the order is paid and prepared, and the front car is delivered. The change feature for Deliverable 4 is **Order Cancellation / Drive-Off** (`cancel <carId>`).

## How to Compile and Run

Requires JDK 17+. All source files are in the project root (default package).

```bash
mkdir -p out
javac -d out Car.java FoodTicket.java DriveThruQueue.java DriveThruSystem.java \
      DriveThruView.java DriveThruController.java InputHandler.java
java -cp out DriveThruController
```

Commands: `add <carId>`, `order <carId> <item> ...` (menu: Burger, Fries, Soda), `pay <ticketId>`, `prep <ticketId>`, `deliver`, `quit`. The `cancel <carId>` command is added in the final build (see Next Steps).

## How to Run the Tests

JUnit is bundled in `lib/`.

```bash
javac -d out -cp lib/junit-platform-console-standalone-6.1.3.jar *.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar -cp out --scan-classpath
```

You can also run them from the VS Code Testing sidebar (`.vscode/settings.json` already points at `lib/`). Tests target the model (`CarTest`, `FoodTicketTest`, `DriveThruQueueTest`, `DriveThruSystemTest`), the controller (`DriveThruControllerTest`), and the input and view layers (`InputHandlerTest`, `DriveThruViewTest`). `InputHandler` accepts an `InputStream`, so commands are tested without blocking on `System.in`.

---

## Reflection

### How did our design absorb the change?

**Classes the cancel feature touched so far:**

- `DriveThruQueue<T>`: added `remove(T data)`, which unlinks a node from the front, middle, or rear while keeping `front`, `rear`, and `size` correct. It throws `IllegalArgumentException` for `null` and `NoSuchElementException` if the element is missing.
- `DriveThruSystem`: `deliverOrder()` now uses `foodTicketQueue.remove(...)` instead of `dequeue()`. The `cancelOrder(String carId)` method from the plan is **not written yet**.
- `InputHandler`: we added a `cancel <carId>` command, then removed it, because `DriveThruController.cancelOrder` doesn't exist yet and the project would not compile with it. Related cleanup in the same file: the "valid commands" message is a single constant, and `add` now requires exactly one argument.
- `DriveThruController`: not changed for the feature. The only edit was a cosmetic rename of the constructor parameter.

**What matched the D3 prediction:** `DriveThruQueue` needed `remove` exactly as predicted, and `Car`, `FoodTicket`, and `DriveThruView` did not change at all. Everything cancel needs to know about a car and its ticket (`getTicketId()`, `isPrepared()`) was already there.

**What surprised us:**

- The model piece was the easy part and the wiring was the hard part. `cancelOrder` has to exist in the system and the controller before the input handler can call it, so the layers could not be merged independently.
- `remove` turned out to be useful for more than cancel: `deliverOrder` now removes the front ticket through it.
- Adding `Car.equals` on `carID` earlier made `remove(new Car(id))` and duplicate detection work without extra search code.

### Did the change cross the model/view line?

No. `DriveThruView` is unchanged and learned nothing new about the model, because it only renders the `List<Car>` and `List<FoodTicket>` snapshots the controller hands it. No model class references `InputHandler`, `DriveThruView`, or `DriveThruController`. The only layer ripple we hit was build-order: `InputHandler` depended on a controller method that wasn't there yet.

### What changed from our D3 plan?

- **Feature scope:** `cancel` is not yet reachable from the CLI. We pulled the command from `InputHandler` to keep the build compiling, and the model/controller wiring moves to the final build. We are not switching features.
- **`DriveThruQueue.remove`:** it returns `void` and throws `NoSuchElementException` when the element isn't found, rather than returning a boolean, so callers can't silently ignore a miss.
- **MVP fixes from D3 that are done:**
  - `Car.setTicketId` checks the field and the constructor rejects blank IDs. We kept `IllegalArgumentException` instead of the planned `IllegalStateException`, and `CarTest` expects that.
  - `addCar` rejects duplicate car IDs through `Car.equals`.
  - `deliverOrder` throws `IllegalStateException` when the front order isn't paid and prepared.
  - `createOrder` rejects items not on the menu.
- **Tests:** `DriveThruSystemTest` and `FoodTicketTest` were added since D3. We fixed `testDeliverOrderNotReady` to expect the error message (it still expected the old `NullPointerException`).

### What doesn't work yet?

- **`cancel` is not implemented end to end.** `DriveThruSystem.cancelOrder` and `DriveThruController.cancelOrder` are missing, and `InputHandlerTest.testCancelIsNoLongerACommand` currently asserts that `cancel` is an unknown command. That test must be replaced when the command returns.
- **`createOrder` compares car IDs with `!=`** (`carId != carQueue.peek().getCarId()`), which compares String references. IDs typed on separate lines are different objects, so ordering from the CLI can be rejected as "not at the front of the queue." It should use `.equals`.
- **`createOrder` lets only the front car order.** The rule should be "the first car with `ticketId == null`." The 7-item cap is also missing.
- **Delivering with empty queues** throws `NoSuchElementException`, which `DriveThruController.deliver()` doesn't catch (only `IllegalStateException`), so the user gets a generic "Unknown command." `testDeliverEmptyQueue` currently locks in that behavior.
- **Missing tests:** `DriveThruQueueTest` has no tests for `remove`, and `DriveThruSystemTest` has no `deliverOrder` or cancel tests. `DriveThruControllerTest` and `FoodTicketTest` use JUnit 4 imports while the other test classes use JUnit 5.
- **Multi-word menu items** are unsupported because commands are split on whitespace.

### What design change would make the next feature cheaper?

- **Queue helpers that take a predicate** (`find(Predicate<T>)` and `removeIf(Predicate<T>)`), so features like cancel, lookup by ID, or "move to back of the line" become one-line calls in `DriveThruSystem` instead of new linked-list code.
- **One shared "ordering car" lookup** (a single method that returns the first car without a ticket), used by `createOrder` and by cancel's validation, so the rule lives in one place.
- **A command table in `InputHandler`** (`Map<String, Command>`), so adding a command is one entry instead of editing a `switch`, a usage message, and its tests.

---

## Next Steps

### What is left for the final build (in order)

1. **Finish `cancel` end to end:**
   - `DriveThruSystem.cancelOrder(String carId)`: throw `NoSuchElementException` for an unknown car and `IllegalStateException` if the linked ticket is already prepared. Otherwise remove the ticket (if any) and the car.
   - `DriveThruController.cancelOrder(String carId)`: confirm, refresh the view, and show errors for those two exceptions.
   - Restore the `cancel <carId>` command in `InputHandler`, and replace `testCancelIsNoLongerACommand` with real tests.
2. **Fix `createOrder`:** compare IDs with `.equals`, allow only the first car without a ticket to order, and enforce the 7-item cap.
3. **Fix empty delivery:** `deliver()` on empty queues shows the same friendly error as an unready order. Update `testDeliverEmptyQueue` to match.
4. **Fill test gaps:** `remove` (front, middle, rear, only element, missing element, null), `cancelOrder` normal and bad-input cases, controller cancel tests, and an order check after interleaved enqueue, dequeue, and remove calls.
5. **End-to-end check and rehearsal:** run the full CLI scenario, confirm all tests pass, and update this README to match the final code.

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
| 1:55–2:15 | Bad input survives | `cancel C999`, `order C103 Pizza`, `pay abc` |
| 2:15–2:45 | Finish the first order | `pay 1`, `prep 1`, `deliver` |
| 2:45–3:00 | Show passing tests and quit | `quit` |

### Who does what

| Remaining item | Owner |
|---|---|
| `DriveThruSystem.cancelOrder`; `createOrder` fixes; empty-delivery fix; `remove` and `DriveThruSystemTest` tests | Aaron |
| `DriveThruController.cancelOrder`; controller tests for cancel and delivery | Golden |
| Restore `cancel` in `InputHandler` and its tests; end-to-end run; demo script and timing | Julia |
| Confirm the view after cancel (removed cars and tickets disappear) and add a view test | Cassidy |

## Attribution

| Class | Name | Contribution Details |
|---|---|---|
| DriveThruView | Cassidy | Drafted the functions on my own. Then I used GenAI to find any inconsistencies with the draft and asked for design suggestions. Then I fixed a bug and had GenAI check over it again. |
| DriveThruViewTest | Cassidy | I had GenAI draft edge case tests, and then I used only the necessary ones once I reviewed the GenAI output. |
| DriveThruSystem | Aaron | Drafted functions along side vscode copilot to help catch errors. Also wrote methods then asked GenAi what the method does to see if the method does exactly what I wanted it to do. |
| DriveThruQueue<T> | Aaron | I used GenAI to see the basic outline of a First-In-First-Out queue then wrote the methods myself based on the outline only using what is needed for the progarm. |
| FoodTicket + Car | Aaron | Wrote the object classes based on the the outline the group made. Used vscode copiolt to help with the methods. ALos to make sure all the method names are the same accros the classes. |
| DriveThruController | Golden | Implemented the DriveThruController class based on the team's outline. Used AI to help implement the controller methods and ensure that the method calls and names were consistent across the different classes. |
| ImputHandler | Julia | Implemented the InputHandler class (readInput, command tokenization, input-layer validation, and InputStream testing constructor) with GenAI assistance to adhere to strict MVC boundaries, and authored the Reflection and Next Steps sections of the README.md |
