
import java.sql.Connection;         // Represents the connection to MySQL.
import java.sql.DriverManager;      // Creates the database connection.
import java.sql.PreparedStatement;  // Executes SQL using supplied values.
import java.sql.SQLException;       // Handles database errors.

public class DatabaseInsertTest {

    public static void main(String[] args) {

        // Identify the MySQL database we want to use.
        String url = "jdbc:mysql://localhost:3306/job_tracker";

        // MySQL login details.
        String username = "root";
        String password = System.getenv("JOBTRACKER_DB_PASSWORD"); // Replace with your actual password.
       if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "Database password is not configured."
            );
        }

        // SQL command with placeholders for the application details.
        String sql = "INSERT INTO applications " +
                     "(company_name, job_role, status, application_date, job_description) " +
                     "VALUES (?, ?, ?, ?, ?)";

        // Connect to MySQL and automatically close resources afterward.
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password);

            // Prepare the SQL command for execution.
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            // Supply the company name for the first question mark.
            statement.setString(1, "Deloitte");

            // Supply the job role for the second question mark.
            statement.setString(2, "Java Developer");

            // Supply the application status for the third question mark.
            statement.setString(3, "Applied");

            // Supply the application date for the fourth question mark.
            statement.setDate(
                4, java.sql.Date.valueOf("2026-10-25")
            );

            // Supply the job description for the fifth question mark.
            statement.setString(
                5, "Develop and test Java applications."
            );

            // Execute the INSERT command and save the record in MySQL.
            int rowsInserted = statement.executeUpdate();

            // Confirm whether a record was inserted.
            if (rowsInserted > 0) {
                System.out.println("Application added successfully!");
            }

        } catch (SQLException e) {

            // Display the database error if something goes wrong.
            System.out.println("Failed to add application.");
            System.out.println(e.getMessage());
        }
    }
}
