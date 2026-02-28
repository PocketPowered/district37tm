plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register<Exec>("runAdminPortal") {
    workingDir = file("$projectDir/event-manager")
    commandLine(
        "sh",
        "-c",
        """
            npm install --legacy-peer-deps
            PORT="${'$'}{ADMIN_PORT_START:-3000}"
            MAX_PORT="$((PORT + 200))"
            is_port_in_use() {
              if command -v lsof >/dev/null 2>&1; then
                lsof -nP -iTCP:"${'$'}1" -sTCP:LISTEN >/dev/null 2>&1
              elif command -v ss >/dev/null 2>&1; then
                ss -ltn "( sport = :${'$'}1 )" 2>/dev/null | tail -n +2 | grep -q .
              else
                node -e "const net=require('net'); const port=Number(process.argv[1]); const hosts=['127.0.0.1','::1']; let done=false; let pending=hosts.length; const finish=(inUse)=>{ if (done) return; done=true; process.exit(inUse ? 0 : 1); }; hosts.forEach((host)=>{ const socket=net.createConnection({ host, port }); socket.setTimeout(250); socket.once('connect', ()=>{ socket.destroy(); finish(true); }); socket.once('timeout', ()=>{ socket.destroy(); if (--pending===0) finish(false); }); socket.once('error', ()=>{ if (--pending===0) finish(false); }); });" "${'$'}1" >/dev/null 2>&1
              fi
            }
            while is_port_in_use "${'$'}PORT"; do
              PORT="$((PORT + 1))"
              if [ "${'$'}PORT" -gt "${'$'}MAX_PORT" ]; then
                echo "Unable to find an open port between ${'$'}{ADMIN_PORT_START:-3000} and ${'$'}MAX_PORT" >&2
                exit 1
              fi
            done
            echo "Starting admin portal on http://localhost:${'$'}{PORT}"
            PORT="${'$'}PORT" npm start
        """.trimIndent()
    )
}
