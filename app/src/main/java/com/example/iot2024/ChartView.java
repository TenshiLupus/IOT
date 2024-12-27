package com.example.iot2024;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ChartView extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint gridPaint;

    // Sample data for the chart
    private float[] xPoints = {50, 150, 250, 350, 450};
    private float[] yPoints = {300, 200, 400, 100, 250};

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint for the grid lines
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2f);

        // Paint for the line connecting points
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(4f);

        // Paint for the points
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background grid lines
        int height = getHeight();
        int width = getWidth();
        for (int i = 50; i < height; i += 50) {
            canvas.drawLine(0, i, width, i, gridPaint);
        }
        for (int i = 50; i < width; i += 50) {
            canvas.drawLine(i, 0, i, height, gridPaint);
        }

        // Draw the line connecting points
        for (int i = 0; i < xPoints.length - 1; i++) {
            canvas.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], linePaint);
        }

        // Draw the points
        for (int i = 0; i < xPoints.length; i++) {
            canvas.drawCircle(xPoints[i], yPoints[i], 10, pointPaint);
        }
    }
}