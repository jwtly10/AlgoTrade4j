package dev.jwtly10.core;

public interface BarDataListener {
    /**
     * Called when a new bar is available
     *
     * @param bar the new bar
     */
    void onBar(Bar bar);
}