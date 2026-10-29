
public class JobApplication {

    // Store the company name
    String companyName;

    // Store the job role or position
    String jobRole;

    // Store the current application status
    String status;

    // NEW: Store the date when the application was submitted
    String applicationDate;

    // NEW: Store a description of the job
    String jobDescription;

    // Constructor: initialize all five fields when creating an application
    public JobApplication(
            String companyName,
            String jobRole,
            String status,
            String applicationDate,
            String jobDescription) {

        // Assign the supplied values to this object's fields
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.status = status;
        this.applicationDate = applicationDate;
        this.jobDescription = jobDescription;
    }
}
