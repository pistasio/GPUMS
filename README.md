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

## Sample test data

The `demo-data` folder holds a saved simulation prepared for the demonstration.

To use it:
1. Close the application.
2. Copy the four files from `demo-data` into the project folder (the folder that contains `src`), replacing any existing save files.
3. Run `src/ui/LoginFrame.java`.

What it contains (simulation time 23/11/2026 09:40):
- 5 approved students, 1 pending registration (100000006) and 1 rejected registration (100000007)
- 1 approved extra-time grant (100000002, +20 minutes) and 1 pending extra-time request (100000004, 30 minutes)
- 21 jobs: 10 completed, 4 running (all four GPUs busy), 3 queued and 4 cancelled
  (J107 queued, cancelled by its student; J108 running, cancelled by the administrator;
  J111 running, cancelled by its student; J120 queued, cancelled by the administrator)
- Queued jobs J118, J119 and J121 have predicted waits of 5, 10 and 15 minutes

| Account | Username | Password | Allowance |
|---|---|---|---|
| Administrator | admin | gpu2026 | - |
| Aisha Al Mansoori | 100000001 | aisha101 | 100 |
| Omar Haddad | 100000002 | omar102 | 120 |
| Fatima Al Ali | 100000003 | fatima103 | 100 |
| Yousef Khan | 100000004 | yousef104 | 40 (7 available) |
| Mariam Saeed | 100000005 | mariam105 | 150 |
| Khalid Rashed (pending) | 100000006 | khalid106 | - |
| Noor Hamdan (rejected) | 100000007 | noor107 | - |

Suggested demonstration:
1. Approval: as admin, approve request 6 (Khalid) and change his initial minutes.
2. Allowance limits: log in as 100000004 (7 minutes available). A Medium job is rejected; a Small job is accepted.
3. Waits with all GPUs busy: Dashboard, Waiting queue tab.
4. Cancellations: cancel a queued job as a student, and a running job as the admin from Dashboard, Jobs.
5. Completion: click Advance minute 5 times. J114 completes at 09:45 and J118 starts on GPU 3.
6. Save and reload: log out, close the login window, then run `LoginFrame` again. The clock, jobs and balances are restored.

## Project structure

- `model`: users, jobs, GPUs, requests and time allowances
- `logic`: account management and scheduling
- `storage`: saving and loading
- `ui`: Swing windows, dialogs and dashboard