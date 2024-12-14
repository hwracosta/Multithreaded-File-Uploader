# Multithreaded File Uploader

## User Manual

### Pre-requisites

#### Environment Setup
1. **Java Version:**
   - Ensure you have Java 17 or higher installed.
   - Use the command `java -version` to verify the installed version.

2. **PostgreSQL:**
   - Install and configure PostgreSQL.
   - Create a database named `fileuploader`.

3. **Dependencies:**
   - Ensure Maven is installed for managing project dependencies.

#### Project Setup

1. **Clone or Download the Project:**
   - Clone the repository or download the project files to your local machine.

2. **Database Setup:**
   - Run the following SQL commands to initialize the required tables:

     ```sql
     CREATE TABLE file_metadata (
         id SERIAL PRIMARY KEY,
         file_name VARCHAR(255) NOT NULL,
         file_size BIGINT NOT NULL,
         total_chunks INTEGER NOT NULL,
         uploaded_chunks INTEGER DEFAULT 0,
         status VARCHAR(50) DEFAULT 'Pending',
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     );

     CREATE TABLE chunk_metadata (
         id SERIAL PRIMARY KEY,
         file_id BIGINT NOT NULL,
         chunk_number INTEGER NOT NULL,
         status VARCHAR(50) DEFAULT 'Pending',
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         progress DOUBLE PRECISION DEFAULT 0.0,
         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     );
     ```

   - Use `psql` or a database client to execute the above SQL commands.

3. **Update Database Credentials:**
   - Modify `DataSourceConfig.java` with your PostgreSQL username and password:

     ```java
     .username("your_username")
     .password("your_password")
     ```

### Build and Run

1. **Build the Project:**
   - Navigate to the project directory.
   - Use Maven to build the project:

     ```bash
     mvn clean install
     ```

2. **Run the Application:**
   - Start the JavaFX application:

     ```bash
     mvn javafx:run
     ```

3. **Launch the UI:**
   - JavaFX will open the main UI window titled **"Multithreaded File Uploader"**.

### Usage

1. **Select a File:**
   - Click the **Select File** button to choose a file for upload.
   - Click the **Select File** once more to choose another file to upload at the same time.

2. **Start Upload:**
   - Press **Start Upload** to initiate the upload process.
   - Observe the progress bar and real-time updates for upload status.

3. **Pause Upload:**
   - Press **Pause Upload** to temporarily halt the upload.
   - The progress bar stops updating, and the resume button is now available.

4. **Resume Upload:**
   - Press **Resume Upload** to continue the upload from where it paused.

5. **Cancel Upload:**
   - Press **Cancel Upload** to terminate the upload process.
   - The upload stops, and the metadata are deleted from the database 

Note:
You can upload multiple files simultaneously. Each file will have its own progress bar and functions for managing its upload status.
---

## Documentation

### Usage of Threads

#### Thread Management

**Purpose and Importance:**  
Threads are the backbone of this project, enabling efficient and seamless file uploads in a multithreaded environment. They allow asynchronous operations, ensuring the application can handle large file uploads without compromising UI responsiveness.

---

**Implementation:**

1. **Threading in File Upload:**
    - The `FileUploadService` uses an `ExecutorService` with a fixed thread pool size of 5, managing and limiting concurrent uploads efficiently.
    - Each file upload is handled by a separate thread, enabling dynamic user interactions (e.g., pause, resume, cancel) while the upload continues in the background.

2. **Integration with JavaFX:**
    - JavaFX requires UI updates to be executed on the JavaFX Application Thread. This is handled using `Platform.runLater`, ensuring thread-safe updates to progress bars, labels, and buttons based on thread states.

---

### Core Threaded Operations

#### Upload Process
- **Files:** `FileUploadService.java`, `MainView.java`
- **The `uploadFile()` Method:**
    - Handles the upload of multiple files concurrently by processing each file in chunks.
    - Reads files in chunks and updates `FileMetadata` and `ChunkMetadata` records in the database.
    - Sends progress updates to the UI via callbacks for each file, ensuring dynamic and real-time progress visualization.
    - Prevents the UI thread from freezing by performing all upload-related operations on separate threads.

---

#### Pause and Resume
- **Files:** `FileUploadService.java`, `MainView.java`
- Threads use the `isPaused` flag to temporarily pause operations. While paused, threads enter a sleep state.
- Upon resuming (`isPaused` set to `false`), threads continue processing from their previous state.
- Supports pausing and resuming uploads for individual files or all files simultaneously without restarting the process.

---

#### Cancel
- **Files:** `FileUploadService.java`, `MainView.java`
- The `isCancelled` flag gracefully stops threads, ensuring any ongoing chunk operation is completed before terminating the upload.
- Removes associated file and chunk metadata from the database via `cleanupCanceledUpload()`, preventing residual data.
- Supports canceling specific file uploads or all uploads in progress, ensuring that canceled operations do not leave stale data in the system.

---

### Thread-Related Methods and Their Roles

| **Method**                   | **Purpose**                                                                 |
|-------------------------------|-----------------------------------------------------------------------------|
| `uploadFile()`                | Core upload logic, executes file uploads on separate threads.              |
| `pauseUpload()`               | Pauses the upload thread via flag control.                                 |
| `resumeUpload()`              | Resumes a paused thread via flag control.                                  |
| `cancelUpload()`              | Cancels the upload thread gracefully and resets state.                     |
| `cleanupCanceledUpload()`     | Deletes metadata after cancellation, ensuring no stale data remains.       |
| `resetUploadState()`          | Resets all upload-related flags for a clean start.                         |
| `resetProgress()`             | Updates the UI to reflect reset progress state.                            |
| `deleteFileMetadataAndChunks()`| Deletes database entries for the canceled file and its chunks.             |

---

### Detailed Breakdown of Thread-Related Methods

1. **`uploadFile()` (Core Threaded Method):**
    - Responsible for uploading files chunk by chunk on a separate thread.
    - Continuously checks for flags (`isPaused`, `isCancelled`) to determine if the upload should be paused or stopped.
    - Reports progress to the UI in real time using callbacks executed via `Platform.runLater`.

2. **`pauseUpload()` (Pausing Threads):**
    - Activates the `isPaused` flag, causing the thread in `uploadFile()` to pause operations temporarily.
    - Ensures threads enter a sleep state when paused, avoiding resource wastage.

3. **`resumeUpload()` (Resuming Threads):**
    - Deactivates the `isPaused` flag, allowing paused threads to continue processing from their last state.
    - Prevents restarting uploads, preserving progress continuity.

4. **`cancelUpload()` (Canceling Threads):**
    - Sets the `isCancelled` flag, gracefully terminating the thread in `uploadFile()`.
    - Ensures all associated metadata is removed from the database by invoking `cleanupCanceledUpload()`.

5. **`cleanupCanceledUpload()` (Post-Cancellation Cleanup):**
    - Removes both `FileMetadata` and `ChunkMetadata` records for the canceled file from the database.
    - Prevents threads from attempting to resume canceled uploads by ensuring all references to the file are deleted.

6. **`resetUploadState()` (Resetting States):**
    - Clears all thread-related flags (`isPaused`, `isCancelled`, `isUploading`) and resets the `currentFile` variable.
    - Ensures threads are in a clean state for new uploads.

7. **`resetProgress()` (UI Synchronization):**
    - Updates the progress bar to reflect a reset state (0%) after an upload is paused or canceled.
    - Works closely with `Platform.runLater` to ensure thread-safe UI updates.

8. **`deleteFileMetadataAndChunks()` (Metadata Cleanup):**
    - Deletes all database records related to a specific file (both file and chunk metadata).
    - Ensures canceled files do not leave orphaned entries in the database.

---

### Why Threads Are Essential

1. **Efficiency:**  
   Threads allow multiple files to be uploaded concurrently, maximizing system resource utilization.

2. **UI Responsiveness:**  
   By running upload tasks on separate threads, the UI remains responsive to user actions (e.g., pausing, resuming, canceling).

3. **Scalability:**  
   The `ExecutorService` ensures that multiple uploads are handled without overwhelming system resources by limiting the number of active threads.

4. **Enhanced User Experience:**  
   Users receive real-time feedback through progress bars and status updates, which are seamlessly integrated into the UI using `Platform.runLater`.

---


### Data Structures Used

1. **File Metadata and Chunk Metadata:**
   - **Entities:**
      - `FileMetadata` tracks file-level details such as file size, name, and upload status.
      - `ChunkMetadata` monitors progress and status at the chunk level.
   - **Database Integration:** These entities are persisted in a PostgreSQL database for tracking and recovery purposes.
   - **Fields:** Include IDs, chunk numbers, progress percentages, and status strings.

2. **Thread Control Flags:**
   - **Booleans (`isPaused`, `isCancelled`, `isUploading`):**
      - Manage thread states for pausing, resuming, and canceling uploads.

3. **ExecutorService:**
   - A fixed-size thread pool manages active upload threads, balancing system resources effectively.

4. **Collections:**
   - **Lists:** Temporarily hold file chunks during processing in the `FileChunker` class.

---

### Feasibility Without Threads:
- **UI Freezing:** Without threads, the UI would become unresponsive during uploads, leading to a poor user experience. Files would be uploaded sequentially. 
- **Limited Functionality:** Features like pause, resume, and cancel would not be feasible without threads.

## Contact Us

For any questions or support, feel free to reach out:

- **Harry Acosta**: [hracosta@up.edu.ph](mailto:hracosta@up.edu.ph)
- **Alyssa Surban**: [ansurban@up.edu.ph](mailto:ansurban@up.edu.ph)
- **Monty Yu**: [tayu1@up.edu.ph](mailto:tayu1@up.edu.ph)  
