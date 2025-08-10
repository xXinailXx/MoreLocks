package net.xXinailXx.more_locks.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LockScreen extends Screen {
    private final int countLocks;

    public LockScreen(int countLocks) {
        super(Component.empty());
        this.countLocks = countLocks;
    }
}
