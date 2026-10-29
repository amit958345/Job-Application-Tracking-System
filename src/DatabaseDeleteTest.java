
import java.sql.Connection;         // Represents the connection to MySQL.
import java.sql.DriverManager;      // Establishes the database connection.
import java.sql.PreparedStatement;  // Executes SQL with supplied values.
import java.sql.SQLException;       // Handles database errors.

public class DatabaseDeleteTest {

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

        // Delete only the application belonging to Wipro.
        String sql = "DELETE FROM applications WHERE company_name = ?";

        // Connect to MySQL and automatically close resources afterward.
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password);

            // Prepare the SQL delete command.
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            // Supply Wipro as the company to delete.
            statement.setString(1, "Wipro");

            // Execute the DELETE command.
            int rowsDeleted = statement.executeUpdate();

            // Display how many records were deleted.
            System.out.println("Rows deleted: " + rowsDeleted);

        } catch (SQLException e) {

            // Display any database error.
            System.out.println("Failed to delete application.");
            System.out.println(e.getMessage());
        }
    }
}
