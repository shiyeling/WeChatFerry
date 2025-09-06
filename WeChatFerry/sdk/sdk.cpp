#include "sdk.h"

#include <chrono>
#include <filesystem>
#include <fstream>
#include <optional>
#include <process.h>
#include <sstream>
#include <thread>

#include "framework.h"
#include <tlhelp32.h>

#include "injector.h"
#include "util.h"

// Windows头文件
#include <windows.h>
#include <commctrl.h>
#include <objbase.h>

// C++标准库
#include <string>
#include <vector>
#include <iostream>
#include <filesystem>

// GDI+头文件
#include <gdiplus.h>
using namespace Gdiplus;

// 其他Windows库
#pragma comment(lib, "gdiplus.lib")
#pragma comment(lib, "user32.lib")
#pragma comment(lib, "ole32.lib")
#pragma comment(lib, "crypt32.lib")
#pragma comment(lib, "shlwapi.lib")

using namespace Gdiplus;
using namespace std;

extern "C" IMAGE_DOS_HEADER __ImageBase;

static bool injected    = false;
static HANDLE wcProcess = NULL;
static HMODULE spyBase  = NULL;
static std::string spyDllPath;

//区分MSVC和MinGW
#ifdef _MSC_VER
constexpr char WCFSDKDLL[]       = "sdk.dll";
constexpr char WCFSPYDLL[]       = "spy.dll";
constexpr char WCFSPYDLL_DEBUG[] = "spy_debug.dll";
#else
constexpr char WCFSDKDLL[]       = "libsdk.dll";
constexpr char WCFSPYDLL[]       = "libspy.dll";
constexpr char WCFSPYDLL_DEBUG[] = "libspyd.dll";
#endif

constexpr std::string_view DISCLAIMER_FLAG      = ".license_accepted.flag";
constexpr std::string_view DISCLAIMER_TEXT_FILE = "DISCLAIMER.md";


// 定义返回结构体
struct ScreenshotResult
{
    BOOL success;           // 是否成功
    WCHAR filePath[MAX_PATH]; // 图片文件路径
    INT width;              // 图片宽度
    INT height;             // 图片高度
};


// 前向声明
BOOL CALLBACK EnumWindowsProc(HWND hwnd, LPARAM lParam);
BOOL TakeWindowScreenshot(HWND hWnd, const WCHAR* filename, ScreenshotResult* result);
BOOL IsMainWindow(HWND hWnd);
BOOL GetWindowPID(HWND hWnd, DWORD* pid);

// 存储进程ID和结果的结构
struct EnumData
{
    DWORD pid;
    ScreenshotResult* results;
    int count;
    int maxCount;
};


namespace fs = std::filesystem;

static fs::path get_module_directory()
{
    char buffer[MAX_PATH] = { 0 };
    HMODULE hModule       = reinterpret_cast<HMODULE>(&__ImageBase);
    GetModuleFileNameA(hModule, buffer, MAX_PATH);
    fs::path modulePath(buffer);
    return modulePath.parent_path();
}

static bool show_disclaimer()
{
    fs::path sdk_path = get_module_directory();
    if (fs::exists(sdk_path / DISCLAIMER_FLAG)) {
        return true;
    }

    fs::path path = sdk_path / DISCLAIMER_TEXT_FILE;
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) {
        util::MsgBox(NULL, "免责声明文件读取失败。", "错误", MB_ICONERROR);
        return false;
    }

    auto disclaimerText = std::string((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    if (disclaimerText.empty()) {
        util::MsgBox(NULL, "免责声明文件为空", "错误", MB_ICONERROR);
        return false;
    }

    int result = util::MsgBox(NULL, disclaimerText.c_str(), "免责声明", MB_ICONWARNING | MB_OKCANCEL | MB_DEFBUTTON2);
    if (result == IDCANCEL) {
        util::MsgBox(NULL, "您拒绝了免责声明，程序将退出。", "提示", MB_ICONINFORMATION);
        return false;
    }

    std::ofstream flagFile(sdk_path / DISCLAIMER_FLAG, std::ios::out | std::ios::trunc);
    if (!flagFile) {
        util::MsgBox(NULL, "无法创建协议标志文件。", "错误", MB_ICONERROR);
        return false;
    }
    flagFile << "User accepted the license agreement.";

    return true;
}

static std::string get_dll_path(bool debug)
{
    char buffer[MAX_PATH] = { 0 };
    GetModuleFileNameA(GetModuleHandleA(WCFSDKDLL), buffer, MAX_PATH);

    fs::path path(buffer);
    path.remove_filename(); // 只保留目录路径
    path /= debug ? WCFSPYDLL_DEBUG : WCFSPYDLL;

    if (!fs::exists(path)) {
        util::MsgBox(NULL, path.string().c_str(), "文件不存在", MB_ICONERROR);
        return "";
    }

    return path.string();
}



// 检查窗口是否属于指定进程
BOOL GetWindowPID(HWND hWnd, DWORD* pid)
{
    return GetWindowThreadProcessId(hWnd, pid);
}

// 检查窗口是否是主窗口
BOOL IsMainWindow(HWND hWnd)
{
    return GetWindow(hWnd, GW_OWNER) == (HWND)0 && IsWindowVisible(hWnd);
}

// 枚举窗口的回调函数
BOOL CALLBACK EnumWindowsProc(HWND hwnd, LPARAM lParam)
{
    EnumData* data = (EnumData*)lParam;
    DWORD windowPid = 0;

    if (IsMainWindow(hwnd) && GetWindowPID(hwnd, &windowPid) && windowPid == data->pid)
    {
        if (data->count < data->maxCount)
        {
            // 生成临时文件名
            WCHAR tempPath[MAX_PATH];
            WCHAR fileName[MAX_PATH];

            GetTempPathW(MAX_PATH, tempPath);
            _snwprintf_s(fileName, MAX_PATH, _TRUNCATE, L"%sscreenshot_%d_%p.bmp",
                         tempPath, data->pid, hwnd);

            // 截取窗口截图
            if (TakeWindowScreenshot(hwnd, fileName, &data->results[data->count]))
            {
                data->count++;
            }
        }
    }
    return TRUE;
}

// 截取指定窗口的截图
BOOL TakeWindowScreenshot(HWND hWnd, const WCHAR* filename, ScreenshotResult* result)
{
    // 检查窗口是否最小化
    if (IsIconic(hWnd))
    {
        return FALSE;
    }

    RECT windowRect;
    GetClientRect(hWnd, &windowRect);

    int width = windowRect.right - windowRect.left;
    int height = windowRect.bottom - windowRect.top;

    if (width == 0 || height == 0)
    {
        return FALSE;
    }

    HDC hdcScreen = GetDC(NULL);
    HDC hdc = CreateCompatibleDC(hdcScreen);
    HBITMAP hBitmap = CreateCompatibleBitmap(hdcScreen, width, height);
    SelectObject(hdc, hBitmap);

    // 打印窗口
    PrintWindow(hWnd, hdc, PW_CLIENTONLY);

    // 使用GDI+保存为BMP
    Bitmap* bitmap = Bitmap::FromHBITMAP(hBitmap, NULL);

    CLSID bmpClsid;
    CLSIDFromString(L"{557CF400-1A04-11D3-9A73-0000F81EF32E}", &bmpClsid);

    Status status = bitmap->Save(filename, &bmpClsid, NULL);

    delete bitmap;
    DeleteObject(hBitmap);
    DeleteDC(hdc);
    ReleaseDC(NULL, hdcScreen);

    if (status == Ok)
    {
        // 填充结果结构
        result->success = TRUE;
        wcscpy_s(result->filePath, MAX_PATH, filename);
        result->width = width;
        result->height = height;
        return TRUE;
    }
    else
    {
        // 删除失败的文件
        DeleteFileW(filename);
        return FALSE;
    }
}

int GetProcessScreenshots(DWORD pid, ScreenshotResult** results)
{
    // 分配初始内存
    int maxResults = 10;
    *results = (ScreenshotResult*)CoTaskMemAlloc(maxResults * sizeof(ScreenshotResult));

    if (*results == NULL)
    {
        return 0;
    }

    // 初始化结果数组
    for (int i = 0; i < maxResults; i++)
    {
        (*results)[i].success = FALSE;
        (*results)[i].filePath[0] = L'\0';
        (*results)[i].width = 0;
        (*results)[i].height = 0;
    }

    // 枚举窗口数据
    EnumData data;
    data.pid = pid;
    data.results = *results;
    data.count = 0;
    data.maxCount = maxResults;

    // 枚举所有窗口
    EnumWindows(EnumWindowsProc, (LPARAM)&data);

    return data.count;
}

void FreeScreenshotResults(ScreenshotResult* results)
{
    if (results)
    {
        CoTaskMemFree(results);
    }
}

// 记录一个全局的当前微信ID
DWORD wcPid = 0;

extern "C" {
__declspec(dllexport) int WxInitSDK(bool debug, int port) {
    if (!show_disclaimer()) {
        exit(-1); // 用户拒绝协议，退出程序
    }

    int status  = 0;

    spyDllPath = get_dll_path(debug);
    if (spyDllPath.empty()) {
        return ERROR_FILE_NOT_FOUND; // DLL 文件路径不存在
    }

    status = util::open_wechat(wcPid);
    if (status != 0) {
        util::MsgBox(NULL, "打开微信失败", "WxInitSDK", 0);
        return status;
    }

    std::this_thread::sleep_for(std::chrono::seconds(2)); // 等待微信打开
    wcProcess = inject_dll(wcPid, spyDllPath, &spyBase);
    if (wcProcess == NULL) {
        util::MsgBox(NULL, "注入失败", "WxInitSDK", 0);
        return -1;
    }
    injected = true;

    util::PortPath pp = { 0 };
    pp.port           = port;
    snprintf(pp.path, MAX_PATH, "%s", fs::current_path().string().c_str());

    status       = -3; // TODO: 统一错误码
    bool success = call_dll_func_ex(wcProcess, spyDllPath, spyBase, "InitSpy", (LPVOID)&pp, sizeof(util::PortPath),
                                    (DWORD *)&status);
    if (!success || status != 0) {
        WxDestroySDK();
    }

    return status;
}

__declspec(dllexport) int WxDestroySDK()
{
    if (!injected) {
        return 1; // 未注入
    }

    if (!call_dll_func(wcProcess, spyDllPath, spyBase, "CleanupSpy", NULL)) {
        return -1;
    }

    if (!eject_dll(wcProcess, spyBase)) {
        return -2;
    }
    injected = false;

    return 0;
}

__declspec(dllexport) const char* GetScreenshot()
{
 // 使用示例：获取记事本进程的截图

    ScreenshotResult* results = NULL;
    int count = GetProcessScreenshots(wcPid, &results);
    std::wstring path;

    if (count > 0)
    {
        for (int i = 0; i < count; i++)
        {
            if (results[i].success)
            {
                path = results[i].filePath;
                std::wstring logMessage = L"截图成功: " + std::wstring(results[i].filePath) +
                         L" (" + std::to_wstring(results[i].width) +
                         L"x" + std::to_wstring(results[i].height) + L")\n";
                util::MsgBox(NULL, util::w2s(logMessage), "GetProcessScreenshot", 0);
            }
        }

        // 释放内存
        FreeScreenshotResults(results);
    }
    else
    {
        printf("未找到该进程的窗口或截图失败\n");
    }
    return path.empty() ? "" : util::w2s(path).c_str();
}
}
