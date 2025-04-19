package com.its.tourist;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.Calendar;

/**
 * @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 */
public class ToolbarArcBackground extends View {
    private float scale = 1;

    private float timeRate;

    private int gradientColor1 = 0xff4CAF50;
    private int gradientColor2 = 0xFF0E3D10;
    private int lastGradientColor1 = 0xff4CAF50;
    private int lastGradientColor2 = 0xFF0E3D10;
    private Context context;

    private Bitmap sun;
    private Bitmap sunMorning;
    private Bitmap sunNoon;
    private Bitmap sunEvening;

    private Bitmap cloud1;
    private Bitmap cloud2;
    private Bitmap cloud3;

    private Bitmap moon;
    private Bitmap star;

    int waveHeight = 60;

    private int cloud1Y = waveHeight + 150;
    private int cloud2Y = waveHeight + 120;
    private int cloud3Y = waveHeight + 20;

    private int height;

    private Day now = Day.MORNING;

    enum Day {
        MORNING, NOON, AFTERNOON, EVENING,
        MIDNIGHT
    }

    public ToolbarArcBackground(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ToolbarArcBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ToolbarArcBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    private void init() {
        calculateTimeLine();
        createBitmaps();
        initGradient();
    }

    private void initGradient(){
        switch (now) {
            case MORNING:
                lastGradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_midnight);
                lastGradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_midnight);

                gradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_morning);
                gradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_morning);
                break;
            case NOON:
                lastGradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_morning);
                lastGradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_morning);

                gradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_noon);
                gradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_noon);
                break;
            case AFTERNOON:
                lastGradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_noon);
                lastGradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_noon);

                gradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_noon_evening);
                gradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_noon_evening);
                break;
            case EVENING:
                lastGradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_noon_evening);
                lastGradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_noon_evening);

                gradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_evening);
                gradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_evening);
                break;
            case MIDNIGHT:
                lastGradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_evening);
                lastGradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_evening);

                gradientColor1 = ContextCompat.getColor(context, R.color.toolbar_gradient_1_midnight);
                gradientColor2 = ContextCompat.getColor(context, R.color.toolbar_gradient_2_midnight);
                break;
        }
    }

    private void calculateTimeLine() {
        //Al posto di usare la classe Date ho usato la classe Calendar in quanto la prima classe è deprecata e non va
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.HOUR_OF_DAY) > 5 && c.get(Calendar.HOUR_OF_DAY) < 11) {
            now = Day.MORNING;
        } else if (c.get(Calendar.HOUR_OF_DAY) < 13 && c.get(Calendar.HOUR_OF_DAY) >= 11) {
            now = Day.NOON;
        } else if (c.get(Calendar.HOUR_OF_DAY) < 18 && c.get(Calendar.HOUR_OF_DAY) >= 13) {
            now = Day.AFTERNOON;
        } else if (c.get(Calendar.HOUR_OF_DAY) < 22 && c.get(Calendar.HOUR_OF_DAY) >= 18) {
            now = Day.EVENING;
        } else if (c.get(Calendar.HOUR_OF_DAY) <= 5 || c.get(Calendar.HOUR_OF_DAY) >= 22
                && c.get(Calendar.HOUR_OF_DAY) < 23) {
            now = Day.MIDNIGHT;
        }

        if (c.get(Calendar.HOUR_OF_DAY) > 5 && c.get(Calendar.HOUR_OF_DAY) < 18) {
            timeRate = ((float)c.get(Calendar.HOUR_OF_DAY) - 5 )/ 13;
        } else {
            if (c.get(Calendar.HOUR_OF_DAY) >= 18) {
                timeRate = (float)(c.get(Calendar.HOUR_OF_DAY) - 17) / 12;
            } else {
                timeRate = (float)(c.get(Calendar.HOUR_OF_DAY) + 6) / 12;
            }
        }
    }

    private void createBitmaps() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        cloud1 = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_cloud_01);
        cloud2 = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_cloud_02);
        cloud3 = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_cloud_03);

        sun = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_sun_noon);
        sunMorning = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_sun_morning);
        sunNoon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_sun_noon);
        sunEvening = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_sun_evening);

        moon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_moon);
        star = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_stars);
    }

    public void startAnimate(){
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(3000);
        //anim.setInterpolator();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float temp = timeRate;

            int currentGradientColor1 =gradientColor1;
            int currentGradientColor2 =gradientColor2;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (float) animation.getAnimatedValue();
                timeRate = currentValue * temp;

                ArgbEvaluator argbEvaluator = new ArgbEvaluator();
                gradientColor1 = (int) (argbEvaluator.evaluate(currentValue, lastGradientColor1,
                        currentGradientColor1));
                gradientColor2 = (int) (argbEvaluator.evaluate(currentValue, lastGradientColor2,
                        currentGradientColor2));
                invalidate();
            }
        });
        anim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGradient(canvas);
        drawCloud(canvas);
        if (now == Day.MIDNIGHT || now == Day.EVENING) {
            drawStar(canvas);
            drawMoon(canvas);
        } else {
            drawSun(canvas);
        }
        //drawOval(canvas);
    }

    private void drawOval(Canvas canvas) {
        Paint ovalPaint = new Paint();
        final Path path = new Path();
        ovalPaint.setColor(Color.WHITE);
        ovalPaint.setAntiAlias(true);

        path.moveTo(0, getMeasuredHeight());

        //arco
        path.quadTo(getMeasuredWidth() >> 2, getMeasuredHeight() - waveHeight * scale,
                getMeasuredWidth(), getMeasuredHeight());

        path.lineTo(0, getMeasuredHeight());
        path.close();
        canvas.drawPath(path, ovalPaint);
    }

    private void drawCloud(Canvas canvas) {
        int cloud1X = 50;
        canvas.drawBitmap(cloud1, cloud1X * scale, cloud1Y * scale, null);
        int cloud2X = 450;
        canvas.drawBitmap(cloud2, cloud2X * scale, cloud2Y * scale, null);
        int cloud3X = 850;
        canvas.drawBitmap(cloud3, cloud3X + (1 - scale) * getMeasuredWidth(),
                cloud3Y * scale, null);
    }

    private void drawStar(Canvas canvas) {
        canvas.drawBitmap(star, 100, 10, null);
        canvas.drawBitmap(star, 500, 130, null);
        canvas.drawBitmap(star, 900, 40, null);
        canvas.drawBitmap(star, 1250, 100, null);
    }

    private void drawMoon(Canvas canvas) {
        int passed =  (int)(getMeasuredWidth() * timeRate);
        int xpos = passed - moon.getWidth() / 2;
        canvas.drawBitmap(moon, xpos + (1 - scale) * getMeasuredWidth(), (float) (-height/1.2), null);
    }

    private void drawGradient(Canvas canvas) {
        Paint paint = new Paint();
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        int changedColor1 = (int) (argbEvaluator.evaluate(1 - scale, gradientColor1,
                ContextCompat.getColor(context, R.color.colorPrimaryDark)));
        int changedColor2 = (int) (argbEvaluator.evaluate(1 - scale, gradientColor2,
                ContextCompat.getColor(context, R.color.toolbar_gradient_2_noon)));
        LinearGradient linearGradient = new LinearGradient(0f, 0f, getMeasuredWidth(),
                getMeasuredHeight(), changedColor1, changedColor2, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
    }

    private void drawSun(Canvas canvas) {
        int passed =  (int)(getMeasuredWidth() * timeRate);
        int xpos = passed - sun.getWidth() / 2;
        if (now == Day.MORNING) {
            canvas.drawBitmap(sunMorning, xpos + (1 - scale) * getMeasuredWidth(),
                    (float) (-height/1.2), null);
        } else if (now == Day.NOON) {
            canvas.drawBitmap(sunNoon, xpos + (1 - scale) * getMeasuredWidth(),
                    (float) (-height/1.2), null);
        } else if (now == Day.AFTERNOON) {
            canvas.drawBitmap(sunEvening, xpos + (1 - scale) * getMeasuredWidth(),
                    (float) (-height / 1.2), null);
        }
    }

    public void setHeight(int h){
        height = h;
    }

}