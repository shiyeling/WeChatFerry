#include "injector.h"

#include <filesystem>

#include "psapi.h"

#include "util.h"

#include <tlhelp32.h>

using namespace std;

static void handle_injection_error(HANDLE process, LPVOID remote_address, const std::string &error_msg)
{
    util::MsgBox(NULL, error_msg.c_str(), "Error", MB_ICONERROR);
    if (remote_address) {
        VirtualFreeEx(process, remote_address, 0, MEM_RELEASE);
    }
    if (process) {
        CloseHandle(process);
    }
}

HMODULE get_target_module_base(HANDLE process, const string &dll)
{
    DWORD needed;
    HMODULE modules[512];
    if (!EnumProcessModulesEx(process, modules, sizeof(modules), &needed, LIST_MODULES_64BIT)) {
        util::MsgBox(NULL, "获取模块失败", "get_target_module_base", 0);
        return NULL;
    }

    DWORD count = needed / sizeof(HMODULE);
    char module_name[MAX_PATH];
    for (DWORD i = 0; i < count; i++) {
        GetModuleBaseNameA(process, modules[i], module_name, sizeof(module_name));
        if (!strncmp(dll.c_str(), module_name, dll.size())) {
            return modules[i];
        }
    }
    return NULL;
}

// 辅助函数：宽字符转窄字符
std::string wstring_to_string(const std::wstring& wstr)
{
    int size_needed = WideCharToMultiByte(CP_UTF8, 0, &wstr[0], (int)wstr.size(), NULL, 0, NULL, NULL);
    std::string str(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, &wstr[0], (int)wstr.size(), &str[0], size_needed, NULL, NULL);
    return str;
}

bool is_dll_loaded(DWORD pid, const std::string& dll_name)
{
    HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE | TH32CS_SNAPMODULE32, pid);
    if (hSnapshot == INVALID_HANDLE_VALUE) {
        return false;
    }

    MODULEENTRY32 me32;
    me32.dwSize = sizeof(MODULEENTRY32);

    bool is_loaded = false;
    std::string target_dll = dll_name;
    std::transform(target_dll.begin(), target_dll.end(), target_dll.begin(), ::tolower);

    if (Module32First(hSnapshot, &me32)) {
        do {
            std::string current_dll = wstring_to_string(me32.szModule);
            std::transform(current_dll.begin(), current_dll.end(), current_dll.begin(), ::tolower);

            if (current_dll.find(target_dll) != std::string::npos) {
                is_loaded = true;
                break;
            }
        } while (Module32Next(hSnapshot, &me32));
    }

    CloseHandle(hSnapshot);
    return is_loaded;
}

HANDLE inject_dll(DWORD pid, const string &dll_path, HMODULE *injected_base)
{
    SIZE_T path_size = dll_path.size() + 1;

    // 1. 打开目标进程
    HANDLE hProcess = OpenProcess(PROCESS_ALL_ACCESS, FALSE, pid);
    if (!hProcess) {
        util::MsgBox(NULL, "打开进程失败", "inject_dll", 0);
        return NULL;
    }

    // 检查DLL是否已经加载
    string dll_name = filesystem::path(dll_path).filename().string();
    if (is_dll_loaded(pid, dll_name)) {
        util::MsgBox(NULL, "DLL已加载", "inject_dll", 0);
        // 或者使用: if (is_dll_loaded_psapi(hProcess, dll_name))
        *injected_base = get_target_module_base(hProcess, dll_name);
        // CloseHandle(hProcess);
        return hProcess; // 或者返回特殊值表示已加载
    }

    // 2. 在目标进程的内存里开辟空间
    LPVOID pRemoteAddress = VirtualAllocEx(hProcess, NULL, path_size, MEM_COMMIT, PAGE_READWRITE);
    if (!pRemoteAddress) {
        handle_injection_error(hProcess, NULL, "DLL 路径写入失败");
        return NULL;
    }

    // 3. 把 dll 的路径写入到目标进程的内存空间中
    WriteProcessMemory(hProcess, pRemoteAddress, dll_path.c_str(), path_size, NULL);

    // 4. 创建一个远程线程，让目标进程调用 LoadLibrary
    HMODULE k32 = GetModuleHandleA("kernel32.dll");
    if (!k32) {
        DWORD error = GetLastError();
        string msg = "获取 kernel32 失败 失败，错误代码: " + to_string(error);
        handle_injection_error(hProcess, pRemoteAddress, msg);
        return NULL;
    }

    FARPROC libAddr = GetProcAddress(k32, "LoadLibraryA");
    if (!libAddr) {
        DWORD error = GetLastError();
        handle_injection_error(hProcess, pRemoteAddress, "获取 LoadLibrary 失败，错误代码: " + to_string(error));
        return NULL;
    }

    HANDLE hThread = CreateRemoteThread(hProcess, NULL, 0, (LPTHREAD_START_ROUTINE)libAddr, pRemoteAddress, 0, NULL);
    if (!hThread) {
        DWORD error = GetLastError();
        handle_injection_error(hProcess, pRemoteAddress, "CreateRemoteThread 失败，错误代码: " + to_string(error));
        return NULL;
    }

    WaitForSingleObject(hThread, INFINITE);
    CloseHandle(hThread);

    *injected_base = get_target_module_base(hProcess, dll_name);

    VirtualFreeEx(hProcess, pRemoteAddress, 0, MEM_RELEASE);
    return hProcess;
}

bool eject_dll(HANDLE process, HMODULE dll_base)
{
    HMODULE k32 = GetModuleHandleA("kernel32.dll");
    if (!k32) {
        util::MsgBox(NULL, "获取 kernel32 失败", "eject_dll", 0);
        return false;
    }

    FARPROC libAddr = GetProcAddress(k32, "FreeLibraryAndExitThread");
    if (!libAddr) {
        util::MsgBox(NULL, "获取 FreeLibrary 失败", "eject_dll", 0);
        return false;
    }

    HANDLE hThread = CreateRemoteThread(process, NULL, 0, (LPTHREAD_START_ROUTINE)libAddr, (LPVOID)dll_base, 0, NULL);
    if (!hThread) {
        util::MsgBox(NULL, "FreeLibrary 调用失败!", "eject_dll", 0);
        return false;
    }

    WaitForSingleObject(hThread, INFINITE);
    CloseHandle(hThread);
    CloseHandle(process);
    return true;
}

static uint64_t get_func_offset(const string &dll_path, const string &func_name)
{
    HMODULE dll = LoadLibraryA(dll_path.c_str());
    if (!dll) {
        util::MsgBox(NULL, "获取 DLL 失败", "get_func_offset", 0);
        return 0;
    }

    LPVOID absAddr  = reinterpret_cast<LPVOID>(GetProcAddress(dll, func_name.c_str()));
    uint64_t offset = reinterpret_cast<uint64_t>(absAddr) - reinterpret_cast<uint64_t>(dll);
    FreeLibrary(dll);

    return offset;
}

bool call_dll_func(HANDLE process, const string &dll_path, HMODULE dll_base, const string &func_name, DWORD *ret)
{
    uint64_t offset = get_func_offset(dll_path, func_name);
    if (offset == 0 || offset > (UINT64_MAX - reinterpret_cast<uint64_t>(dll_base))) {
        return false; // 避免溢出
    }
    uint64_t pFunc = reinterpret_cast<uint64_t>(dll_base) + offset;
    HANDLE hThread = CreateRemoteThread(process, NULL, 0, (LPTHREAD_START_ROUTINE)pFunc, NULL, 0, NULL);
    if (!hThread) {
        return false;
    }
    WaitForSingleObject(hThread, INFINITE);
    if (ret) {
        GetExitCodeThread(hThread, ret);
    }

    CloseHandle(hThread);
    return true;
}

bool call_dll_func_ex(HANDLE process, const string &dll_path, HMODULE dll_base, const string &func_name,
                      LPVOID parameter, size_t size, DWORD *ret)
{
    uint64_t offset = get_func_offset(dll_path, func_name);
    if (offset == 0 || offset > (UINT64_MAX - reinterpret_cast<uint64_t>(dll_base))) {
        return false; // 避免溢出
    }
    uint64_t pFunc        = reinterpret_cast<uint64_t>(dll_base) + offset;
    LPVOID pRemoteAddress = VirtualAllocEx(process, NULL, size, MEM_COMMIT, PAGE_READWRITE);
    if (!pRemoteAddress) {
        util::MsgBox(NULL, "申请内存失败", "call_dll_func_ex", 0);
        return false;
    }

    WriteProcessMemory(process, pRemoteAddress, parameter, size, NULL);

    HANDLE hThread = CreateRemoteThread(process, NULL, 0, (LPTHREAD_START_ROUTINE)pFunc, pRemoteAddress, 0, NULL);
    if (!hThread) {
        VirtualFreeEx(process, pRemoteAddress, 0, MEM_RELEASE);
        util::MsgBox(NULL, "远程调用失败", "call_dll_func_ex", 0);
        return false;
    }

    WaitForSingleObject(hThread, INFINITE);
    VirtualFreeEx(process, pRemoteAddress, 0, MEM_RELEASE);
    if (ret) {
        GetExitCodeThread(hThread, ret);
    }

    CloseHandle(hThread);
    return true;
}
