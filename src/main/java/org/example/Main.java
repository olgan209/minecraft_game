package org.example;

import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;
    private float posX = 0.0f, posY = 0.0f, posZ = -5.0f;
    private float angle = 0.0f;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(900, 700, "Minecraft", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if( action == GLFW_PRESS || action == GLFW_REPEAT ){
                switch (key) {
                    case GLFW_KEY_UP -> posY +=0.05f;
                    case GLFW_KEY_DOWN -> posY -=0.05f;
                    case GLFW_KEY_LEFT -> posX -=0.05f;
                    case GLFW_KEY_RIGHT -> posX += 0.05f;

                    case GLFW_KEY_W -> posZ += 0.05f;
                    case GLFW_KEY_S -> posZ -= 0.05f;
                }
            }

            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0) ,
                900.0f / 700.0f, 0.1f, 100f);
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        projection.get(fb);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glTranslatef(posX, posY, posZ);
            glRotatef(angle, 0f, 1f, 0f);
            glScalef(0.33f, 0.33f, 0.33f);


            glBegin(GL_QUADS);
            //front
            glColor3f(1.0f, 0.0f, 0.0f);
            glVertex3f(-0.5f,-0.5f, 0.5f);
            glVertex3f(0.5f,-0.5f, 0.5f);
            glVertex3f(0.5f,0.5f, 0.5f);
            glVertex3f(-0.5f,0.5f, 0.5f);

            //back
            glColor3f(0.0f, 1.0f, 0.0f);
            glVertex3f(-0.5f,-0.5f, -0.5f);
            glVertex3f(-0.5f,0.5f, -0.5f);
            glVertex3f(0.5f,0.5f, -0.5f);
            glVertex3f(0.5f,-0.5f, -0.5f);

            //top face
            glColor3f(0.0f, 0.0f, 1.0f);
            glVertex3f(-0.05f,0.05f, -0.05f);
            glVertex3f(-0.05f,0.05f, 0.05f);
            glVertex3f(0.05f,0.05f, 0.05f);
            glVertex3f(0.05f,0.05f, -0.05f);

            //bottom face
            glColor3f(1.0f, 1.0f, 0.0f);
            glVertex3f(-0.5f,-0.5f, -0.5f);
            glVertex3f(0.5f,-0.5f, -0.5f);
            glVertex3f(0.5f,-0.5f, 0.5f);
            glVertex3f(-0.5f,-0.5f, 0.5f);

            //right face
            glColor3f(1.0f, 0.0f, 1.0f);
            glVertex3f(0.5f,-0.5f, -0.5f);
            glVertex3f(0.5f,0.5f, -0.5f);
            glVertex3f(0.5f,0.5f, 0.5f);
            glVertex3f(0.5f,-0.5f, 0.5f);

            //left face
            glColor3f(0.0f, 1.0f, 1.0f);
            glVertex3f(-0.5f,-0.5f, -0.5f);
            glVertex3f(-0.5f,-0.5f, 0.5f);
            glVertex3f(-0.5f,0.5f, 0.5f);
            glVertex3f(-0.5f,0.5f, -0.5f);
            glEnd();

            angle += 0.5f;

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

}