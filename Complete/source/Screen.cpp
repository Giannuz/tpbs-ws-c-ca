#include <windows.h>
#include <string>
using namespace std;

// Function to get the directory where the exe is located
string GetExeDirectory() {
    char buffer[MAX_PATH];
    GetModuleFileNameA(NULL, buffer, MAX_PATH);
    string::size_type pos = string(buffer).find_last_of("\\/");
    return string(buffer).substr(0, pos);
}

// Function to save bitmap to BMP file (raw Win32 API)
bool SaveBitmapToFile(HBITMAP hBitmap, const char* filename, int width, int height) {
    BITMAPFILEHEADER bmfh;
    BITMAPINFOHEADER bmih;
    DWORD dwBmpSize;
    HANDLE hFile;
    DWORD dwBytesWritten;
    
    // Get bitmap info
    bmih.biSize = sizeof(BITMAPINFOHEADER);
    bmih.biWidth = width;
    bmih.biHeight = height;
    bmih.biPlanes = 1;
    bmih.biBitCount = 24; // 24-bit for smaller file size
    bmih.biCompression = BI_RGB;
    bmih.biSizeImage = 0;
    bmih.biXPelsPerMeter = 0;
    bmih.biYPelsPerMeter = 0;
    bmih.biClrUsed = 0;
    bmih.biClrImportant = 0;
    
    dwBmpSize = ((width * bmih.biBitCount + 31) / 32) * 4 * height;
    
    // Setup file header
    bmfh.bfType = 0x4D42; // "BM"
    bmfh.bfSize = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER) + dwBmpSize;
    bmfh.bfReserved1 = 0;
    bmfh.bfReserved2 = 0;
    bmfh.bfOffBits = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);
    
    // Create file
    hFile = CreateFileA(filename, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) return false;
    
    // Write file header
    WriteFile(hFile, &bmfh, sizeof(BITMAPFILEHEADER), &dwBytesWritten, NULL);
    WriteFile(hFile, &bmih, sizeof(BITMAPINFOHEADER), &dwBytesWritten, NULL);
    
    // Get bitmap bits
    HDC hDC = GetDC(NULL);
    char* lpbitmap = new char[dwBmpSize];
    GetDIBits(hDC, hBitmap, 0, height, lpbitmap, (BITMAPINFO*)&bmih, DIB_RGB_COLORS);
    
    // Write bitmap data
    WriteFile(hFile, lpbitmap, dwBmpSize, &dwBytesWritten, NULL);
    
    // Cleanup
    delete[] lpbitmap;
    ReleaseDC(NULL, hDC);
    CloseHandle(hFile);
    return true;
}

// Windows entry point - no console window
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Get screen dimensions
    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);
    
    // Create device contexts
    HDC screenDC = GetDC(NULL);
    HDC memoryDC = CreateCompatibleDC(screenDC);
    
    // Create bitmap and capture screen
    HBITMAP bitmap = CreateCompatibleBitmap(screenDC, screenWidth, screenHeight);
    HBITMAP oldBitmap = (HBITMAP)SelectObject(memoryDC, bitmap);
    BitBlt(memoryDC, 0, 0, screenWidth, screenHeight, screenDC, 0, 0, SRCCOPY);
    
    // Generate simple filename
    string exeDir = GetExeDirectory();
    string fileName = exeDir + "\\screenshot.bmp";
    
    // Save using fast Win32 API
    SaveBitmapToFile(bitmap, fileName.c_str(), screenWidth, screenHeight);
    
    // Quick cleanup
    SelectObject(memoryDC, oldBitmap);
    DeleteObject(bitmap);
    DeleteDC(memoryDC);
    ReleaseDC(NULL, screenDC);
    
    return 0;
}