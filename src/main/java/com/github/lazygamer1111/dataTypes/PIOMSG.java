package com.github.lazygamer1111.dataTypes;

import org.jetbrains.annotations.NotNull;

public record PIOMSG(String command, int id, String msg) {
    public static PIOMSG fromThrottle(int id, int throttle, boolean telemetry){
        return new PIOMSG("THROTTLE", id, String.format("%d %b", throttle, telemetry));
    }

    public static PIOMSG fromOK(int id){
        return new PIOMSG("OK", id, "");
    }

    public static PIOMSG fromERR(int id, String msg){
        return new PIOMSG("ERR", id, msg);
    }

    @NotNull
    public String toString() {
        return String.format("%s %d %s", command, id, msg);
    }
}
