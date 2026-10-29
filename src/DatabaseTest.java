
import java.sql.Connection;       // Allows Java to connect to MySQL.
import java.sql.DriverManager;    // Creates the database connection.
import java.sql.ResultSet;        // Holds the rows returned by a query.
import java.sql.Statement;         // Executes SQL statements.
import java.sql.SQLException;      // Handles database errors.

public class DatabaseTest {

    public static void main(String[] args) {

        // Address of our MySQL database.
        String url = "jdbc:mysql://localhost:3306/job_tracker";

        // MySQL login details.
        String username = "root";
        String password = System.getenv("JOBTRACKER_DB_PASSWORD"); // Replace with your actual password.
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "Database password is not configured."
            );
        }

        // Connect to MySQL and automatically close resources afterward.
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password);

            // Create an object to execute SQL queries.
            Statement statement = connection.createStatement();

            // Ask MySQL to return every row from the applications table.
            ResultSet resultSet =
                statement.executeQuery("SELECT * FROM applications")
        ) {
            // Display a heading in the terminal.
            System.out.println("=== Saved Job Applications ===");

            // Move through the returned rows, one at a time.
            while (resultSet.next()) {

                // Read and display each column by its column name.
                System.out.println("ID: " +
                    resultSet.getInt("id"));

                System.out.println("Company: " +
                    resultSet.getString("company_name"));

                System.out.println("Job Role: " +
                    resultSet.getString("job_role"));

                System.out.println("Status: " +
                    resultSet.getString("status"));

                System.out.println("Application Date: " +
                    resultSet.getDate("application_date"));

                System.out.println("Description: " +
                    resultSet.getString("job_description"));

                // Add a blank line between applications.
                System.out.println();
            }

        } catch (SQLException e) {

            // Display any database error.
            System.out.println("Database operation failed!");
            System.out.println(e.getMessage());
        }
    }
}
