
import java.sql.Connection;         // Represents the connection to MySQL.
import java.sql.DriverManager;      // Establishes the database connection.
import java.sql.PreparedStatement;  // Executes SQL with supplied values.
import java.sql.SQLException;       // Handles database errors.

public class DatabaseUpdateTest {

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

        // Update the status of the application belonging to Deloitte.
        String sql = "UPDATE applications SET status = ? WHERE company_name = ?";

        // Connect to MySQL and automatically close resources afterward.
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password);

            // Prepare the SQL update command.
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            // Set the new status for the first question mark.
            statement.setString(1, "Assessment");

            // Set the company name for the second question mark.
            statement.setString(2, "Deloitte");

            // Execute the UPDATE command.
            int rowsUpdated = statement.executeUpdate();

            // Report how many records were updated.
            System.out.println("Rows updated: " + rowsUpdated);

        } catch (SQLException e) {

            // Display any database error.
            System.out.println("Failed to update application.");
            System.out.println(e.getMessage());
        }
    }
}
