package com.example.android.filter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.example.android.filter.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = "FaceGraphic";

    private static final float DOT_RADIUS = 3.0f;
    private static final float TEXT_OFFSET_Y = -30.0f;

    private boolean mIsFrontFacing;

    private volatile FaceData mFaceData;

    private Paint mHintTextPaint;
    private Paint mHintOutlinePaint;
    private Paint mEyeWhitePaint;
    private Paint mIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyelidPaint;

    private Drawable mPigNoseGraphic;
    private Drawable mMustacheGraphic;
    private Drawable mHappyStarGraphic;
    private Drawable mHatGraphic;


    FaceGraphic(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
        super(overlay);
        mIsFrontFacing = isFrontFacing;
        Resources resources = context.getResources();
        initializePaints(resources);
        initializeGraphics(resources);
    }

    private void initializeGraphics(Resources resources) {
        mPigNoseGraphic = resources.getDrawable(R.drawable.pig_nose_emoji);
        mMustacheGraphic = resources.getDrawable(R.drawable.mustache);
        mHappyStarGraphic = resources.getDrawable(R.drawable.happy_star);
        mHatGraphic = resources.getDrawable(R.drawable.red_hat);
    }

    private void initializePaints(Resources resources) {
        mHintTextPaint = new Paint();
        mHintTextPaint.setColor(resources.getColor(R.color.overlayHint));
        mHintTextPaint.setTextSize(resources.getDimension(R.dimen.textSize));

        mHintOutlinePaint = new Paint();
        mHintOutlinePaint.setColor(resources.getColor(R.color.overlayHint));
        mHintOutlinePaint.setStyle(Paint.Style.STROKE);
        mHintOutlinePaint.setStrokeWidth(resources.getDimension(R.dimen.hintStroke));

        mEyeWhitePaint = new Paint();
        mEyeWhitePaint.setColor(resources.getColor(R.color.eyeWhite));
        mEyeWhitePaint.setStyle(Paint.Style.FILL);

        mIrisPaint = new Paint();
        mIrisPaint.setColor(resources.getColor(R.color.iris));
        mIrisPaint.setStyle(Paint.Style.FILL);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(resources.getColor(R.color.eyeOutline));
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(resources.getDimension(R.dimen.eyeOutlineStroke));

        mEyelidPaint = new Paint();
        mEyelidPaint.setColor(resources.getColor(R.color.eyelid));
        mEyelidPaint.setStyle(Paint.Style.FILL);
    }

    void update(FaceData faceData){
        mFaceData=faceData;
        postInvalidate();// it cause draw method to call again
    }

    @Override
    public void draw(Canvas canvas) {
        final float DOT_RADIUS = 3.0f;
        final float TEXT_OFFSET_Y = -30.0f;

        // Confirm that the face and its features are still visible
        // before drawing any graphics over it.
        if (mFaceData == null) {
            return;
        }

        PointF detectPosition = mFaceData.getPosition();
        PointF detectLeftEyePosition = mFaceData.getLeftEyePosition();
        PointF detectRightEyePosition = mFaceData.getRightEyePosition();
        PointF detectNoseBasePosition = mFaceData.getNoseBasePosition();
        PointF detectMouthLeftPosition = mFaceData.getMouthLeftPosition();
        PointF detectMouthBottomPosition = mFaceData.getMouthBottomPosition();
        PointF detectMouthRightPosition = mFaceData.getMouthRightPosition();

        if ((detectPosition == null) ||
                (detectLeftEyePosition == null) ||
                (detectRightEyePosition == null) ||
                (detectNoseBasePosition == null) ||
                (detectMouthLeftPosition == null) ||
                (detectMouthBottomPosition == null) ||
                (detectMouthRightPosition == null)) {
            return;
        }

        // Face position and dimensions
        PointF position = new PointF(translateX(detectPosition.x),
                translateY(detectPosition.y));
        float width = scaleX(mFaceData.getWidth());
        float height = scaleY(mFaceData.getHeight());

        // Eye coordinates
        PointF leftEyePosition = new PointF(translateX(detectLeftEyePosition.x),
                translateY(detectLeftEyePosition.y));
        PointF rightEyePosition = new PointF(translateX(detectRightEyePosition.x),
                translateY(detectRightEyePosition.y));

        // Eye state
        boolean leftEyeOpen = mFaceData.isLeftEyeOpen();
        boolean rightEyeOpen = mFaceData.isRightEyeOpen();

        // Nose coordinates
        PointF noseBasePosition = new PointF(translateX(detectNoseBasePosition.x),
                translateY(detectNoseBasePosition.y));

        // Mouth coordinates
        PointF mouthLeftPosition = new PointF(translateX(detectMouthLeftPosition.x),
                translateY(detectMouthLeftPosition.y));
        PointF mouthRightPosition = new PointF(translateX(detectMouthRightPosition.x),
                translateY(detectMouthRightPosition.y));
        PointF mouthBottomPosition = new PointF(translateX(detectMouthBottomPosition.x),
                translateY(detectMouthBottomPosition.y));

        // Smile state
        boolean smiling = mFaceData.isSmiling();


        final float EYE_RADIUS_PROPORTION = 0.45f;
        final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;
        float distance = (float) Math.sqrt(
                (rightEyePosition.x - leftEyePosition.x) * (rightEyePosition.x - leftEyePosition.x) +
                        (rightEyePosition.y - leftEyePosition.y) * (rightEyePosition.y - leftEyePosition.y));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        // Draw the eyes.
        drawEye(canvas, leftEyePosition, eyeRadius, leftEyePosition, irisRadius, leftEyeOpen, smiling);
        drawEye(canvas, rightEyePosition, eyeRadius, rightEyePosition, irisRadius, rightEyeOpen, smiling);

        // Draw the nose.
        drawNose(canvas, noseBasePosition, leftEyePosition, rightEyePosition, width);


    }
    private void drawEye(Canvas canvas,
                         PointF eyePosition, float eyeRadius,
                         PointF irisPosition, float irisRadius,
                         boolean eyeOpen, boolean smiling) {
        if (eyeOpen) {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitePaint);
            if (smiling) {
                mHappyStarGraphic.setBounds(
                        (int)(irisPosition.x - irisRadius),
                        (int)(irisPosition.y - irisRadius),
                        (int)(irisPosition.x + irisRadius),
                        (int)(irisPosition.y + irisRadius));
                mHappyStarGraphic.draw(canvas);
            } else {
                canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mIrisPaint);
            }
        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyelidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }
        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }

    private void drawNose(Canvas canvas,
                          PointF noseBasePosition,
                          PointF leftEyePosition, PointF rightEyePosition,
                          float faceWidth) {
        final float NOSE_FACE_WIDTH_RATIO = (float)(1 / 5.0);
        float noseWidth = faceWidth * NOSE_FACE_WIDTH_RATIO;
        int left = (int)(noseBasePosition.x - (noseWidth / 2));
        int right = (int)(noseBasePosition.x + (noseWidth / 2));
        int top = (int)(leftEyePosition.y + rightEyePosition.y) / 2;
        int bottom = (int)noseBasePosition.y;

        mPigNoseGraphic.setBounds(left, top, right, bottom);
        mPigNoseGraphic.draw(canvas);
    }

}
