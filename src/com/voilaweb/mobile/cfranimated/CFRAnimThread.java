package com.voilaweb.mobile.cfranimated;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

abstract public class CFRAnimThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean isRunning;
        private CFRAnimated mSurface;
        private long mPreviousTime = 0L;
        private long mStartTime = 0L;
        private long mCompletionTime = 0L;

        public CFRAnimThread(SurfaceHolder surfaceHolder, CFRAnimated surface) {
            this.surfaceHolder = surfaceHolder;
            this.mSurface = surface;
            isRunning = false;
        }

        public void setRunning(boolean run) {
            isRunning = run;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void run() {
            Canvas c;
            while (isRunning) {
                // Wait until surface us realized
                if(mSurface.mSurfaceExists == false) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Ahem.
                        return;
                    }
                    continue;
                }

                c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        doDraw_(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
            //
            mSurface.reset_();
        }

        private void doDraw_(Canvas canvas) {
            if(mSurface.getAnimationState() == CFRAnimated.ANIM_WAIT) return;
            // Redundant since we ask the thread to die:
            // if(mSurface.isAnimationComplete()) return;
            long now = System.currentTimeMillis();
            long elapsed = (now - mPreviousTime);
            // Pacer
            if(elapsed < mSurface.mPacer) return;
            // Lazily set start time
            if(mSurface.mMaxDuration > 0) {
                if(mStartTime == 0L) {
                    mStartTime = now;
                    mCompletionTime = mStartTime + mSurface.mMaxDuration;
                }
            }

            long totalElapsed = (now - mStartTime);

            // Stepper
            if(!mSurface.isAnimationComplete()) {
                // If a max duration was specified, we wish to dynamically adapt our stepper
                // to help us reach our deadline.
                if(mSurface.mSteppingDown) {
                    mSurface.mStepped -= mSurface.mStepperValue;
                    if(mSurface.mStepped < mSurface.mStepperEnd) {
                        mSurface.mStepped = mSurface.mStepperEnd;
                    }
                }
                else {
                    mSurface.mStepped += mSurface.mStepperValue;
                    if(mSurface.mStepped > mSurface.mStepperEnd) {
                        mSurface.mStepped = mSurface.mStepperEnd;
                    }
                }
            }

            mSurface.mBeats ++;

            // Adjust velocity?
            if(mSurface.mMaxDuration > 0 && totalElapsed > 0) {
                float elapsedRatio = (float)mSurface.mMaxDuration / (float)totalElapsed;
                float beatsRatio = (float)mSurface.mPotentialBeats / (float)mSurface.mBeats;
                if(beatsRatio > elapsedRatio) {
// Not needed so far                        long remainingTime = mSurface.mMaxDuration - totalElapsed;
                    mSurface.mPotentialBeats = (int)(elapsedRatio * mSurface.mBeats);
                    if(mSurface.mPotentialBeats > mSurface.mBeats) {
                        mSurface.mStepperValue =
                                Math.abs(
                                    (mSurface.mStepperEnd - mSurface.mStepped) /
                                    (mSurface.mPotentialBeats - mSurface.mBeats));
                    }
                    else if(mSurface.mPotentialBeats < mSurface.mBeats) {
                        // Oh it's already too late!
                        mSurface.mStepperValue = Math.abs(mSurface.mStepperEnd - mSurface.mStepped);
                    }
                }
            }

            mPreviousTime = System.currentTimeMillis();
            doDraw(canvas);
        }

        abstract protected void doDraw(Canvas canvas);
}
