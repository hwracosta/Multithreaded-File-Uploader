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
   - Start the Spring Boot application:

     ```bash
     mvn spring-boot:run
     ```

   - Alternatively, run the generated JAR file:

     ```bash
     java -jar target/MultithreadedFileUploader-1.0.jar
     ```

3. **Launch the UI:**
   - JavaFX will open the main UI window titled **"Multithreaded File Uploader"**.

### Usage

1. **Select a File:**
   - Click the **Select File** button to choose a file for upload.

2. **Start Upload:**
   - Press **Start Upload** to initiate the upload process.
   - Observe the progress bar and real-time updates for upload status.

3. **Pause Upload:**
   - Press **Pause Upload** to temporarily halt the upload.
   - The progress bar stops updating, and the status changes to "Upload Paused".

4. **Resume Upload:**
   - Press **Resume Upload** to continue the upload from where it paused.

5. **Cancel Upload:**
   - Press **Cancel Upload** to terminate the upload process.
   - The upload stops, and the status updates to "Upload Cancelled".

Note:
You can upload multiple files simultaneously. Each file will have its own progress bar and controls for managing its upload status.
---

## Documentation

### Usage of Threads

#### Thread Management

**Purpose and Importance:**  
Threads are the backbone of this project, enabling efficient and seamless file uploads in a multithreaded environment. They allow asynchronous operations, ensuring the application can handle large file uploads without compromising UI responsiveness.

**Implementation:**

1. **Threading in File Upload:**
   - The `FileUploadService` uses an `ExecutorService` with a fixed thread pool size of 5, managing and limiting concurrent uploads efficiently.
   - Each file upload is handled by a separate thread, enabling dynamic user interactions (e.g., pause, resume, cancel) while the upload continues in the background.

2. **Integration with JavaFX:**
   - JavaFX requires UI updates to be executed on the JavaFX Application Thread. This is handled using `Platform.runLater`, ensuring thread-safe updates to progress bars, labels, and buttons based on thread states.


# Core Threaded Operations  

## Upload Process  
- *Files:* FileUploadService.java, MainView.java  
- **The uploadFile Method:**  
  - Handles the upload of *multiple files* concurrently by processing each file in chunks.  
  - Updates FileMetadata and ChunkMetadata records in the database as each chunk is processed.  
  - Sends progress updates to the UI via callbacks for each file.  

Without Threads: The UI would freeze during uploads, causing poor user experience and limiting interactivity.  
 
With Threads: Each file's upload runs on separate threads, ensuring smooth UI performance and user interaction.  


## Pause and Resume  
- *Files:* FileUploadService.java, MainView.java  
- Threads use the isPaused flag to temporarily pause operations. While paused, threads enter a sleep state.  
- Upon resuming (isPaused set to false), threads continue processing from their previous state.  
- Supports pausing and resuming uploads for individual files or all files simultaneously.  

## Cancel  
- *Files:* FileUploadService.java, MainView.java  
- The isCancelled flag gracefully stops threads, saving partial progress and cleaning up related metadata.  
- Supports canceling specific file uploads or all uploads in progress.  

#### UI Integration

1. **Files:** `MainView.java`, `FileUploadService.java`

2. **Thread Enhancements for UI:**
   - UI elements like start, pause, resume, and cancel buttons interact with threads using shared flags (`isPaused`, `isCancelled`).
   - UI updates such as progress bars and status labels are executed dynamically via `Platform.runLater`.

3. **Benefits:**
   - Prevents UI unresponsiveness during long uploads.
   - Enhances user experience with real-time feedback and control.

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

### Why Threads Matter

#### Feasibility Without Threads:
- **UI Freezing:** Without threads, the UI would become unresponsive during uploads, leading to a poor user experience.
- **Limited Functionality:** Features like pause, resume, and cancel would not be feasible without threads.

#### Proof of Concept:
This project demonstrates the practical use of threads in:
- Ensuring non-blocking execution.
- Supporting dynamic user interactions during background operations.
- Managing resource-intensive tasks efficiently with thread pooling.

---

### Architectural Highlights

1. **FileUploadService.java:**
   - Manages thread operations and database updates for file uploads.

2. **MainView.java:**
   - Connects the UI with threading operations for seamless user interactions.

3. **FileChunker.java:**
   - Prepares files by dividing them into manageable chunks for upload.

4. **FileUploadController.java:**
   - Provides REST endpoints for pausing, resuming, and canceling uploads.

---

## Contact Us

For any questions or support, feel free to reach out:

- **Harry Acosta**: [hracosta@up.edu.ph](mailto:hracosta@up.edu.ph)
- **Alyssa Surban**: [ansurban@up.edu.ph](mailto:ansurban@up.edu.ph)
- **Monty Yu**: [tayu1@up.edu.ph](mailto:tayu1@up.edu.ph)  
