// src/main/java/com/bo/cultivatereg/client/ClientState.java
package com.bo.cultivatereg.client;
public final class ClientState {
    private static boolean QI_SIGHT = false;
    public static boolean isQiSightEnabled() { return QI_SIGHT; }
    public static boolean toggleQiSight() { QI_SIGHT = !QI_SIGHT; return QI_SIGHT; }
    private ClientState() {}
}
