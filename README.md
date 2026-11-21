# GPUMS
A Java OOP application that simulates a GPU Utilization Management System, including features like job scheduling, student and admin accounts, and GPU utilization. 

## Requirements

- Java 21
- Eclipse or another Java IDE
- No external libraries required

## Running the application

1. Open the project in Eclipse.
2. Open `src/ui/LoginFrame.java`.
3. Run it as a Java Application.

Default administrator login:
- Username: `admin`
- Password: `gpu2026`

## Using the application

1. Register a student account from the login screen.
2. Log in as administrator and approve the registration.
   The initial GPU allowance can be adjusted during approval.
3. Set the simulation starting date and time.
4. Log in as the approved student to submit jobs.

There are four GPUs, each running one job at a time.
Additional jobs wait in first-come, first-served order.

Job durations:
- Small: 5 minutes
- Medium: 15 minutes
- Large: 30 minutes
- Very large: 60 minutes

Simulation time advances only when the administrator clicks
Advance minute. Real-world waiting does not advance the simulation.

## GPU allowances

Submitting a job reserves its full estimated duration.
Available minutes equal allocated minus used minus reserved minutes.

Completed jobs are charged their full duration.
Cancelled running jobs are charged their elapsed runtime.
Cancelled queued jobs are charged zero minutes.
Cancellation releases the job's reservation.

Students can request extra time. Administrators can approve requests
or grant extra time directly with a recorded reason.

## Saving and loading

To save and exit:
1. Log out to return to the login screen.
2. Close the login window using its close button.

The application saves these files in its working directory:
- `accounts.txt`
- `requests.txt`
- `jobs.txt`
- `clock.txt`

All four files are loaded together on the next launch.
The simulation resumes from its saved time.

Stopping the application through Eclipse's Terminate button bypasses
the normal save-on-close process.

## Project structure

- `model`: users, jobs, GPUs, requests and time allowances
- `logic`: account management and scheduling
- `storage`: saving and loading
- `ui`: Swing windows, dialogs and dashboard