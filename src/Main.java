
import java.util.ArrayList; // Import ArrayList to store multiple job applications.

public class Main {

    public static void main(String[] args) {

        // Create an empty list to store JobApplication objects.
        ArrayList<JobApplication> applications = new ArrayList<>();

        // Add the first application with all five fields.
        applications.add(new JobApplication(
            "Infosys",
            "Java Developer",
            "Applied",
            "2026-10-20",
            "Develop and maintain Java applications."
        ));

        // Add the second application.
        applications.add(new JobApplication(
            "TCS",
            "Software Engineer",
            "Interview",
            "2026-10-21",
            "Work on software development and testing."
        ));

        // Add the third application.
        applications.add(new JobApplication(
            "Wipro",
            "Graduate Engineer Trainee",
            "Assessment",
            "2026-10-22",
            "Assist with software engineering projects."
        ));

        // Add the fourth application.
        applications.add(new JobApplication(
            "Google",
            "Software Engineer",
            "Applied",
            "2026-10-23",
            "Design, develop, and improve software systems."
        ));

        // Update Google's application status.
        // Index 3 refers to the fourth application.
        applications.get(3).status = "Assessment";


        // ==================== SEARCH APPLICATIONS ====================

        // Display a heading for the search results.
        System.out.println("\n=== Search Results ===");

        // Store the company name we want to search for.
        String searchCompany = "TCS";

        // Check each application for a matching company name.
        for (JobApplication app : applications) {

            // Match the company name without considering letter case.
            if (app.companyName.equalsIgnoreCase(searchCompany)) {

                // Display the matching application's details.
                System.out.println(
                    app.companyName + " | " +
                    app.jobRole + " | " +
                    app.status
                );

                // Display the application date.
                System.out.println("Date: " + app.applicationDate);

                // Display the job description.
                System.out.println("Description: " + app.jobDescription);
            }
        }


        // ==================== DELETE APPLICATION ====================

        // Store the company name of the application to delete.
        String companyToDelete = "Wipro";

        // Remove applications whose company name matches Wipro.
        applications.removeIf(app ->
            app.companyName.equalsIgnoreCase(companyToDelete)
        );

        // Display the number of applications remaining after deletion.
        System.out.println(
            "\nTotal applications after deletion: " + applications.size()
        );


        // ==================== DISPLAY ALL APPLICATIONS ====================

        // Display a heading before listing all remaining applications.
        System.out.println("\n=== Job Application Tracker ===");

        // Display every application remaining in the list.
        for (JobApplication app : applications) {

            // Display the company, role, and current status.
            System.out.println(
                app.companyName + " | " +
                app.jobRole + " | " +
                app.status
            );

            // Display the date the application was submitted.
            System.out.println("Date: " + app.applicationDate);

            // Display the job description.
            System.out.println("Description: " + app.jobDescription);

            // Print a blank line to separate applications.
            System.out.println();
        }

        // Display the final number of applications.
        System.out.println("Total applications: " + applications.size());
    }
}