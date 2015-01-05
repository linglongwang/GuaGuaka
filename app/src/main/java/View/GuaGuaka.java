package View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import guaguaka.wll.com.guaguaka.OnCompleteListener;
import guaguaka.wll.com.guaguaka.R;

/**
 * Created by wanglinglong on 15-1-2.
 */
public class GuaGuaka extends View{

    private int mLastX;
    private int mLastY;

    private Canvas mCanvas;
    private Bitmap mBitmap;

    private Paint mOutPaint;
    private Paint mTextPaint;
    private int mTextSize;
    private int mTextColor;
    private String mText ;
    private Rect mTextBounds;

    private Path path;

    private Bitmap mLocalBitmap;
    private Bitmap mCover;

    private int mTotalPixels;
    private int mClearedPixels;
    private OnCompleteListener onCompleteListener;

    public GuaGuaka(Context context) {
        this(context, null);
    }

    public GuaGuaka(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int x = getMeasuredWidth();
        int y = getMeasuredHeight();

        //A R G B  R 有值的才会透明
        mBitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        setupOutPaint();
        setupTextPaint();

        mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawBitmap(mCover, null, new Rect(0, 0, x, y), mOutPaint);
    }

    private void setupTextPaint() {

        mTextSize = 50;
        mTextColor = Color.RED;
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextBounds = new Rect();

        mText = "$500,000,000";

        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
    }

    private void setupOutPaint() {
        mOutPaint = new Paint();
        mOutPaint.setStyle(Paint.Style.STROKE);
        mOutPaint.setAntiAlias(true);
        mOutPaint.setDither(true);
        mOutPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutPaint.setStrokeWidth(50);
    }

    public GuaGuaka(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override

    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mLocalBitmap,null, new Rect(0, 0, getWidth(), getHeight()), null);
        if (!isComplete){

            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }else {
            if (onCompleteListener != null){
                onCompleteListener.complete();
            }
        }

    }

    private void drawText() {
        mCanvas.drawText(mText, getWidth()/2-mTextBounds.width()/2,
                getHeight()/2+mTextBounds.height()/2, mTextPaint);
    }

    private void drawPath() {
        mOutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(path, mOutPaint);

    }

    private void init() {

        path = new Path();

        mLocalBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.mei);
        mCover = BitmapFactory.decodeResource(getResources(),
                R.drawable.you);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN :
                mLastX = x;
                mLastY = y;

                path.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy= Math.abs(y- mLastY);
                if (dx > 5 || dy > 5){
                    path.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
        }
        invalidate();
        return true;
    }

    private volatile boolean isComplete;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            float totalArea = w * h;
            float clearArea = 0;

            int[] mPixels = new int[w * h];

            //计算清楚的像素点数
            mBitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        clearArea++;
                    }
                }
            }
            if (totalArea > 0 && clearArea > 0) {
                int percent = (int) (100 * clearArea / totalArea);
                Log.d("clear percent : ", " " + percent);
                if (percent > 40) {
                    //大于60%全部清除
                    isComplete = true;
                    postInvalidate();
                }
            }
        }
    };

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }
}
