package io.devbobcorn.toy.shaders;

import io.devbobcorn.toy.Renderer;
import io.devbobcorn.toy.Window;
import org.lwjgl.Version;

import static org.lwjgl.glfw.GLFW.*;

public class ShaderToySim {

    private Window window = null;
    private Thread renderThread = null;

    public void run() {
        init();
        loop();
    }

    private void init() {

        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        var opts = new Window.WindowOptions();
        opts.width = 854;
        opts.height = 480;
        opts.useUiScale = false;

        window = new Window("喵呜机", opts, () -> {
            //resize();
            return null;
        });

        Renderer renderer = new Renderer(window);

        renderThread = new Thread(renderer);
        renderThread.start();
    }

    private void loop() {

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !window.windowShouldClose() ) {
            window.pollEvents();
        }

        glfwWaitEvents();

        // Wait for render thread to stop
        try {
            renderThread.join();
        } catch (InterruptedException ignored) {

        } finally {
            System.out.println("Cleaning up...");

            // Clean up the window
            window.cleanup();
        }
    }

    public static void main(String[] args) {
        new ShaderToySim().run();
    }

}
