package com.xty.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


/**
 * Created by nanhuaqq on 2015/12/8.
 * 自适应的tagview  有 单选 多选模式（可替换选车中的文字标签控件）
 */
public class BjTagsContainView extends ViewGroup{
    private int hCount;
    private int vCount; //多少行是要经过自适应计算
    private float hGap; //横向间距 其实只能算是最小横向间距，自适应过程中会重新计算hGap；
    private float vGap; //竖直间距
    private boolean isMultMode = false; //默认为单选

    private float maxTextLen = 0.0f; //根据标签计算最大 字符长度
    private float tagPaddingLeftAndRight;  //
    private int tagBgId;
    private int tagTextColorId;
    private ColorStateList tagColorList;
    private float tagHeight;
    private float tagTextSize;

    private boolean autoAdjust = true; //是否需要自适应（默认为需要自适应）

    private float tagWidth; //要经过计算得出

    private Point size = new Point();

    private List<String> tags;

    /**
     * 记录 tag 选中状态的 位置
     */
    private BitSet selectPositionBitSet;
    private int preSelected = -1;

    private Paint textMesurePaint = new TextPaint();

    /**
     * 互斥位置 （该tag 与 其它所有的tags 选中状态互斥）
     */
    private int excludePosition = -1;

    private boolean isSupportAnimation = false;

    public int getExcludePosition() {
        return excludePosition;
    }

    public void setExcludePosition(int excludePosition) {
        this.excludePosition = excludePosition;
    }

    private void init(AttributeSet attr){
        selectPositionBitSet = new BitSet();
        tags = new ArrayList<>();
        if ( attr != null ){
            TypedArray a = getContext().obtainStyledAttributes(attr, R.styleable.bjTagsContainView);
            if ( a != null ){
                hCount = a.getInt(R.styleable.bjTagsContainView_bj__hcount, 4);
                hGap = a.getDimension(R.styleable.bjTagsContainView_bj__hgap, 16);
                vGap = a.getDimension(R.styleable.bjTagsContainView_bj__vgap, 16);
                tagPaddingLeftAndRight = a.getDimension(R.styleable.bjTagsContainView_bj__tag_paddingLeftAndRight, 4);
                tagBgId = a.getResourceId(R.styleable.bjTagsContainView_bj__tag_bg, R.drawable.bj__tag_cutdown_bg);
                tagTextColorId = a.getResourceId(R.styleable.bjTagsContainView_bj__tag_textColor, R.color.bj__cutdown_tag_text_color_selector);
                tagColorList = a.getColorStateList(R.styleable.bjTagsContainView_bj__tag_textColor);
                tagHeight = a.getDimension(R.styleable.bjTagsContainView_bj__tag_height, 27);
                tagTextSize = a.getDimension(R.styleable.bjTagsContainView_bj__tag_textsize, 14);
                isMultMode = a.getBoolean(R.styleable.bjTagsContainView_bj__multi_mode, false);
                autoAdjust = a.getBoolean(R.styleable.bjTagsContainView_bj__auto_adjust,true);
                isSupportAnimation = a.getBoolean(R.styleable.bjTagsContainView_bj__support_animation,false);
                textMesurePaint.setTextSize(tagTextSize);

            }
        }
    }

    AnimatorSet tagAnimationSet ;
    private void animateItem(View tagItemView){
        if ( isSupportAnimation ){
            tagAnimationSet = new AnimatorSet();
            Animator scaleX = ObjectAnimator.ofFloat(tagItemView, "scaleX",1f,1.2f,1f,1.15f,1f);
            scaleX.setInterpolator(new AccelerateInterpolator());
            scaleX.setDuration(300);

            Animator scaleY = ObjectAnimator.ofFloat(tagItemView, "scaleY",1f,1.2f,1f,1.15f,1f);
            scaleY.setInterpolator(new AccelerateInterpolator());
            scaleY.setDuration(300);
            tagAnimationSet.playTogether(scaleX,scaleY);
            tagAnimationSet.start();
        }
    }

    public BjTagsContainView(Context context) {
        super(context);
        init(null);
    }

    public BjTagsContainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BjTagsContainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BjTagsContainView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if ( tags == null || tags.size() == 0 ){
            return;
        }
        int size = getChildCount();
        int x = (int) hGap;
        int y = 0;
        for ( int position = 0; position < size; position++ ){
            if( position % hCount == 0 ){ //换行
                x = (int) hGap;

                if ( position == 0 ){
                    y += vGap;
                }else {
                    y = ( int ) ( y + tagHeight + vGap );
                }
            }
            View childView = getChildAt(position);
            int right = (int) (x + tagWidth);
            int bottom = (int) (y +tagHeight);
            childView.layout(x,y,right,bottom);

            x = (int) (x + tagWidth + hGap);
        }
    }

    private  int computeMeasureSize(int measureSpec, int defSize) {
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return Math.min(defSize, MeasureSpec.getSize(measureSpec));
            default:
                return defSize;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ( tags == null || tags.size() == 0 ){
            return;
        }
        size.x = computeMeasureSize(widthMeasureSpec, widthMeasureSpec);
        if ( autoAdjust ){
            calculateVCount();
        }else{
            calculateVCountNotAdjust();
        }
        size.y = (int) (vCount * tagHeight + ( vCount+1 ) * vGap);

        int len = getChildCount();
        for ( int i = 0; i < len; i++ ){
            View child = getChildAt(i);
            child.measure(
                    MeasureSpec.makeMeasureSpec((int) tagWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) tagHeight, MeasureSpec.EXACTLY)
            );
        }

//        measureChildren(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(size.x,size.y);
    }

    /**
     * 计算 tag 长度
     */
    private float calculateTagWidth(){
        for (String tag:
             tags) {
            if ( !TextUtils.isEmpty(tag) ){
                float textLen =  textMesurePaint.measureText(tag);
                maxTextLen =  maxTextLen < textLen ? textLen:maxTextLen;
            }
        }
        return 2 * tagPaddingLeftAndRight + maxTextLen;
    }

    /**
     * 根据 横向 tag的数目 计算容器最小宽度
     * @return
     */
    private float calculateTotalWidthByHcount(){
        return hCount * tagWidth + ( hCount+1 ) * hGap;
    }

    /**
     * 重新计算 横向间距
     * @return
     */
    private float reCalculateHGap(){
        return  ( size.x - hCount * tagWidth )/ ( hCount+1 );
    }

    private void calculateVCountNotAdjust(){
        tagWidth = (size.x - hGap *(hCount+1) )/ hCount;
        vCount = (int) Math.ceil( (float)tags.size() /(float) hCount );
    }

    /**
     * 计算 tag的行数目 (自适应计算)
     */
    private void calculateVCount(){
        tagWidth = calculateTagWidth(); //计算得到标签的长度
        float totalWidth = calculateTotalWidthByHcount();
        if ( totalWidth < size.x ){ //tagContainer宽度有盈余
            //重新计算 横向间距hGap;
            hGap = reCalculateHGap();
        }else if ( totalWidth > size.x ){ //tagContainer宽度不够了
            //重新计算 hCount hGap;
            while( --hCount > 0 ){
                totalWidth = calculateTotalWidthByHcount();
                if ( totalWidth < size.x ){
                    break;
                }
            }
            hGap = reCalculateHGap();
        }
        vCount = (int) Math.ceil( (float)tags.size() /(float) hCount );
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.removeAllViews();
        this.tags.addAll(tags);
        if ( this.tags != null && this.tags.size() > 0 ){
            int tagLen = this.tags.size();
            for ( int i = 0; i < tagLen; i ++ ) {
                String tagStr = this.tags.get(i);
                TextView tagView = createTagView(i,tagStr);
                if ( tagView != null ){
                    addView(tagView);
                }
            }

            postInvalidate();
        }
    }

    private TextView createTagView(int position,String tagStr){
        if ( TextUtils.isEmpty(tagStr) ){
            return null;
        }
        TextView tagView = new TextView(getContext());
        tagView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tagTextSize);
        tagView.setGravity(Gravity.CENTER);
        tagView.setBackgroundResource(tagBgId);
        tagView.setTextColor(tagColorList);
        tagView.setText(tagStr);

        LayoutParams layoutParams = new LayoutParams((int)tagWidth,(int)tagHeight);
        tagView.setLayoutParams(layoutParams);
        tagView.setTag(position);
        tagView.setOnClickListener(tagClickListener);

        return tagView;
    }

    /**
     * 记录选中状态
     * @param position
     * @param selected
     */
    private void recordSelectStatus (int position,boolean selected) {
        if ( isMultMode ){
            selectPositionBitSet.set(position,selected);
        }else {
            if ( preSelected != -1 && position != preSelected ){ //清除以前的选中状态
                if ( preSelected != -1 ){
                    getChildAt(preSelected).setSelected(false);
                }
            }
            selectPositionBitSet.clear();
            selectPositionBitSet.set(position,selected);
            preSelected = position;
        }
    }

    private OnClickListener tagClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            String tagStr = tags.get(position);

            boolean isSelected = v.isSelected();
            isSelected = !isSelected;
            recordSelectStatus(position, isSelected);
            v.setSelected(isSelected);

            if ( position == excludePosition && isSelected ){
                clearAllOtherSelectedStatus();
            }else if ( position != excludePosition && isSelected ){
                clearExcludeSelectedStatus();
            }
            animateItem(v);
            if ( onTagClickListener != null ){
                onTagClickListener.onTagClick(v,position,tagStr);
            }
        }
    };

    private OnTagClickListener onTagClickListener;

    public OnTagClickListener getOnTagClickListener() {
        return onTagClickListener;
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
    }

    public interface OnTagClickListener{
        void onTagClick(View child, int position, String tagStr);
    }

    /**
     * 获取所有选中的选项
     * @return
     */
    public List<String> getAllSelectedTags(){
        if ( selectPositionBitSet.isEmpty() ){
            return null;
        }
        List<String> allSelectedTagList = new ArrayList<>();
        int len = tags.size();
        for ( int position = 0 ; position < len; position ++ ){
            boolean isSelected = selectPositionBitSet.get(position);
            if ( isSelected && position != excludePosition ){
                allSelectedTagList.add(tags.get(position));
            }
        }
        return  allSelectedTagList;
    }

    public void setSelectChildByPosition(int position){
        if ( position >= 0 && position < getChildCount() ){
            if ( !isMultMode && position != preSelected ){ //单选
                if ( preSelected != -1 ){
                    View child = getChildAt( preSelected );
                    if ( child != null ){
                        child.setSelected(false);
                    }
                }
                View childWillSelect = getChildAt(position);
                childWillSelect.setSelected(true);
                selectPositionBitSet.clear();
                selectPositionBitSet.set(position,true);
                preSelected = position;
            }else if ( isMultMode ){
                boolean hasSelectedThisPosition = selectPositionBitSet.get(position);
                if ( hasSelectedThisPosition ){
                    return;
                }else {
                    View childWillSelect = getChildAt(position);
                    childWillSelect.setSelected(true);
                    selectPositionBitSet.set(position,true);
                }
            }
        }
    }

    public void cancelSelectChildByPosition(int position){
        if ( position >= 0 && position < getChildCount() ){
            boolean hasSelectedThisPosition = selectPositionBitSet.get(position);
            if ( !hasSelectedThisPosition ){
                return;
            }else {
                View childWillSelect = getChildAt(position);
                childWillSelect.setSelected(false);
                selectPositionBitSet.set(position,false);
            }
        }
    }


    public void clearAllOtherSelectedStatus(){
        if ( !selectPositionBitSet.isEmpty() ){
            int len = tags.size();
            for ( int position = 0 ; position < len; position ++ ){
                boolean isSelected = selectPositionBitSet.get(position);
                if ( isSelected && excludePosition != position ){
                    View child = getChildAt(position);
                    child.setSelected(false);
                }
            }
            if ( excludePosition != -1 ){
                selectPositionBitSet.clear();
                selectPositionBitSet.set(excludePosition,true);
            }
        }
    }

    public void clearExcludeSelectedStatus(){
        if ( excludePosition != -1 && selectPositionBitSet.get(excludePosition) ){
            View child = getChildAt(excludePosition);
            child.setSelected(false);
            selectPositionBitSet.set(excludePosition,false);
        }
    }
}
