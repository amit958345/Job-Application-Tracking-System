
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

@WebServlet("/api/applications")
public class ApplicationServlet extends HttpServlet {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/job_tracker";
        String username = "root";

        // Read the password from an environment variable.
        // Never put your real password in this file.
        String password = System.getenv("JOBTRACKER_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new SQLException(
                "JOBTRACKER_DB_PASSWORD environment variable is not configured."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver could not be loaded.", e
            );
        }

        return DriverManager.getConnection(url, username, password);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder jsonResponse = new StringBuilder("[");
        String sql = """
            SELECT id, company_name, job_role,
                   status, application_date, job_description
            FROM applications
            ORDER BY id DESC
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            boolean first = true;

            while (resultSet.next()) {
                if (!first) {
                    jsonResponse.append(",");
                }

                jsonResponse.append("{");
                jsonResponse.append("\"id\":")
                    .append(resultSet.getInt("id")).append(",");

                jsonResponse.append("\"company\":\"")
                    .append(json(resultSet.getString("company_name")))
                    .append("\",");

                jsonResponse.append("\"role\":\"")
                    .append(json(resultSet.getString("job_role")))
                    .append("\",");

                jsonResponse.append("\"status\":\"")
                    .append(json(resultSet.getString("status")))
                    .append("\",");

                java.sql.Date date =
                    resultSet.getDate("application_date");

                jsonResponse.append("\"date\":\"")
                    .append(date == null ? "" : date.toString())
                    .append("\",");

                jsonResponse.append("\"description\":\"")
                    .append(json(resultSet.getString("job_description")))
                    .append("\"}");

                first = false;
            }

            jsonResponse.append("]");

            try (PrintWriter out = response.getWriter()) {
                out.print(jsonResponse);
            }

        } catch (SQLException e) {
            getServletContext().log(
                "Unable to retrieve job applications.", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Unable to load applications.\"}"
                );
            }
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String company = request.getParameter("company");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String date = request.getParameter("date");
        String description = request.getParameter("description");

        if (company == null || company.isBlank()
                || role == null || role.isBlank()
                || status == null || status.isBlank()
                || date == null || date.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Company, role, status and date are required.\"}"
                );
            }
            return;
        }

        if (!Set.of(
                "Applied",
                "Assessment",
                "Interview",
                "Selected",
                "Rejected"
        ).contains(status)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Invalid application status.\"}");
            }
            return;
        }

        LocalDate applicationDate;

        try {
            applicationDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Date must be in YYYY-MM-DD format.\"}"
                );
            }
            return;
        }

        String sql = """
            INSERT INTO applications
                (company_name, job_role, status,
                 application_date, job_description)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, company.trim());
            statement.setString(2, role.trim());
            statement.setString(3, status);
            statement.setDate(
                4, java.sql.Date.valueOf(applicationDate)
            );
            statement.setString(
                5, description == null ? "" : description.trim()
            );

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted == 1) {
                response.setStatus(HttpServletResponse.SC_CREATED);

                try (PrintWriter out = response.getWriter()) {
                    out.print(
                        "{\"message\":\"Application added successfully.\"}"
                    );
                }
            } else {
                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                try (PrintWriter out = response.getWriter()) {
                    out.print(
                        "{\"error\":\"Application could not be added.\"}"
                    );
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                "Unable to insert job application.", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Unable to save application to the database.\"}"
                );
            }
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParameter = request.getParameter("id");
        int applicationId;

        try {
            if (idParameter == null || idParameter.isBlank()) {
                throw new NumberFormatException("Missing application ID");
            }

            applicationId = Integer.parseInt(idParameter);

            if (applicationId <= 0) {
                throw new NumberFormatException("Invalid application ID");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"A valid application ID is required.\"}");
            }
            return;
        }

        String sql = "DELETE FROM applications WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, applicationId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted == 1) {
                response.setStatus(HttpServletResponse.SC_OK);

                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"message\":\"Application deleted successfully.\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Application not found.\"}");
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                "Unable to delete job application.", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Unable to delete application from the database.\"}"
                );
            }
        }
    }

    
    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParameter = request.getParameter("id");
        String company = request.getParameter("company");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String date = request.getParameter("date");
        String description = request.getParameter("description");

        int applicationId;

        try {
            if (idParameter == null || idParameter.isBlank()) {
                throw new NumberFormatException("Missing application ID");
            }

            applicationId = Integer.parseInt(idParameter);

            if (applicationId <= 0) {
                throw new NumberFormatException("Invalid application ID");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"A valid application ID is required.\"}");
            }
            return;
        }

        if (company == null || company.isBlank()
                || role == null || role.isBlank()
                || status == null || status.isBlank()
                || date == null || date.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Company, role, status and date are required.\"}"
                );
            }
            return;
        }

        if (!Set.of(
                "Applied",
                "Assessment",
                "Interview",
                "Selected",
                "Rejected"
        ).contains(status)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Invalid application status.\"}");
            }
            return;
        }

        LocalDate applicationDate;

        try {
            applicationDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Date must be in YYYY-MM-DD format.\"}"
                );
            }
            return;
        }

        String sql = """
            UPDATE applications
            SET company_name = ?,
                job_role = ?,
                status = ?,
                application_date = ?,
                job_description = ?
            WHERE id = ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, company.trim());
            statement.setString(2, role.trim());
            statement.setString(3, status);
            statement.setDate(
                4, java.sql.Date.valueOf(applicationDate)
            );
            statement.setString(
                5, description == null ? "" : description.trim()
            );
            statement.setInt(6, applicationId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 1) {
                response.setStatus(HttpServletResponse.SC_OK);

                try (PrintWriter out = response.getWriter()) {
                    out.print(
                        "{\"message\":\"Application updated successfully.\"}"
                    );
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Application not found.\"}");
                }
            }

        } catch (SQLException e) {
            getServletContext().log(
                "Unable to update job application.", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            try (PrintWriter out = response.getWriter()) {
                out.print(
                    "{\"error\":\"Unable to update application in the database.\"}"
                );
            }
        }
    }


    private String json(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
