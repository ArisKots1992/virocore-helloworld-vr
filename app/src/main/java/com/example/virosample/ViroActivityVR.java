/*
 * Copyright (c) 2017-present, Viro, Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.virosample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.viro.core.AmbientLight;
import com.viro.core.AnimationTimingFunction;
import com.viro.core.AnimationTransaction;
import com.viro.core.AsyncObject3DListener;
import com.viro.core.Box;
import com.viro.core.ClickListener;
import com.viro.core.ClickState;
import com.viro.core.DragListener;
import com.viro.core.GesturePinchListener;
import com.viro.core.GestureRotateListener;
import com.viro.core.Node;

import com.viro.core.Object3D;
import com.viro.core.PhysicsBody;
import com.viro.core.PhysicsShapeAutoCompound;
import com.viro.core.PhysicsShapeBox;
import com.viro.core.PinchState;
import com.viro.core.Quaternion;
import com.viro.core.RotateState;
import com.viro.core.Scene;
import com.viro.core.Sphere;
import com.viro.core.Text;
import com.viro.core.Texture;
import com.viro.core.TouchpadScrollListener;
import com.viro.core.Vector;
import com.viro.core.ViroView;
import com.viro.core.ViroViewGVR;
import com.viro.core.ViroViewOVR;

import java.io.IOException;
import java.io.InputStream;

/**
 * A sample Android activity for creating GVR and/or OVR stereoscopic scenes.
 * <p>
 * This activity automatically handles the creation of the VR renderer based on the currently
 * selected build variant in Android Studio.
 * <p>
 * Extend and override onRendererStart() to start building your 3D scenes.
 */
public class ViroActivityVR extends Activity {

    private static final String TAG = "KOTS!";
    private ViroView mViroView;
    private AssetManager mAssetManager;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.VIRO_PLATFORM.equalsIgnoreCase("GVR")) {
            mViroView = createGVRView();
        } else if (BuildConfig.VIRO_PLATFORM.equalsIgnoreCase("OVR")) {
            mViroView = createOVRView();
        }
        setContentView(mViroView);
    }

    private ViroView createGVRView() {
        ViroViewGVR viroView = new ViroViewGVR(this, new ViroViewGVR.StartupListener() {
            @Override
            public void onSuccess() {
                onRendererStart();
            }

            @Override
            public void onFailure(ViroViewGVR.StartupError error, String errorMessage) {
                onRendererFailed(error.toString(), errorMessage);
            }
        }, new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "On GVR userRequested exit");
            }
        });

        viroView.setVRModeEnabled(false);
        return viroView;
    }

    private ViroView createOVRView() {
        ViroViewOVR viroView = new ViroViewOVR(this, new ViroViewOVR.StartupListener() {
            @Override
            public void onSuccess() {
                onRendererStart();
            }

            @Override
            public void onFailure(ViroViewOVR.StartupError error, String errorMessage) {
                onRendererFailed(error.toString(), errorMessage);
            }
        });
        return viroView;
    }

    private void onRendererStart() {
        // Create your scene here. We provide a simple Hello World scene as an example
        Log.e(TAG, "createHelloWorldScene");
        createHelloWorldScene();
    }

    public void onRendererFailed(String error, String errorMessage) {
        // Fail as you wish!
    }

    private void createHelloWorldScene() {
        // Create a new Scene and get its root Node
        Scene scene = new Scene();
        Node rootNode = scene.getRootNode();

        // Load the background image into a Bitmap file from assets
        Bitmap backgroundBitmap = bitmapFromAsset("vr.png");

        // Add a 360 Background Texture if we were able to find the Bitmap
        if (backgroundBitmap != null) {
            Texture backgroundTexture = new Texture(backgroundBitmap, Texture.Format.RGBA8, true, true);
            scene.setBackgroundTexture(backgroundTexture);
        }

//        Bitmap right = bitmapFromAsset("right.png");
//        Texture texture = new Texture(bitmapFromAsset("right.png"),
//                bitmapFromAsset("left.png"),
//                bitmapFromAsset("top.png"),
//                bitmapFromAsset("bot.png"),
//                bitmapFromAsset("back.png"),
//                bitmapFromAsset("front.png"),
//                                        Texture.Format.RGBA8);
//        scene.setBackgroundCubeTexture(texture);

        // Create a Text geometry
        Text helloWorldText = new Text.TextBuilder().viroContext(mViroView.getViroContext()).
                textString("Hello World").fontSize(400).
                color(Color.WHITE).
                width(100).height(50).
                horizontalAlignment(Text.HorizontalAlignment.CENTER).
                verticalAlignment(Text.VerticalAlignment.CENTER).
                lineBreakMode(Text.LineBreakMode.NONE).
                clipMode(Text.ClipMode.CLIP_TO_BOUNDS).
                maxLines(1).build();

        // Create a Node, position it, and attach the Text geometry to it
        Node textNode = new Node();
        textNode.setPosition(new Vector(0, 0, -10));
        textNode.setGeometry(helloWorldText);

        // Attach the textNode to the Scene's rootNode.
        rootNode.addChildNode(textNode);
        mViroView.setScene(scene);

        ///////////////////
        final Object3D heart = new Object3D();
        heart.loadModel(mViroView.getViroContext(),Uri.parse("file:///android_asset/island_rescaled.obj"), Object3D.Type.OBJ, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {

                heart.setPosition(new Vector(0,0,10));
                heart.setScale(new Vector(0.2,0.2,0.2));
                heart.setTag("ARIS");
            }

            @Override
            public void onObject3DFailed(String s) {

            }
        });
        AmbientLight ambient = new AmbientLight(Color.WHITE, 1000.0f);
        scene.getRootNode().addLight(ambient);
        scene.getRootNode().addChildNode(heart);

        /////
        heart.setClickListener(new ClickListener() {
            @Override
            public void onClick(int i, Node node, Vector vector) {
                Log.i(TAG,"Clicked: " + node.getTag());
            }
            @Override
            public void onClickState(int i, Node node, ClickState clickState, Vector vector) {
            }
        });

//
        heart.setDragListener(new DragListener() {
            @Override
            public void onDrag(int i, Node node, Vector vector, Vector vector1) {
            }
        });
//        heart.initPhysicsBody(PhysicsBody.RigidBodyType.DYNAMIC, 1, new PhysicsShapeAutoCompound());

        AnimationTransaction.begin();
        AnimationTransaction.setAnimationDuration(5000);
//        AnimationTransaction.setListener(new AnimationTransaction.Listener() {
//            @Override
//            public void onFinish(AnimationTransaction animationTransaction) {
//            }
//
//        });
        AnimationTransaction.setAnimationLoop(true);
        heart.setRotation(new Vector(0, 1, 0));
        AnimationTransaction.commit();

//        Box box = new Box(1, 1, 1);
//        heart.setGeometry(box);
//        heart.initPhysicsBody(PhysicsBody.RigidBodyType.DYNAMIC, 1, new PhysicsShapeAutoCompound());
//        heart.setGestureRotateListener(new GestureRotateListener() {
//            @Override
//            public void onRotate(int i, Node node, float v, RotateState rotateState) {
//            }
//        });

//        heart.setGesturePinchListener(new GesturePinchListener() {
//            @Override
//            public void onPinch(int i, Node node, float v, PinchState pinchState) {
//                node.setScale(new Vector(v,v,v));
//            }
//        });

        /////////////
        Node sun = new Node();
        sun.setPosition(new Vector(10, 0, 5));
        sun.setGeometry(new Sphere(4));

        Node earth = new Node();
        earth.setPosition(new Vector(20, 20, 20));
        earth.setGeometry(new Sphere(1));
        sun.addChildNode(earth);

        Node moon = new Node();
        moon.setPosition(new Vector(10, 0, 0));
        moon.setGeometry(new Sphere(0.2f));
        earth.addChildNode(moon);

        scene.getRootNode().addChildNode(sun);

        AnimationTransaction.begin();
        AnimationTransaction.setAnimationDuration(5000);
//        earth.setRotationPivot(sun.getPositionRealtime());
//        earth.setRotation(new Vector(0, 10, 0));
        AnimationTransaction.commit();
    }

    private Bitmap bitmapFromAsset(String assetName) {
        if (mAssetManager == null) {
            mAssetManager = getResources().getAssets();
        }

        InputStream imageStream;
        try {
            imageStream = mAssetManager.open(assetName);
        } catch (IOException exception) {
            Log.w(TAG, "Unable to find image ["+assetName+"] in assets!");
            return null;
        }
        return BitmapFactory.decodeStream(imageStream);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }
}