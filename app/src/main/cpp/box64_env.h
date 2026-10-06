/**
 * box64_env.h - Dynarec Environment Tuning for Qualcomm Snapdragon Adreno
 * Definitions and environment variables for Box64 x86_64 JIT on ARM64.
 */

#ifndef BOX64_ENV_H
#define BOX64_ENV_H

// Core Box64 Dynarec Performance Flags
#define ENV_BOX64_DYNAREC           "BOX64_DYNAREC"            // 1 = Enable JIT dynamic recompiler
#define ENV_BOX64_DYNAREC_FASTNAN   "BOX64_DYNAREC_FASTNAN"    // 1 = Fast IEEE-754 NaN handling
#define ENV_BOX64_DYNAREC_FASTROUND "BOX64_DYNAREC_FASTROUND"  // 1 = Faster FP rounding mode
#define ENV_BOX64_DYNAREC_X87DOUBLE "BOX64_DYNAREC_X87DOUBLE"  // 0/1 = Force 64-bit precision for x87
#define ENV_BOX64_DYNAREC_BIGBLOCK  "BOX64_DYNAREC_BIGBLOCK"   // 0=off, 1=safe, 2=aggressive (high speed)
#define ENV_BOX64_DYNAREC_STRONGMEM "BOX64_DYNAREC_STRONGMEM"  // 0=none, 1=weak, 2=strong memory ordering
#define ENV_BOX64_DYNAREC_SAFEFLAGS "BOX64_DYNAREC_SAFEFLAGS"  // 1=safe flag computation, 2=all flags
#define ENV_BOX64_DYNAREC_CALLRET   "BOX64_DYNAREC_CALLRET"    // 1=optimize call/ret pairing
#define ENV_BOX64_DYNAREC_WAIT      "BOX64_DYNAREC_WAIT"       // Wait for JIT compiler thread

// Turnip & Mesa Graphics Environment Flags (Bypass Qualcomm Proprietary Driver)
#define ENV_VK_ICD_FILENAMES        "VK_ICD_FILENAMES"         // Point to turnip_icd.aarch64.json
#define ENV_MESA_VK_WSI_PRESENT_MODE "MESA_VK_WSI_PRESENT_MODE" // mailbox (lowest input latency) or fifo
#define ENV_TU_DEBUG                "TU_DEBUG"                 // noconform, gmem, cche
#define ENV_MESA_DEBUG              "MESA_DEBUG"               // silent in production

// Wine Execution Flags
#define ENV_WINEPREFIX              "WINEPREFIX"               // Container storage directory
#define ENV_WINEDEBUG               "WINEDEBUG"                // -all (suppress debug output for max FPS)
#define ENV_WINEESYNC               "WINEESYNC"                // 1 = eventfd-based synchronization
#define ENV_WINEFSYNC               "WINEFSYNC"                // 1 = futex2 synchronization (if kernel >= 5.16)
#define ENV_WINEDLLOVERRIDES        "WINEDLLOVERRIDES"         // Override stock DLLs (dxgi=n,b; d3d11=n,b)

#endif // BOX64_ENV_H
