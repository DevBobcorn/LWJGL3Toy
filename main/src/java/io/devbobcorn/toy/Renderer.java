package io.devbobcorn.toy;

import io.devbobcorn.toy.shaders.ShaderProgram;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class Renderer implements Runnable {

    private final Window window;
    private float lastRenderTime = 0f;

    private ShaderProgram shaderProgram = null;
    private QuadMesh quadMesh = null;

    public Renderer(Window window) {
        this.window = window;
    }

    public void prepareShader(String shaderToyCode) {
        try {
            shaderProgram = new ShaderProgram();
            shaderProgram.createVertexShader(Utils.resourceToString("shaders/wrapper.vert"));
            var fragShader = Utils.resourceToString("shaders/wrapper.frag")
                    .replace("[SHADERTOY_CODE]", shaderToyCode);
            shaderProgram.createFragmentShader(fragShader);
            shaderProgram.link();
        } catch (IOException e) {
            System.err.println("Failed to read shader source: " + e.getMessage());
            // Reset shader program
            shaderProgram = null;
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

        window.update();
    }

    @Override
    public void run() {
        System.out.println("Renderer starting...");

        // Setup GL context, on this render thread
        glfwMakeContextCurrent(window.getWindowId());

        // Update swap interval
        glfwSwapInterval(1);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Load shader
        String shaderToyCode;

        try {
            shaderToyCode = Utils.resourceToString("shaders/shadertoy_matrix.glsl");

            prepareShader(shaderToyCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create mesh
        prepareMesh();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !window.windowShouldClose() ) {
            // Do the rendering
            render();
        }

        // Exit logic
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        if (quadMesh != null) {
            quadMesh.cleanup();
        }

        System.out.println("Renderer stopping...");
    }
}
