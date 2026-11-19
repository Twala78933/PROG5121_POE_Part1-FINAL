# PROG5121 POE — QuickChat FINAL (Parts 1, 2 and 3)

This is **one complete Apache NetBeans Maven project**, not a Word/PDF code submission.
It contains Java source, JUnit 5 tests, a Maven `pom.xml`, and GitHub Actions configuration.

## Open and run in Apache NetBeans

1. Extract the outer ZIP completely.
2. Open **NetBeans → File → Open Project** and select the **PROG5121_POE_FINAL_NETBEANS** folder (the folder containing `pom.xml`).
3. Allow Maven to resolve JUnit the first time (internet may be required).
4. Right-click the project → **Run** (or run the `PROG5121_POE_Part1` main class).
5. To run all JUnit tests, right-click the project → **Test**.

## QuickChat functionality

- **Part 1:** Registration, username/password/SA telephone number validation, login.
- **Part 2:** Send, disregard, or store messages; 250-character message limit; recipient validation; unique 10-digit IDs; hashes; JSON storage; recent sent messages.
- **Part 3:** Five dynamically filled arrays (sent, disregarded, stored, hashes, IDs); a fourth main-menu option containing seven functions: stored recipients/senders, longest stored message, search by ID, search by recipient, delete by hash, report, reload JSON. All saved messages are read from JSON at startup.

The runtime creates `stored_messages.json` in the working folder. Do not manually edit it while the app is running.

## Automated tests

The project includes JUnit 5 tests of registration, messaging, arrays, longest message, searches, deletion, JSON round trips, Unicode/quotes, reporting and menu operation. GitHub Actions is configured to execute `mvn --batch-mode clean test` after pushing this unzipped project to the **root** of your repository. Verify a green check mark before submission. Do not upload the ZIP alone as the only GitHub file: GitHub Actions needs `pom.xml` and `.github/workflows/maven-tests.yml` at repository root.

## Submission checklist

- Confirm project builds and runs in NetBeans.
- Run `Test` to confirm JUnit tests pass in your environment.
- Add/upload the *contents* of the extracted project at the root of your GitHub repository.
- Confirm Actions runs successfully.
- Submit the GitHub repository link according to your lecturer's instructions.

## Assessment notes

The screenshots of the assignment specify five arrays, fourth-menu reports/actions, JSON loading and specific test data. The sample test data are included in JUnit test fixtures and are **not hard-coded into the live arrays**. In the supplied rubric, test message 4 shows an atypical local number and an example that uses it as a message ID; the tests therefore set that ID explicitly while live messages still require international `+27` format.
