// dllmain.cpp : 定义 DLL 应用程序的入口点。
#include "framework.h"
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

// 全局变量用于GDI+初始化
ULONG_PTR gdiplusToken = 0;

BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved)
{
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
        {
            // 初始化GDI+
            GdiplusStartupInput gdiplusStartupInput;
            GdiplusStartup(&gdiplusToken, &gdiplusStartupInput, NULL);
            break;
        }
        case DLL_THREAD_ATTACH:{
            break;
        }
        case DLL_THREAD_DETACH:{
            break;
        }
        case DLL_PROCESS_DETACH:
        {
            // 清理GDI+
            if (gdiplusToken)
            {
                GdiplusShutdown(gdiplusToken);
            }
            break;
        }
            break;
    }
    return TRUE;
}
