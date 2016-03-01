package com.chinababyface.view.segment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.chinababyface.view.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ChinaBabyFace
 */
public class SegmentIndicatorBar extends View {

    /**
     * 主画笔
     */
    private Paint mPaint;
    /**
     * 用于绘制指针形状
     */
    private Path indicatorPath;
    /**
     * 线段的描述字体尺寸
     */
    private float segmentLabelTextSize = 20;
    /**
     * 线段高度
     */
    private float segmentHeight = 35;
    /**
     * 线段对象集合
     */
    private List<SegmentView> segmentList;
    /**
     * 刻度字体尺寸
     */
    private float graduationTextSize = 20;
    /**
     * 刻度字体颜色
     */
    private int graduationColor = Color.BLACK;
    /**
     * 指针数值字体尺寸
     */
    private float valueTextSize = 30;
    /**
     * 指针数值字体颜色
     */
    private int valueTextColor = Color.BLACK;
    /**
     * 指针值
     */
    private float value = 28.5f;
    /**
     * 指针尺寸
     */
    private float indicatorTriangleSideLength = 30;
    /**
     * 指针颜色
     */
    private int indicatorColor = Color.YELLOW;
    /**
     * 是否显示刻度
     */
    private boolean isShowGraduation = true;
    /**
     * 是否显示指针
     */
    private boolean isShowIndicator = true;
    /**
     * 是否显示指针动画
     */
    private boolean isIndicatorAimation = true;

    public SegmentIndicatorBar(Context context) {
        super(context);

    }

    /**
     * TypedArray是一个用来存放由context.obtainStyledAttributes获得的属性的数组,
     * 在使用完成后，一定要调用recycle方法, 属性的名称是styleable中的名称+“_”+属性名称
     *
     * @param context
     * @param attrs
     */
    public SegmentIndicatorBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        indicatorPath = new Path();
        segmentList = getBmiSegmentViewList();
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.SegmentIndicatorBar);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.SegmentIndicatorBar_segmentHeight) {
                segmentHeight = array.getDimension(attr, segmentHeight);
            } else if (attr == R.styleable.SegmentIndicatorBar_segmentLabelTextSize) {
                segmentLabelTextSize = array.getDimensionPixelSize(attr,
                        getTextSize((int) segmentLabelTextSize));
            } else if (attr == R.styleable.SegmentIndicatorBar_graduationTextSize) {
                graduationTextSize = array.getDimensionPixelSize(attr,
                        getTextSize((int) graduationTextSize));
            } else if (attr == R.styleable.SegmentIndicatorBar_graduationColor) {
                graduationColor = array.getColor(attr, graduationColor);
            } else if (attr == R.styleable.SegmentIndicatorBar_valueTextSize) {
                valueTextSize = array.getDimensionPixelSize(attr,
                        getTextSize((int) valueTextSize));
            } else if (attr == R.styleable.SegmentIndicatorBar_valueTextColor) {
                valueTextColor = array.getColor(attr, valueTextColor);
            } else if (attr == R.styleable.SegmentIndicatorBar_value) {
                value = array.getFloat(attr, 0);
            } else if (attr == R.styleable.SegmentIndicatorBar_indicatorTriangleSideLength) {
                indicatorTriangleSideLength = array.getDimension(attr,
                        indicatorTriangleSideLength);
            } else if (attr == R.styleable.SegmentIndicatorBar_indicatorColor) {
                indicatorColor = array.getColor(attr, indicatorColor);
            } else if (attr == R.styleable.SegmentIndicatorBar_isShowGraduation) {
                isShowGraduation = array.getBoolean(attr, true);
            } else if (attr == R.styleable.SegmentIndicatorBar_isShowIndicator) {
                isShowIndicator = array.getBoolean(attr, true);
            } else if (attr==R.styleable.SegmentIndicatorBar_isIndicatorAimation) {
                isIndicatorAimation = array.getBoolean(attr, true);
            }
        }
        array.recycle(); // 一定要调用，否则这次的设定会对下次的使用造成影响
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float barTotalWidth = canvas.getWidth() - getPaddingLeft()
                - getPaddingRight();
        float barSegmentWidth = barTotalWidth / segmentList.size();
        float indicatorTranslateDelta = 0.0f;

        mPaint.setTextSize(valueTextSize);
        float valueWidth = mPaint.measureText("" + value);
        float valueTop = 0;
        float valueLeft = 0;
        float valueRight = 0;
        float valueBottom = valueTop + valueTextSize;

        float cursorHeight = (float) Math.sqrt(Math.pow(
                indicatorTriangleSideLength, 2)
                - Math.pow(indicatorTriangleSideLength / 2, 2));
        float cursorTop = valueBottom;
        float cursorLeft = 0;
        float cursorBottom = cursorTop + cursorHeight;

        float barTop = cursorBottom;
        float barLeft = getPaddingLeft();
        float barBottom = barTop + segmentHeight;

        // 准备画笔
        mPaint.setStyle(Style.FILL); // 设置填充
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // 开始根据输入值计算指针相对于起点的偏移量indicatorTranslateDelta
        if (segmentList.size() > 0) {
            for (int i = 0; i < segmentList.size(); i++) {
                if (value - segmentList.get(i).getEndX() >= 0) {
                    indicatorTranslateDelta += barSegmentWidth;
                } else {
                    indicatorTranslateDelta += (value - segmentList.get(i)
                            .getStartX())
                            / (segmentList.get(i).getEndX() - segmentList
                            .get(i).getStartX()) * barSegmentWidth;
                    break;
                }
            }
        }

        // 绘制指针上的值
        mPaint.setColor(valueTextColor);
        valueLeft = getPaddingLeft() + indicatorTranslateDelta - valueWidth / 2;
        valueRight = valueLeft + valueWidth;
        drawTextInCenter(canvas, valueLeft, valueTop, valueRight, valueBottom,
                mPaint, "" + value);

        // 绘制指针图形
        cursorLeft = getPaddingLeft() + indicatorTranslateDelta
                - indicatorTriangleSideLength / 2;
        Log.e("DIY", "V:" + valueLeft + "C" + cursorLeft);
        mPaint.setColor(indicatorColor);
        indicatorPath.moveTo(cursorLeft, cursorTop);// 此点为多边形的起点
        indicatorPath.lineTo(cursorLeft + indicatorTriangleSideLength,
                cursorTop);
        indicatorPath.lineTo(getPaddingLeft() + indicatorTranslateDelta,
                cursorBottom);
        indicatorPath.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(indicatorPath, mPaint);

        // 绘制线段及刻度
        for (int i = 0; i < segmentList.size(); i++) {
            // 绘制线段
            mPaint.setColor(segmentList.get(i).getBackgroundColor());
            canvas.drawRect(barLeft + i * barSegmentWidth, barTop, barLeft
                    + (i + 1) * barSegmentWidth, barBottom, mPaint);
            // 绘制线段标签
            mPaint.setColor(segmentList.get(i).getLabelColor());
            mPaint.setTextSize(segmentLabelTextSize);
            drawTextInCenter(canvas, barLeft + i * barSegmentWidth, barTop,
                    barLeft + (i + 1) * barSegmentWidth, barBottom, mPaint,
                    segmentList.get(i).getName());
            // 绘制刻度
            mPaint.setColor(graduationColor);
            mPaint.setTextSize(graduationTextSize);
            float graduationLeft = barLeft + (i + 1) * barSegmentWidth
                    - mPaint.measureText("" + segmentList.get(i).getEndX()) / 2;
            float graduationRight = barLeft + (i + 1) * barSegmentWidth
                    + mPaint.measureText("" + segmentList.get(i).getEndX()) / 2;
            drawTextInCenter(canvas, graduationLeft, barBottom,
                    graduationRight, barBottom + segmentHeight, mPaint, ""
                            + segmentList.get(i).getEndX());
        }
    }

    public void drawTextInCenter(Canvas canvas, RectF targetRect, Paint mPaint,
                                 String content) {
        FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        float baseline = targetRect.top
                + (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top)
                / 2 - fontMetrics.top;
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(content, targetRect.centerX(), baseline, mPaint);
    }

    public void drawTextInCenter(Canvas canvas, float left, float top,
                                 float right, float bottom, Paint mPaint, String content) {
        FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        float baseline = top
                + (bottom - top - fontMetrics.bottom + fontMetrics.top) / 2
                - fontMetrics.top;
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(content, (left + right) * 0.5f, baseline, mPaint);
    }

    public List<SegmentView> getBmiSegmentViewList() {
        List<SegmentView> list = new ArrayList<>();
        SegmentView segment1 = new SegmentView();
        segment1.setName("偏瘦");
        segment1.setStartX(0);
        segment1.setStartY(0);
        segment1.setEndX(18.5f);
        segment1.setEndY(0);
        segment1.setLabelColor(Color.WHITE);
        segment1.setBackgroundColor(Color.BLUE);
        SegmentView segment2 = new SegmentView();
        segment2.setName("标准");
        segment2.setStartX(18.5f);
        segment2.setStartY(0);
        segment2.setEndX(24.0f);
        segment2.setEndY(0);
        segment2.setLabelColor(Color.WHITE);
        segment2.setBackgroundColor(Color.RED);
        SegmentView segment3 = new SegmentView();
        segment3.setName("偏胖");
        segment3.setStartX(24.0f);
        segment3.setStartY(0);
        segment3.setEndX(28.0f);
        segment3.setEndY(0);
        segment3.setLabelColor(Color.WHITE);
        segment3.setBackgroundColor(Color.GREEN);
        SegmentView segment4 = new SegmentView();
        segment4.setName("肥胖");
        segment4.setStartX(28.0f);
        segment4.setStartY(0);
        segment4.setEndX(100.0f);
        segment4.setEndY(0);
        segment4.setLabelColor(Color.WHITE);
        segment4.setBackgroundColor(Color.MAGENTA);
        list.add(segment1);
        list.add(segment2);
        list.add(segment3);
        list.add(segment4);
        return list;
    }

    public int getTextSize(int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, getResources().getDisplayMetrics());
    }

    public float getSegmentLabelTextSize() {
        return segmentLabelTextSize;
    }

    public void setSegmentLabelTextSize(float segmentLabelTextSize) {
        this.segmentLabelTextSize = segmentLabelTextSize;
    }

    public float getSegmentHeight() {
        return segmentHeight;
    }

    public void setSegmentHeight(float segmentHeight) {
        this.segmentHeight = segmentHeight;
    }

    public List<SegmentView> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<SegmentView> segmentList) {
        this.segmentList = segmentList;
    }

    public float getGraduationTextSize() {
        return graduationTextSize;
    }

    public void setGraduationTextSize(float graduationTextSize) {
        this.graduationTextSize = graduationTextSize;
    }

    public int getGraduationColor() {
        return graduationColor;
    }

    public void setGraduationColor(int graduationColor) {
        this.graduationColor = graduationColor;
    }

    public float getValueTextSize() {
        return valueTextSize;
    }

    public void setValueTextSize(float valueTextSize) {
        this.valueTextSize = valueTextSize;
    }

    public int getValueTextColor() {
        return valueTextColor;
    }

    public void setValueTextColor(int valueTextColor) {
        this.valueTextColor = valueTextColor;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getIndicatorTriangleSideLength() {
        return indicatorTriangleSideLength;
    }

    public void setIndicatorTriangleSideLength(float indicatorTriangleSideLength) {
        this.indicatorTriangleSideLength = indicatorTriangleSideLength;
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }

    public boolean isShowGraduation() {
        return isShowGraduation;
    }

    public void setShowGraduation(boolean isShowGraduation) {
        this.isShowGraduation = isShowGraduation;
    }

    public boolean isShowIndicator() {
        return isShowIndicator;
    }

    public void setShowIndicator(boolean isShowIndicator) {
        this.isShowIndicator = isShowIndicator;
    }

    public boolean isIndicatorAimation() {
        return isIndicatorAimation;
    }

    public void setIndicatorAimation(boolean isIndicatorAimation) {
        this.isIndicatorAimation = isIndicatorAimation;
    }
}
