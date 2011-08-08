package com.voilaweb.mobile.cfranimated;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.AndroidCharacter;
import android.view.View;
import android.widget.*;


public class Demo extends Activity
{
    protected DemoAnim mDemoAnim;
    protected Button mRedBouncyBallButton;
    protected Button mBallsButton;
    protected Button mRotateButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDemoAnim = (DemoAnim)findViewById(R.id.anim_view);
        mRedBouncyBallButton = (Button)findViewById(R.id.redbouncyballbutton);
        mRedBouncyBallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mDemoAnim
                        .setSelectedAnimation(DemoAnim.ANIMATION_REDBOUNCYBALL)
                        .reset()
                        .runAnimation()
                        .waitForAnimationComplete();
            }
        });
        mBallsButton = (Button)findViewById(R.id.ballsbutton);
        mBallsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mDemoAnim
                    .setSelectedAnimation(DemoAnim.ANIMATION_BALLS)
                    .reset()
                    .runAnimation()
                    .waitForAnimationComplete();
            }
        });
        mRotateButton = (Button)findViewById(R.id.rotatebutton);
        mRotateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mDemoAnim
                    .setSelectedAnimation(DemoAnim.ANIMATION_ROTATE)
                    .reset()
                    .runAnimation()
                    .waitForAnimationComplete();
                for(int i=0; i<8; i++) {
                    mDemoAnim
                        .setSelectedAnimation(DemoAnim.ANIMATION_ROTATE2)
                        .reset()
                        .runAnimation()
                        .waitForAnimationComplete();
                }
            }
        });

        // We force the realization of our component in the layout file so when we need it
        // surfaceview will be available.

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("This application's purpose is to demo the CFRAnimation library in action. Nothing really eye-popping to see here; the three animations that can be triggered by pushing the three numbered buttons are time-constrained and use Canvas.")
                .setPositiveButton("OK", null)
                .show();
    }
}
