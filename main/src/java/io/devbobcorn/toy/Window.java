package io.devbobcorn.toy;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final long windowId;
    private int height;
    private final Callable<Void> resizeFunc;
    private int width;

    public Window(String title, WindowOptions opts, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (opts.compatibleProfile) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        if (opts.useUiScale) {
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GL_TRUE);
        }

        if (opts.width > 0 && opts.height > 0) {
            this.width = opts.width;
            this.height = opts.height;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = vidMode.width();
            height = vidMode.height();
        }

        windowId = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowId == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowId, (window, w, h) -> resized(w, h));

        glfwSetErrorCallback((int errorCode, long msgPtr) ->
                System.err.printf("Error code [{}], msg [{}]%n", errorCode, MemoryUtil.memUTF8(msgPtr))
        );

        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });

        glfwMakeContextCurrent(windowId);

        if (opts.fps > 0) {
            glfwSwapInterval(0);
        } else {
            glfwSwapInterval(1);
        }

        glfwShowWindow(windowId);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowId, arrWidth, arrHeight);
        width = arrWidth[0];
        height = arrHeight[0];
    }

    public void cleanup() {
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public long getWindowId() {
        return windowId;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowId, keyCode) == GLFW_PRESS;
    }

    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(windowId, true); // We will detect this in the rendering loop
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    protected void resized(int width, int height) {
        this.width = width;
        this.height = height;
        try {
            resizeFunc.call();
        } catch (Exception excp) {
            System.err.println("Error calling resize callback:" + excp);
        }
    }

    public void update() {
        glfwSwapBuffers(windowId);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowId);
    }

    public static class WindowOptions {
        public boolean compatibleProfile;
        public int fps;
        public int height;
        //public int ups = Engine.TARGET_UPS;
        public int width;
        public boolean useUiScale;
    }
}