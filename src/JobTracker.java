
import java.util.Scanner;          // Reads keyboard input.
import java.sql.Connection;         // Represents the database connection.
import java.sql.DriverManager;      // Creates the database connection.
import java.sql.ResultSet;          // Holds rows returned by SQL queries.
import java.sql.Statement;          // Executes SQL queries.
import java.sql.SQLException;       // Handles database errors.
import java.sql.PreparedStatement; // Executes SQL commands using supplied values.
import java.sql.Date;              // Handles SQL date values.

public class JobTracker {

    // Create a reusable method to connect to MySQL.
    public static Connection getConnection() throws SQLException {

        // Address of the job_tracker database.
        String url = "jdbc:mysql://localhost:3306/job_tracker";

        // MySQL login details.
        String username = "root";
        // Replace with your actual password.
        String password = System.getenv("JOBTRACKER_DB_PASSWORD");
        
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "Database password is not configured."
            );
        } // Replace with your actual password.

        // Open and return the database connection.
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) {

        // Create a Scanner to read the user's menu choice.
        Scanner scanner = new Scanner(System.in);

        // Store the menu option entered by the user.
        int choice;

        // Repeat the menu until the user chooses Exit.
        do {

            // Display the main menu.
            System.out.println("\n===== JOB APPLICATION TRACKER =====");
            System.out.println("1. Add Application");
            System.out.println("2. View All Applications");
            System.out.println("3. Search Application");
            System.out.println("4. Update Application Status");
            System.out.println("5. Delete Application");
            System.out.println("6. Dashboard Statistics");
            System.out.println("7. Exit");


            // Ask the user to enter a menu option.
            System.out.print("Enter your choice: ");

            // Check whether the entered value is an integer.
            if (scanner.hasNextInt()) {

                // Read the menu number.
                choice = scanner.nextInt();

            } else {

                // Display an error when the input is not a number.
                System.out.println("Invalid input. Please enter a number from 1 to 7.");

                // Remove the invalid input so the Scanner can read again.
                scanner.nextLine();

                // Use 0 so the menu repeats instead of exiting.
                choice = 0;
            }


            // Run the feature selected by the user.
            switch (choice) {


                case 1:
                    // Display a heading for adding an application.
                    System.out.println("\n=== Add Job Application ===");

                    // Clear the newline left by scanner.nextInt().
                    scanner.nextLine();

                    // Read the company name.
                    System.out.print("Enter company name: ");
                    String companyName = scanner.nextLine();

                    // Read the job role.
                    System.out.print("Enter job role: ");
                    String jobRole = scanner.nextLine();


                    // Read and validate the application status.
                    System.out.print("Enter status (Applied/Assessment/Interview/Selected/Rejected): ");
                    String status = scanner.nextLine().trim();

                    // Repeat until the user enters a valid status.
                    while (!status.equalsIgnoreCase("Applied")
                            && !status.equalsIgnoreCase("Assessment")
                            && !status.equalsIgnoreCase("Interview")
                            && !status.equalsIgnoreCase("Selected")
                            && !status.equalsIgnoreCase("Rejected")) {

                        // Explain which statuses are allowed.
                        System.out.println("Invalid status. Please enter Applied, Assessment, Interview, Selected, or Rejected.");

                        // Ask the user to enter the status again.
                        System.out.print("Enter status: ");
                        status = scanner.nextLine().trim();
                    }


                    // Read the application date.
                    System.out.print("Enter application date (YYYY-MM-DD): ");
                    String applicationDate = scanner.nextLine();

                    // Read the job description.
                    System.out.print("Enter job description: ");
                    String jobDescription = scanner.nextLine();

                    // Prepare the SQL command with five placeholders.
                    String insertSql = "INSERT INTO applications " +
                        "(company_name, job_role, status, application_date, job_description) " +
                        "VALUES (?, ?, ?, ?, ?)";

                    // Connect to MySQL and automatically close resources.
                    try (
                        Connection connection = getConnection();

                        // Prepare the INSERT statement.
                        PreparedStatement statement =
                            connection.prepareStatement(insertSql)
                    ) {

                        // Supply the company name.
                        statement.setString(1, companyName);

                        // Supply the job role.
                        statement.setString(2, jobRole);

                        // Supply the application status.
                        statement.setString(3, status);

                        // Convert the entered date into a SQL date.
                        statement.setDate(4, Date.valueOf(applicationDate));

                        // Supply the job description.
                        statement.setString(5, jobDescription);

                        // Execute the INSERT command.
                        int rowsInserted = statement.executeUpdate();

                        // Confirm that the application was saved.
                        if (rowsInserted > 0) {
                            System.out.println("Application added successfully!");
                        }

                    } catch (SQLException e) {

                        // Display database errors.
                        System.out.println("Could not add application.");
                        System.out.println(e.getMessage());

                    } catch (IllegalArgumentException e) {

                        // Display an error for an invalid date format.
                        System.out.println(
                            "Invalid date. Use YYYY-MM-DD, for example 2026-10-27."
                        );
                    }

                break;


                case 2:
                    // Display a heading for the saved applications.
                    System.out.println("\n=== All Job Applications ===");

                    // Connect to MySQL and automatically close resources.
                    try (
                        Connection connection = getConnection();

                        // Create an object to execute the SQL query.
                        Statement statement = connection.createStatement();

                        // Retrieve all rows from the applications table.
                        ResultSet resultSet =
                            statement.executeQuery("SELECT * FROM applications")
                    ) {

                        // Move through the returned rows one by one.
                        while (resultSet.next()) {

                            // Display the application ID.
                            System.out.println("ID: " +
                                resultSet.getInt("id"));

                            // Display the company name.
                            System.out.println("Company: " +
                                resultSet.getString("company_name"));

                            // Display the job role.
                            System.out.println("Job Role: " +
                                resultSet.getString("job_role"));

                            // Display the application status.
                            System.out.println("Status: " +
                                resultSet.getString("status"));

                            // Display the application date.
                            System.out.println("Date: " +
                                resultSet.getDate("application_date"));

                            // Display the job description.
                            System.out.println("Description: " +
                                resultSet.getString("job_description"));

                            // Separate each application in the output.
                            System.out.println("----------------------------");
                        }

                    } catch (SQLException e) {

                        // Display an error if the database operation fails.
                        System.out.println("Could not retrieve applications.");
                        System.out.println(e.getMessage());
                    }

                break;


                case 3:
                    // Display a heading for the search feature.
                    System.out.println("\n=== Search Job Application ===");

                    // Clear the leftover newline from the previous menu input.
                    scanner.nextLine();

                    // Ask which company the user wants to search for.
                    System.out.print("Enter company name to search: ");
                    String searchCompany = scanner.nextLine();

                    // Prepare an SQL query to find matching company names.
                    String searchSql =
                        "SELECT * FROM applications WHERE company_name LIKE ?";

                    // Connect to MySQL and automatically close the resources.
                    try (
                        Connection connection = getConnection();

                        // Prepare the SQL query safely.
                        PreparedStatement statement =
                            connection.prepareStatement(searchSql)
                    ) {
                        // Add wildcards so partial company names can match.
                        statement.setString(1, "%" + searchCompany + "%");

                        // Execute the search and store the matching rows.
                        try (ResultSet resultSet = statement.executeQuery()) {

                            // Track whether at least one application was found.
                            boolean found = false;

                            // Display each matching application.
                            while (resultSet.next()) {
                                found = true;

                                System.out.println("\nID: " +
                                    resultSet.getInt("id"));
                                System.out.println("Company: " +
                                    resultSet.getString("company_name"));
                                System.out.println("Job Role: " +
                                    resultSet.getString("job_role"));
                                System.out.println("Status: " +
                                    resultSet.getString("status"));
                                System.out.println("Date: " +
                                    resultSet.getDate("application_date"));
                                System.out.println("Description: " +
                                    resultSet.getString("job_description"));
                                System.out.println("----------------------------");
                            }

                            // Inform the user if no applications matched.
                            if (!found) {
                                System.out.println("No matching applications found.");
                            }
                        }

                    } catch (SQLException e) {
                        // Display any database error.
                        System.out.println("Could not search applications.");
                        System.out.println(e.getMessage());
                    }

                break;


                case 4:
                    // Display a heading for the update feature.
                    System.out.println("\n=== Update Application Status ===");

                    // Clear the leftover newline from the menu input.
                    scanner.nextLine();

                    // Ask for the ID of the application to update.
                    System.out.print("Enter application ID: ");
                    int updateId = scanner.nextInt();

                    // Clear the newline left by nextInt().
                    scanner.nextLine();

                    // Ask for the new application status.
                    System.out.print("Enter new status (Applied/Assessment/Interview/Selected/Rejected): ");
                    String newStatus = scanner.nextLine();

                    // Prepare the SQL command to update the selected application.
                    String updateSql =
                        "UPDATE applications SET status = ? WHERE id = ?";

                    // Connect to MySQL and prepare the update command.
                    try (
                        Connection connection = getConnection();
                        PreparedStatement statement =
                            connection.prepareStatement(updateSql)
                    ) {
                        // Supply the new status.
                        statement.setString(1, newStatus);

                        // Supply the ID of the application to update.
                        statement.setInt(2, updateId);

                        // Execute the UPDATE command.
                        int rowsUpdated = statement.executeUpdate();

                        // Show whether the application was updated.
                        if (rowsUpdated > 0) {
                            System.out.println("Application status updated successfully!");
                        } else {
                            System.out.println("No application found with ID: " + updateId);
                        }

                    } catch (SQLException e) {
                        // Display any database error.
                        System.out.println("Could not update application status.");
                        System.out.println(e.getMessage());
                    }

                break;



                case 5:
                    // Display a heading for the delete feature.
                    System.out.println("\n=== Delete Job Application ===");

                    // Clear the leftover newline from the menu input.
                    scanner.nextLine();

                    // Ask for the ID of the application to delete.
                    System.out.print("Enter application ID to delete: ");
                    int deleteId = scanner.nextInt();

                    // Clear the newline left by nextInt().
                    scanner.nextLine();

                    // Prepare the SQL command to delete the selected application.
                    String deleteSql =
                        "DELETE FROM applications WHERE id = ?";

                    // Connect to MySQL and prepare the delete command.
                    try (
                        Connection connection = getConnection();
                        PreparedStatement statement =
                            connection.prepareStatement(deleteSql)
                    ) {
                        // Supply the ID of the application to delete.
                        statement.setInt(1, deleteId);

                        // Execute the DELETE command.
                        int rowsDeleted = statement.executeUpdate();

                        // Confirm whether the application was deleted.
                        if (rowsDeleted > 0) {
                            System.out.println("Application deleted successfully!");
                        } else {
                            System.out.println("No application found with ID: " + deleteId);
                        }

                    } catch (SQLException e) {
                        // Display any database error.
                        System.out.println("Could not delete application.");
                        System.out.println(e.getMessage());
                    }

                break;


                case 6:
                    // Display a heading for the dashboard.
                    System.out.println("\n=== Dashboard Statistics ===");

                    // SQL query to count applications in each status.
                    String statsSql =
                        "SELECT status, COUNT(*) AS total " +
                        "FROM applications GROUP BY status";

                    // SQL query to count all applications.
                    String totalSql =
                        "SELECT COUNT(*) AS total FROM applications";

                    // Connect to MySQL and automatically close resources.
                    try (
                        Connection connection = getConnection();
                        Statement statement = connection.createStatement()
                    ) {
                        // Execute the query to count all applications.
                        try (ResultSet totalResult =
                                statement.executeQuery(totalSql)) {

                            // Move to the result containing the total count.
                            if (totalResult.next()) {
                                System.out.println(
                                    "Total Applications: " +
                                    totalResult.getInt("total")
                                );
                            }
                        }

                        // Store and display the count for each status.
                        try (ResultSet statsResult =
                                statement.executeQuery(statsSql)) {

                            // Process each status returned by MySQL.
                            while (statsResult.next()) {
                                // Read the status name.
                                String applicationStatus =
                                    statsResult.getString("status");

                                // Read the number of applications in that status.
                                int count =
                                    statsResult.getInt("total");

                                // Display the status and its count.
                                System.out.println(
                                    applicationStatus + ": " + count
                                );
                            }
                        }

                    } catch (SQLException e) {
                        // Display any database error.
                        System.out.println(
                            "Could not retrieve dashboard statistics."
                        );
                        System.out.println(e.getMessage());
                    }

                break;


                case 7:
                    // End the program when the user chooses Exit.
                    System.out.println("Goodbye!");
                    break;

                default:
                    // Handle an invalid menu number.
                    System.out.println("Invalid choice. Enter a number from 1 to 7.");
            }

        } while (choice != 7);

        // Close the keyboard input reader.
        scanner.close();
    }
}
