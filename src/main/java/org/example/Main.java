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
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;
    private List<Cube> cubes = new ArrayList<Cube>();
    private Cube currentCube;

    private boolean rotating = false;

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
                    case GLFW_KEY_UP -> currentCube.y +=0.05f;
                    case GLFW_KEY_DOWN -> currentCube.y -=0.05f;
                    case GLFW_KEY_LEFT -> currentCube.x -=0.05f;
                    case GLFW_KEY_RIGHT -> currentCube.x += 0.05f;
                    case GLFW_KEY_W -> currentCube.z += 0.05f;
                    case GLFW_KEY_S -> currentCube.z -= 0.05f;
                    case GLFW_KEY_R -> rotating = !rotating;

                    case GLFW_KEY_ENTER -> {
                        cubes.add(currentCube);
                        currentCube = new Cube(0,0,-5,0);
                    }
                }
            }

            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        currentCube = new Cube(0, 0, -5, 0);

        for (int row = 0; row < 6; row++) {
            float z = -4.1f - row * 0.3f;
            for (int i = 0; i < 25; i++) {
                float x = -3.4f + i * 0.3f ;
                cubes.add(new Cube(x, -1.7f, z, 0));
            }
        }

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

    private void gradientDj(){
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 0, 600, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDepthMask(false);

        glBegin(GL_QUADS);

        glColor3f(0.0f, 0.3f, 0.7f);
        glVertex2f(0, 0);
        glVertex2f(800, 0);

        glColor3f(0.0f, 0.7f, 1.0f);
        glVertex2f(800, 600);
        glVertex2f(0, 600);

        glEnd();

        glDepthMask(true);
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
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

            gradientDj();

            for (Cube cube : cubes) {
                drawCube(cube);
            }

            drawCube(currentCube);
            currentCube.angle += 0.5f;

            if (rotating){
                currentCube.angle += 0.5f;
            }

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void drawCube(Cube cube) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(cube.x, cube.y, cube.z);
        glRotatef(cube.angle, 0f, 1f, 0f);
        glScalef(0.33f, 0.33f, 0.33f);


        glBegin(GL_QUADS);
        //front
        glColor3f(125/255f, 218/255f, 88/255f);
        glVertex3f(-0.5f,-0.5f, 0.5f);
        glVertex3f(0.5f,-0.5f, 0.5f);
        glVertex3f(0.5f,0.5f, 0.5f);
        glVertex3f(-0.5f,0.5f, 0.5f);

        //back
        glColor3f(125/255f, 218/255f, 88/255f);
        glVertex3f(-0.5f,-0.5f, -0.5f);
        glVertex3f(-0.5f,0.5f, -0.5f);
        glVertex3f(0.5f,0.5f, -0.5f);
        glVertex3f(0.5f,-0.5f, -0.5f);

        //top face
        glColor3f(92/255f, 173/255f, 60/255f);
        glVertex3f(-0.05f,0.05f, -0.05f);
        glVertex3f(-0.05f,0.05f, 0.05f);
        glVertex3f(0.05f,0.05f, 0.05f);
        glVertex3f(0.05f,0.05f, -0.05f);

        //bottom face
        glColor3f(92/255f, 173/255f, 60/255f);
        glVertex3f(-0.5f,-0.5f, -0.5f);
        glVertex3f(0.5f,-0.5f, -0.5f);
        glVertex3f(0.5f,-0.5f, 0.5f);
        glVertex3f(-0.5f,-0.5f, 0.5f);

        //right face
        glColor3f(142/255f, 235/255f, 105/255f);
        glVertex3f(0.5f,-0.5f, -0.5f);
        glVertex3f(0.5f,0.5f, -0.5f);
        glVertex3f(0.5f,0.5f, 0.5f);
        glVertex3f(0.5f,-0.5f, 0.5f);

        //left face
        glColor3f(142/255f, 235/255f, 105/255f);
        glVertex3f(-0.5f,-0.5f, -0.5f);
        glVertex3f(-0.5f,-0.5f, 0.5f);
        glVertex3f(-0.5f,0.5f, 0.5f);
        glVertex3f(-0.5f,0.5f, -0.5f);
        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}