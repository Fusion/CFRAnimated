package com.voilaweb.mobile.cfranimated;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

abstract public class CFRAnimated extends SurfaceView implements SurfaceHolder.Callback {
    public static final int ANIM_WAIT = 0;
    public static final int ANIM_READY_TO_DRAW = 1;
    public static final int ANIM_DRAWING = 2;

    private CFRAnimThread drawThread;
    protected int   mWidth;
    protected int   mHeight;
    protected int   mGapLeft;
    protected int   mGapRight;
    protected int   mGapTop;
    protected int   mGapBottom;

    protected volatile boolean mSurfaceExists = false;

    protected int   mState = ANIM_WAIT;
    protected boolean mAnimationComplete = false;
    protected int   mPacer = 0;
    protected int   mSelectedAnimation = 0;
    protected boolean mIntermediateAnimation = false;

    protected int mClipperOffset = 0;
    protected RectF mClipper = null;

    // Value where to start stepper
    protected float mStepperStart   = 0.0f;
    // Stepper's goal value
    protected float  mStepperEnd    = 100.0f;
    // Stepper increment
    protected float mStepperValue   = 10.0f;
    // Stepping direction (+ or -?)
    protected boolean mSteppingDown = false;
    // Stepper max duration
    protected long mMaxDuration     = 0L;

    // When was anim invoked last
    protected long mAnimationStepped = 0L;
    //
    protected float mStepped         = 0.0f;
    protected int mBeats             = 0;
    protected int mPotentialBeats    = 0;

    public CFRAnimated(Context context, AttributeSet as) {
        super(context, as);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public CFRAnimated(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    protected CFRAnimated reset_() {
        mStepperStart     = 0;
        mStepperEnd       = 100;
        mStepperValue     = 10;
        mSteppingDown     = false;
        mMaxDuration      = 0L;
        mAnimationStepped = 0L;
        mStepped          = 0.0f;
        mBeats            = 0;
        mPotentialBeats   = (int)((mStepperEnd - mStepperStart) / mStepperValue);
        return this;
    }

    public CFRAnimated reset() {
        drawThread.setRunning(true);
        drawThread.start();
        return this;
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceExists = true;
//        if(null != drawThread) {
//            drawThread.setRunning(true);
//            drawThread.start();
//        }
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(null != drawThread) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
            drawThread = null;
        }
    }

    abstract CFRAnimThread getAnimThread(SurfaceHolder holder, CFRAnimated surface);

    public CFRAnimated runAnimation() {
        /* This code is suspect
        if(!drawThread.isRunning()) {
            drawThread.setRunning(true);
            drawThread.start();
        }
        */
        setAnimationState(ANIM_READY_TO_DRAW);
        return this;
    }

    public CFRAnimated waitForAnimationComplete() {
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
        drawThread = null;
        //
        return this;
    }

    public CFRAnimated setStepperStart(float stepperStart) {
        mStepperStart = stepperStart;
        mStepped      = stepperStart;
        return this;
    }

    public float getStepperStart() {
        return mStepperStart;
    }

    public CFRAnimated setStepperEnd(float stepperEnd) {
        mStepperEnd = stepperEnd;
        return this;
    }

    public float getStepperEnd() {
        return mStepperEnd;
    }

    /*
     * As well as specifying beats impact value,
     * specifying value impacts beats.
     */
    public CFRAnimated setStepperValue(float stepperValue) {
        mStepperValue   = stepperValue;
        mPotentialBeats = Math.abs((int)((mStepperEnd - mStepperStart) / mStepperValue));
        return this;
    }

    public float getStepperValue() {
        return mStepperValue;
    }

    public int getPotentialBeats() {
        return mPotentialBeats;
    }

    public CFRAnimated setSteppingDown(boolean steppingDown) {
        mSteppingDown = steppingDown;
        return this;
    }


    public boolean isSteppingDown() {
        return mSteppingDown;
    }

    public CFRAnimated setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration;
        return this;
    }

    public long getMaxDuration() {
        return mMaxDuration;
    }


    public float getStepped() {
        return mStepped;
    }


    public CFRAnimated setAnimationState(int State) {
    	mState = State;
        return this;
    }

    public int getAnimationState() {
        return mState;
    }

    public CFRAnimated setAnimationComplete(boolean animationComplete) {
        if(animationComplete) {
            drawThread.setRunning(false);
        }
        return this;
    }

    public boolean isAnimationComplete() {
        return mAnimationComplete;
    }

    public CFRAnimated setIntermediateAnimation(boolean intermediateAnimation) {
        mIntermediateAnimation = intermediateAnimation;
        return this;
    }

    public boolean isIntermediateAnimation() {
        return mIntermediateAnimation;
    }

    public CFRAnimated setSelectedAnimation(int selectedAnimation) {
        mSelectedAnimation = selectedAnimation;
        drawThread = getAnimThread(getHolder(), this);
        return this;
    }

    public int getSelectedAnimation() {
        return mSelectedAnimation;
    }
}
