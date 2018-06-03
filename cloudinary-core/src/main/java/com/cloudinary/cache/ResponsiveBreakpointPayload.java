package com.cloudinary.cache;

public class ResponsiveBreakpointPayload {

    private final int[] breakpoints;

    public ResponsiveBreakpointPayload(int[] breakpoints) {
        this.breakpoints = breakpoints;
    }

    public int[] getBreakpoints() {
        return breakpoints;
    }
}
