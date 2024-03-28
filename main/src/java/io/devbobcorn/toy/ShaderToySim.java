package io.devbobcorn.toy;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class ShaderToySim {

    private Window window;
    private float lastRenderTime = 0f;

    private ShaderProgram shaderProgram = null;
    private QuadMesh quadMesh = null;

    public void run() {
        init();
        loop();
    }

    public void prepareShader(String shaderToyCode) {
        try {
            shaderProgram = new ShaderProgram();
            shaderProgram.createVertexShader(Utils.readFile("shaders/wrapper.vert"));
            var fragShader = Utils.readFile("shaders/wrapper.frag")
                    .replace("[SHADERTOY_CODE]", shaderToyCode);
            shaderProgram.createFragmentShader(fragShader);
            shaderProgram.link();
        } catch (RuntimeException e) {
            System.err.println("Failed to create shader: " + e.getMessage());
            // Reset shader program
            shaderProgram = null;
        }
    }

    public void prepareMesh() {
        quadMesh = new QuadMesh(1.0f);
    }

    public void render() {
        glViewport(0, 0, window.getWidth(), window.getHeight());

        if (shaderProgram != null) {
            shaderProgram.bind();

            int uniformLoc;

            // Setup ShaderToy inputs
            uniformLoc = GL20.glGetUniformLocation(shaderProgram.getId(), "iResolution");
            GL20.glUniform3f(uniformLoc, window.getWidth(), window.getHeight(), 1f);
            var renderTime = (float) GLFW.glfwGetTime();
            // float [ iTime ] ====================================================================
            uniformLoc = GL20.glGetUniformLocation(shaderProgram.getId(), "iTime");
            GL20.glUniform1f(uniformLoc, renderTime);
            // float [ iTimeDelta ] ===============================================================
            uniformLoc = GL20.glGetUniformLocation(shaderProgram.getId(), "iTimeDelta");
            GL20.glUniform1f(uniformLoc, renderTime - lastRenderTime);
            lastRenderTime = renderTime;


            // Render the quad
            glBindVertexArray(quadMesh.getVaoId());
            glDrawElements(GL_TRIANGLES, quadMesh.getIndexCount(), GL_UNSIGNED_INT, 0);
        }
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
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Load shader
        var shaderToyCode = Utils.readFile("shaders/shadertoy_matrix.glsl");
        prepareShader(shaderToyCode);

        // Create mesh
        prepareMesh();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !window.windowShouldClose() ) {
            window.pollEvents();

            // Do the rendering
            render();

            window.update();
        }

        // Exit logic
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        if (quadMesh != null) {
            quadMesh.cleanup();
        }

        window.cleanup();
    }

    public static void main(String[] args) {
        new ShaderToySim().run();
    }

}
