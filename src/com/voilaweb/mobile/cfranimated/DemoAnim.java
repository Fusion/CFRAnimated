package com.voilaweb.mobile.cfranimated;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Vector;

public class DemoAnim extends CFRAnimated {

        public final static int ANIMATION_REDBOUNCYBALL   = 1;
        public final static int ANIMATION_BALLS           = 2;
        public final static int ANIMATION_ROTATE          = 3;
        public final static int ANIMATION_ROTATE2         = 4;

        protected final String[] exampleStr = {
            "Example #1: A red bouncy ball goes wild for less than 5 seconds...",
            "Example #2: Balls fall and bounce back for up to 5 seconds, even on slow devices, thanks to a very high velocity goal that will keep them accelerating if necessary...",
            "Example #3: These guys need to perform a full revolution in about 2 seconds, at which point we will see a ring color wave several times, each time under 1 second."
        };

        protected Float[] rotateData = { 10f, 20f, 30f, 40f, 50f };
        protected int[] rotateColors = { Color.RED, Color.BLUE, Color.GREEN, Color.GRAY, Color.YELLOW };
        protected int[] rotateWhite  = { Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
        protected float rotateTotal = 150f;

        protected Dim mDim = null;

        private final Paint mBgPaints = new Paint();
        private final Paint mLinePaints = new Paint();
        private final Paint mMaskPaints = new Paint();

        protected int x = 0;
        protected int y = 0;
        protected int dx = 10;
        protected int dy = 10;
        protected int mContainerWidth  = 0;
        protected int mContainerHeight = 0;
        protected boolean mLandscape   = false;
        protected int mTopOffset       = 0;

        public DemoAnim(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            // Or get this value from the layout file as attr.
            mTopOffset = 0;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mLandscape = false;
            int containerWidth  = w;
            int containerHeight = h - mTopOffset;
            int orientation = getResources().getConfiguration().orientation;
            if(Configuration.ORIENTATION_UNDEFINED == orientation) {
                mLandscape = (containerHeight < containerWidth);
            }
            else {
                mLandscape = (Configuration.ORIENTATION_LANDSCAPE == orientation);
            }
            mContainerHeight = containerHeight;
            mContainerWidth  = containerWidth;

            int size= Math.min(mContainerHeight, mContainerWidth);
            mDim = new Dim(size);
        }

        @Override
        CFRAnimThread getAnimThread(SurfaceHolder holder, CFRAnimated surface) {
            switch(getSelectedAnimation()) {
                case ANIMATION_REDBOUNCYBALL: {
                    final int BALL_WIDTH = 20;
                    final int HALF_BALL_WIDTH = BALL_WIDTH / 2;
                    final long startedAt = System.currentTimeMillis();
                    x = 0;
                    y = 0;

                    setStepperStart(0)
                    .setStepperEnd(10000)
                    .setMaxDuration(5000)
                    .setStepperValue(0.01f);
                    return new CFRAnimThread(holder, surface) {
                        @Override
                        protected void doDraw(Canvas canvas) {
                            x += dx;
                            y += dy;
                            if(x < 0) {
                                x = 0;
                                dx = -dx;
                            }
                            if(x + BALL_WIDTH >= mContainerWidth) {
                                x = mContainerWidth - BALL_WIDTH;
                                dx = -dx;
                            }
                            if(y < 0) {
                                y = 0;
                                dy = -dy;
                            }
                            if(y + BALL_WIDTH >= mContainerHeight) {
                                y = mContainerHeight - BALL_WIDTH;
                                dy = -dy;
                            }

                            canvas.drawColor(0xFF000000);
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setStyle(Paint.Style.FILL);

                            long elapsed = System.currentTimeMillis() - startedAt;
                            renderCounter(canvas, paint, elapsed);
                            renderBottomText(canvas, exampleStr[0]);

                            paint.setColor(0xFFFF0000);
                            canvas.drawCircle(x + HALF_BALL_WIDTH, y + HALF_BALL_WIDTH, BALL_WIDTH, paint);
                            if(getStepped() < getStepperEnd()) {
                                setAnimationState(ANIM_DRAWING);
                            }
                            else {
                                setAnimationComplete(true);
                            }
                        }
                    };
                }
                case ANIMATION_BALLS: {
                    final long startedAt = System.currentTimeMillis();

                    final Vector<AnimatedPBall> balls = new Vector<AnimatedPBall>();
                    balls.add(new AnimatedPBall(Color.RED, 10, 0));
                    balls.add(new AnimatedPBall(Color.YELLOW, 60, 0));
                    balls.add(new AnimatedPBall(Color.BLUE, 110, 0));
                    balls.add(new AnimatedPBall(Color.GREEN, 160, 0));

                    /*
                     * Set a high value for stepper's end value, so that we point towards max
                     * acceleration within 5 seconds...a way to make sure that we have
                     * gone through all the bouncing.
                     */
                    setStepperStart(0)
                    .setStepperEnd(50000)
                    .setMaxDuration(5000)
                    .setStepperValue(10);
                    return new CFRAnimThread(holder, surface) {
                        @Override
                        protected void doDraw(Canvas canvas) {
                            canvas.drawColor(0xFF000000);
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setStyle(Paint.Style.FILL);

                            long elapsed = System.currentTimeMillis() - startedAt;
                            renderCounter(canvas, paint, elapsed);
                            renderBottomText(canvas, exampleStr[1]);

                            for(AnimatedPBall ball:balls) {
                                ball.animate(canvas, paint, elapsed);
                            }

                            if(getStepped() < getStepperEnd()) {
                                setAnimationState(ANIM_DRAWING);
                            }
                            else {
                                setAnimationComplete(true);
                            }
                        }
                    };
                }
                case ANIMATION_ROTATE: {
                    // Segments paint
                    mBgPaints.setAntiAlias(true);
                    mBgPaints.setStyle(Paint.Style.FILL);
                    mBgPaints.setColor(0xFF000000);
                    mBgPaints.setStrokeWidth(0.5f);
                    // Delimiters paint
                    mLinePaints.setAntiAlias(true);
                    mLinePaints.setStyle(Paint.Style.STROKE);
                    mLinePaints.setColor(0xFF000000);
                    mLinePaints.setStrokeWidth(0.5f);
                    // Inner black disc paint
                    mMaskPaints.setAntiAlias(true);
                    mMaskPaints.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    mMaskPaints.setColor(Color.TRANSPARENT);

                    final long startedAt = System.currentTimeMillis();

                    setStepperStart(0)
                    .setStepperEnd(360)
                    .setMaxDuration(2000)
                    .setStepperValue(16);
                    return new CFRAnimThread(holder, surface) {
                        @Override
                        protected void doDraw(Canvas canvas) {
                            canvas.drawColor(0xFF000000);

                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setStyle(Paint.Style.FILL);

                            long elapsed = System.currentTimeMillis() - startedAt;
                            renderCounter(canvas, paint, elapsed);
                            renderBottomText(canvas, exampleStr[2]);

                            int refScaledDiameter = mDim.scaledDiameter;
                            for(int i=0; i<9; i++) {
                                Bitmap ring = renderAComponent(
                                        mDim,
                                        i % 2 == 0 ?  (int) getStepped() : 360 - (int) getStepped(),
                                        rotateData,
                                        rotateTotal,
                                        rotateColors,
                                        getStepped() == getStepperEnd());
                                canvas.drawBitmap(
                                        ring,
                                        (refScaledDiameter - mDim.scaledDiameter) / 2,
                                        (refScaledDiameter - mDim.scaledDiameter) / 2,
                                        null);
                                ring.recycle();
                                mDim.scale = mDim.scale * 2 / 3;
                                mDim.reScale(Dim.Diameter, Dim.Gap, Dim.Thickness);
                            }
                            mDim.scale = mDim.refScale;
                            mDim.reScale(Dim.Diameter, Dim.Gap, Dim.Thickness);

                            if(getStepped() < getStepperEnd()) {
                                setAnimationState(ANIM_DRAWING);
                            }
                            else {
                                setAnimationComplete(true);
                            }
                        }
                    };
                }
                case ANIMATION_ROTATE2: {
                    // Segments paint
                    mBgPaints.setAntiAlias(true);
                    mBgPaints.setStyle(Paint.Style.FILL);
                    mBgPaints.setColor(0xFF000000);
                    mBgPaints.setStrokeWidth(0.5f);
                    // Delimiters paint
                    mLinePaints.setAntiAlias(true);
                    mLinePaints.setStyle(Paint.Style.STROKE);
                    mLinePaints.setColor(0xFF000000);
                    mLinePaints.setStrokeWidth(0.5f);
                    // Inner black disc paint
                    mMaskPaints.setAntiAlias(true);
                    mMaskPaints.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    mMaskPaints.setColor(Color.TRANSPARENT);

                    final long startedAt = System.currentTimeMillis();

                    setStepperStart(9)
                    .setStepperEnd(0)
                    .setSteppingDown(true)
                    .setMaxDuration(1000)
                    .setStepperValue(1);
                    return new CFRAnimThread(holder, surface) {
                        @Override
                        protected void doDraw(Canvas canvas) {
                            canvas.drawColor(0xFF000000);

                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setStyle(Paint.Style.FILL);

                            long elapsed = System.currentTimeMillis() - startedAt;
                            renderCounter(canvas, paint, elapsed);
                            renderBottomText(canvas, exampleStr[2]);

                            int refScaledDiameter = mDim.scaledDiameter;
                            for(int i=0; i<9; i++) {
                                Bitmap ring = renderAComponent(
                                        mDim,
                                        0,
                                        rotateData,
                                        rotateTotal,
                                        i == ((int)(getStepped())) % 9 && getStepped() > 0 ?  rotateWhite : rotateColors,
                                        getStepped() == getStepperEnd());
                                canvas.drawBitmap(
                                        ring,
                                        (refScaledDiameter - mDim.scaledDiameter) / 2,
                                        (refScaledDiameter - mDim.scaledDiameter) / 2,
                                        null);
                                ring.recycle();
                                mDim.scale = mDim.scale * 2 / 3;
                                mDim.reScale(Dim.Diameter, Dim.Gap, Dim.Thickness);
                            }
                            mDim.scale = mDim.refScale;
                            mDim.reScale(Dim.Diameter, Dim.Gap, Dim.Thickness);

                            if(getStepped() > getStepperEnd()) {
                                setAnimationState(ANIM_DRAWING);
                            }
                            else {
                                setAnimationComplete(true);
                            }
                        }
                    };
                }
            }
            throw new Resources.NotFoundException("Unknown animation requested.");
        }

        protected void renderCounter(Canvas canvas, Paint paint, long elapsed) {
            String text = Long.toString(elapsed / 1000) + '.' + Long.toString((elapsed % 1000) / 100) + 's';
            renderTopText(canvas, paint, text);
        }

        protected void renderTopText(Canvas canvas, Paint paint, String text) {
            paint.setColor(0xFF00FF44);
            canvas.drawText(text, 10, 10, paint);
        }

        protected void renderBottomText(Canvas canvas, String text) {
            TextPaint tp=new TextPaint();
            tp.setAntiAlias(true);
            tp.setColor(0xFFFFFFFF);
            tp.setTextSize(14);
            canvas.save();
            canvas.translate(10, mContainerHeight - 96);
            StaticLayout sl = new StaticLayout(text, tp, mContainerWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
            sl.draw(canvas);
            canvas.restore();
            //canvas.drawText(text, 10, mContainerHeight - 64, paint);
        }

        protected Bitmap renderAComponent(
                Dim dim,
                int startAngle,
                Float[] data,
                float totalCount,
                int[] colors,
                boolean allDone)
        {
            // Surface where we render our design
            int mainDiameter = dim.scaledDiameter - dim.scaledGap * 2;
            Bitmap bitmap = Bitmap.createBitmap(mainDiameter, mainDiameter, Bitmap.Config.ARGB_8888);
            Canvas canvas    = new Canvas(bitmap);
            // Clip that surface
            RectF clipper = new RectF(dim.scaledGap, dim.scaledGap, dim.scaledDiameter - dim.scaledGap, dim.scaledDiameter - dim.scaledGap);
            float start = startAngle;
            int i = 0;
            for(Float value:data) {
                float sweep = (float) 360 * ( value / totalCount );
                mBgPaints.setColor(colors[i++]);
                canvas.drawArc(clipper, start, sweep, true, mBgPaints);
                canvas.drawArc(clipper, start, sweep, true, mLinePaints);
                if(allDone) {
                    // Well I could do something here!
                }
                start += sweep;
            }

            // Now, render inner disc
            int innerDiameter = mainDiameter - dim.scaledThickness;

            //
            RectF innerClipper = new RectF(
                    dim.scaledGap,
                    dim.scaledGap,
                    innerDiameter - dim.scaledGap,
                    innerDiameter - dim.scaledGap);
            canvas.translate((float) dim.scaledThickness / 2, (float) dim.scaledThickness / 2);
            canvas.drawArc(innerClipper, 0.0f, 360.0f, true, mMaskPaints);

            return bitmap;
        }

        class AnimatedPBall {
            static final int PBALL_WIDTH = 10;
            static final int HALF_PBALL_WIDTH = PBALL_WIDTH / 2;
            static final float GRAVITY = 9.81f;
            static final float FRICTION = 0.95f;
            static final float PBALL_ELASTICITY = 0.99f;

            protected int mColor;
            float mX;
            float mY;
            float mDy;
            public AnimatedPBall(int color, int x, int y) {
                mColor = color;
                mX     = x;
                mY     = y;
                mDy    = 0;
            }

            /*
             * A very naive fall+bounce method, with some gravity, some air friction and ball elasticity.
             */
            public void animate(Canvas canvas, Paint paint, long elapsed) {
                mDy += GRAVITY * getStepperValue() / 1000;
                Log.d("ABCD", "Elapsed=" + elapsed + ", mDy =" + mDy + ", stepped=" + (getStepped() / 1000) + ",stepper=" + getStepperValue() + ",mY=" + mY);
                mDy *= FRICTION;
                mY += mDy;

                if(mY < 0) {
                    mY = 0;
                    mDy = -mDy;
                }
                if(mY + PBALL_WIDTH >= mContainerHeight) {
                    mY = mContainerHeight - PBALL_WIDTH;
                    mDy = (int)-(mDy * PBALL_ELASTICITY);
                }

                draw(canvas, paint);
            }

            protected void draw(Canvas canvas, Paint paint) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(mColor);
                canvas.drawCircle(mX + HALF_PBALL_WIDTH, mY + HALF_PBALL_WIDTH, PBALL_WIDTH, paint);
            }
        }
    }

    class Dim {
        //
        // "Sane" values that are used in rendering/animation
        //

        // A fairly popular width
        public final static int Diameter = 480;
        // Ring width
        public final static int Thickness =  Diameter * 20 /100;
        // Max radius is obvious
        public final static int Radius = Diameter / 2;
        //
        public final static int Gap = 0;

        // This ratio will change as we resize elements
        public float scale   = 1.0f;
        public float scaledCentreX = Diameter / 2;
        public float scaledCentreY = Diameter / 2;

        public float refScale      = scale;
        public int refDiameter     = Diameter;
        public int refGap          = Gap;
        public int refThickness    = Thickness;

        public int scaledDiameter  = Diameter;
        public int scaledGap       = Gap;
        public int scaledThickness = Thickness;

        public Dim(int desiredDiameter) {
            scale   = (float)desiredDiameter / (float)Diameter;
            refScale     = scale;
            reScale(Diameter, Gap, Thickness);
            refDiameter  = scaledDiameter;
            refGap       = scaledGap;
            refThickness = scaledThickness;
        }

        public Dim reScale(int diameter, int gap, int thickness) {
            scaledDiameter  = (int)(diameter * scale);
            scaledGap       = (int)(gap * scale);
            scaledThickness = (int)(thickness * scale);

            scaledCentreX = scaledCentreY = (scaledDiameter - (scaledGap * 2)) / 2;
            return this;
        }
    }