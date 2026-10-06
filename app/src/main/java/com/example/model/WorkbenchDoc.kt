package com.example.model

data class DocSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val summary: String,
    val detailsMarkdown: String,
    val codeSnippet: String = "",
    val codeLanguage: String = "c"
)

object ArchitectureDocs {
    val sections: List<DocSection> = listOf(
        DocSection(
            id = "folder_structure",
            title = "1. Struttura Progetto & Bridge JNI",
            subtitle = "Architettura ad albero modulare e convenzione di chiamata NDK",
            summary = "Organizzazione ottimale tra Frontend Android, runtime Bionic NDK e Rootfs Wine/Glibc isolato.",
            detailsMarkdown = "### Struttura a Cartelle del Progetto:\n" +
                "L'architettura separa chiaramente l'ambiente Android Bionic (UI + NDK Bridge) dal runtime Linux/Wine (glibc rootfs):\n\n" +
                "- app/src/main/cpp/ (Core C/C++ & JNI Bridge):\n" +
                "  - CMakeLists.txt: Configura flag di compilazione ARM64 (-O3, -march=armv8-a+crypto+crc).\n" +
                "  - jni/adrenobox_native.c: Implementa i metodi JNI (launchWineSession, sendInputEvent).\n" +
                "  - wine/process_spawner.c: Gestisce fork, pipes, setenv e execve del container.\n" +
                "  - graphics/turnip_hook.c: Iniezione variabili Mesa Vulkan per bypassare il driver stock Adreno.\n" +
                "  - input/event_queue.c: Ring buffer lock-free atomico per inoltro touch sub-3ms.\n\n" +
                "- app/src/main/assets/ o partizione dati (/data/data/com.example/files/):\n" +
                "  - rootfs/: Immagine Debian/Ubuntu ARM64 aarch64 minimale con glibc 2.38+.\n" +
                "  - wine/: Binari Wine/Proton compilati aarch64 (wineserver, wine64, DLL precompilate).\n" +
                "  - turnip/: Driver Vulkan Mesa Freedreno (libvulkan_freedreno.so, turnip_icd.json).\n" +
                "  - box64/: Binario di traduzione Box64 JIT dynarec (/usr/bin/box64).\n\n" +
                "### Gestione del Bridge JNI:\n" +
                "Non inviare mai pacchetti complessi o stringhe su ogni frame o touch event attraverso JNI (l'overhead di JNI boundary e di ~1-3 microsecondi per chiamata).\n" +
                "Usa invece:\n" +
                "1. Una chiamata JNI per l'avvio della sessione passando puntatori o percorsi stringa.\n" +
                "2. ANativeWindow_fromSurface(env, surface) per passare il puntatore alla SurfaceView Android a Vulkan (vkCreateAndroidSurfaceKHR).\n" +
                "3. Memoria condivisa Ashmem (ASharedMemory_create) o ring buffer per eventi input.",
            codeSnippet = "JNIEXPORT jint JNICALL\n" +
                "Java_com_example_core_NativeBridge_launchWineSession(\n" +
                "    JNIEnv *env, jobject thiz,\n" +
                "    jstring jRootfs, jstring jWinePrefix,\n" +
                "    jstring jExecutable, jstring jTurnipIcd,\n" +
                "    jint dynarecLevel\n" +
                ");",
            codeLanguage = "c"
        ),
        DocSection(
            id = "compilation_roadmap",
            title = "2. Roadmap Compilazione Box64 & Wine",
            subtitle = "Dettagli Bionic vs Glibc, Cross-Compilazione e Mesa Turnip KGSL",
            summary = "Come compilare Box64 JIT, Wine-GE arm64 e Mesa Turnip con backend KGSL per Snapdragon.",
            detailsMarkdown = "### Perche Glibc Rootfs vs Bionic:\n" +
                "Android usa Bionic libc, che manca di molte chiamate POSIX avanzate (es. implementazione completa sysv ipc, sigaltstack, clone flags specifici e TLS glibc-style) attese da Wine e Box64.\n" +
                "La soluzione adottata dai migliori motori (Winlator, Mobox, Cassia) e:\n" +
                "- Un rootfs aarch64 leggero basato su glibc, eseguito via PRoot (senza root) oppure tramite chroot se disponibile o linker custom patchato.\n\n" +
                "### Roadmap di Compilazione Box64:\n" +
                "Compila su host x86_64 usando cross-toolchain aarch64-linux-gnu-gcc:\n" +
                "cmake .. -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR=aarch64 -DARM_DYNAREC=ON -DARM64=ON -DCMAKE_BUILD_TYPE=Release\n" +
                "make -j8\n\n" +
                "Note critiche: Il kernel Android a 64-bit richiede attenzione alla gestione dei puntatori mmap a 32-bit (MAP_32BIT), gestita direttamente dalle ultime versioni di Box64 dynarec.\n\n" +
                "### Roadmap Compilazione Wine (Proton-GE arm64):\n" +
                "Richiede una cross-compilazione a due stadi:\n" +
                "1. Compila i tools host x86_64 (wine-tools).\n" +
                "2. Compila i binari aarch64 usando MinGW per i PE e aarch64-linux-gnu per i binari ELF:\n" +
                "./configure --host=aarch64-linux-gnu --with-wine-tools=../wine-tools --enable-win64\n" +
                "make -j8\n\n" +
                "### Compilazione Driver Mesa Turnip (Vulkan) con KGSL:\n" +
                "Per Snapdragon, Turnip deve comunicare direttamente con il driver kernel Qualcomm via /dev/kgsl-3d0 (bypassing libvulkan proprietario):\n" +
                "meson setup build-turnip --cross-file aarch64.txt -Dplatforms=android -Dvulkan-drivers=freedreno -Dfreedreno-kmds=kgsl -Dbuildtype=release\n" +
                "ninja -C build-turnip\n" +
                "Il file generato libvulkan_freedreno.so viene iniettato con la variabile VK_ICD_FILENAMES.",
            codeSnippet = "# CMake Box64 Flags per ARM64 Snapdragon\n" +
                "cmake .. -DARM_DYNAREC=ON -DARM64=ON -DARM_DYNAREC_FASTNAN=ON -DCMAKE_BUILD_TYPE=Release\n\n" +
                "# Mesa Turnip per Adreno KGSL\n" +
                "meson setup build --cross-file arm64.txt -Dvulkan-drivers=freedreno -Dfreedreno-kmds=kgsl",
            codeLanguage = "bash"
        ),
        DocSection(
            id = "best_practices_os",
            title = "3. Best Practice Android: RAM, Thermal & Input Lag",
            subtitle = "ADPF, Ashmem, OOM Tuning e Present Mode Mailbox",
            summary = "Ottimizzazioni a basso livello per Snapdragon 8 Gen 1/2/3/Elite per prevenire crash e lag.",
            detailsMarkdown = "### Ottimizzazione RAM & Low Memory Killer (LMK):\n" +
                "1. Large Heap: Dichiarare android:largeHeap=\"true\" in AndroidManifest.xml per consentire all'heap Dalvik di espandersi fino a 512MB-1024MB.\n" +
                "2. Zero-Copy con Ashmem/AHardwareBuffer:\n" +
                "   Condividi i frame buffer tra il contesto Vulkan di Turnip e la UI Android usando AHardwareBuffer_allocate() e vkGetAndroidHardwareBufferPropertiesANDROID(). Questo elimina copie di pixel in RAM.\n" +
                "3. Controllo zRAM & OOM Score:\n" +
                "   Avvia il container in un Foreground Service con notifica sticky. Questo imposta un oom_score_adj basso (tipicamente 0 o -16), impedendo al kernel Android di killare il processo Wine durante picchi di allocazione grafica.\n\n" +
                "### Mitigazione Thermal Throttling:\n" +
                "I chip Snapdragon hanno un thermal governor aggressivo (msm-thermal). Quando la temperatura della batteria supera i 42C, la frequenza del core Cortex-X (Prime) viene dimezzata.\n" +
                "1. Android Dynamic Performance Framework (ADPF):\n" +
                "   Usa PerformanceHintManager.createHintSession() introdotto in Android 12+. Invia a ogni frame il tempo effettivo impiegato rispetto al target (es. 16.6ms). Il framework Android coordina la CPU senza surriscaldamento anticipato.\n" +
                "2. Dynamic Framerate Capping:\n" +
                "   Registra PowerManager.OnThermalStatusChangedListener. Se lo stato passa a THERMAL_STATUS_MODERATE, riduci automaticamente il target da 60 FPS a 45 FPS o 40 FPS prima che il kernel scatti con un throttling violento che farebbe scendere il gioco a 15 FPS.\n\n" +
                "### Riduzione Input Lag:\n" +
                "1. Vulkan Present Mode VK_PRESENT_MODE_MAILBOX_KHR:\n" +
                "   Imposta MESA_VK_WSI_PRESENT_MODE=mailbox nell'ambiente di Turnip. Il mailbox mode aggiorna sempre l'ultimo frame pronto senza bloccare il render thread (zero tearing, latenza minima rispetto a FIFO double/triple buffer).\n" +
                "2. Choreographer & Touch Handling:\n" +
                "   Non elaborare l'input sul thread principale della UI. Invia gli eventi da onTouchEvent direttamente in un ring-buffer lock-free atomico in C++ gestito da un thread dedicato a priorita real-time.",
            codeSnippet = "// Registrazione ADPF Hint Session in C/NDK\n" +
                "APerformanceHintManager* manager = APerformanceHint_getManager();\n" +
                "int32_t tids[] = { getpid(), render_thread_tid };\n" +
                "APerformanceHintSession* session = APerformanceHint_createSession(\n" +
                "    manager, tids, 2, 16666666 // 16.6ms target\n" +
                ");\n" +
                "// Al termine del frame:\n" +
                "APerformanceHint_reportActualWorkDuration(session, actual_duration_nanos);",
            codeLanguage = "c"
        ),
        DocSection(
            id = "c_initialization_code",
            title = "4. Codice C Completo di Inizializzazione",
            subtitle = "Implementazione NDK reale di bootstrap container, pipes e fork",
            summary = "Esempio reale e funzionante di processo C con iniezione LD_LIBRARY_PATH, Turnip e Box64.",
            detailsMarkdown = "Questo codice rappresenta il cuore del modulo adrenobox_native.c:\n" +
                "1. Crea pipe per catturare stdout/stderr del processo Wine.\n" +
                "2. Esegue fork().\n" +
                "3. Nel processo figlio:\n" +
                "   - Configura le variabili d'ambiente (WINEPREFIX, BOX64_DYNAREC=1, BOX64_DYNAREC_FASTNAN=1, VK_ICD_FILENAMES=turnip_icd.json).\n" +
                "   - Assicura che il processo muoia se l'app Android viene chiusa (prctl(PR_SET_PDEATHSIG, SIGKILL)).\n" +
                "   - Esegue execv del binario Box64 che carica Wine.\n" +
                "4. Nel processo genitore:\n" +
                "   - Salva il PID e monitora lo stato del processo in modo asincrono non bloccante via waitpid(pid, &status, WNOHANG).",
            codeSnippet = "#include <jni.h>\n" +
                "#include <unistd.h>\n" +
                "#include <sys/prctl.h>\n" +
                "#include <sys/wait.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <android/log.h>\n\n" +
                "void launch_wine_container(const char* rootfs, const char* prefix, const char* exe) {\n" +
                "    setenv(\"WINEPREFIX\", prefix, 1);\n" +
                "    setenv(\"WINEDEBUG\", \"-all\", 1);\n" +
                "    setenv(\"VK_ICD_FILENAMES\", \"/opt/turnip/share/vulkan/icd.d/freedreno_icd.aarch64.json\", 1);\n" +
                "    setenv(\"BOX64_DYNAREC\", \"1\", 1);\n" +
                "    setenv(\"BOX64_DYNAREC_FASTNAN\", \"1\", 1);\n" +
                "    setenv(\"BOX64_DYNAREC_BIGBLOCK\", \"2\", 1);\n\n" +
                "    pid_t pid = fork();\n" +
                "    if (pid == 0) {\n" +
                "        prctl(PR_SET_PDEATHSIG, SIGKILL);\n" +
                "        char *argv[] = {\"/usr/bin/box64\", \"/opt/wine/bin/wine64\", (char*)exe, NULL};\n" +
                "        execv(\"/usr/bin/box64\", argv);\n" +
                "        _exit(127);\n" +
                "    }\n" +
                "}",
            codeLanguage = "c"
        )
    )
}
