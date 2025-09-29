#include <windows.h>
#include <fstream>
#include <string>

class ClipboardReader {
public:
    ClipboardReader() {}
    
    std::string getClipboardText() {
        if (!OpenClipboard(nullptr)) {
            return "";
        }
        
        HANDLE hData = GetClipboardData(CF_TEXT);
        if (hData == nullptr) {
            CloseClipboard();
            return "";
        }
        
        char* pszText = static_cast<char*>(GlobalLock(hData));
        if (pszText == nullptr) {
            CloseClipboard();
            return "";
        }
        
        std::string text(pszText);
        GlobalUnlock(hData);
        CloseClipboard();
        return text;
    }
    
    bool saveToFile(const std::string& text, const std::string& filename) {
        std::ofstream file(filename);
        if (!file.is_open()) {
            return false;
        }
        
        file << text;
        file.close();
        return true;
    }
};

// Function to get the directory where the exe is located
std::string GetExeDirectory() {
    char buffer[MAX_PATH];
    GetModuleFileNameA(NULL, buffer, MAX_PATH);
    std::string::size_type pos = std::string(buffer).find_last_of("\\/");
    return std::string(buffer).substr(0, pos);
}

// Windows GUI entry point - completely silent, no windows
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    ClipboardReader reader;
    std::string clipboardText = reader.getClipboardText();
    
    // If clipboard is empty, just exit silently
    if (clipboardText.empty()) {
        return 1;
    }
    
    // Save to file in the same directory as the exe
    std::string exeDir = GetExeDirectory();
    std::string filename = exeDir + "\\clipboard_content.txt";
    
    // Save file and exit silently (no messages)
    if (reader.saveToFile(clipboardText, filename)) {
        return 0;  // Success
    } else {
        return 1;  // Error
    }
}