package com.vincent.massivelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.widget.EditText;

public class myTextView extends EditText
{
	private int off;
	
	public myTextView(Context context)
	{
		super(context);
		initialize();
	}
	
	public myTextView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.style.TextAppearance);
    }

    public myTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	private void initialize()
	{
		setBackgroundColor(Color.WHITE);
	}
	
	@Override
	protected void onCreateContextMenu(ContextMenu menu)
	{
		
	}
	
	@Override
	protected boolean getDefaultEditable()
	{
		return false;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		Layout layout = getLayout();
		int line = 0;
		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			line = layout.getLineForVertical(getScrollY() + (int) event.getY());
			off = layout.getOffsetForHorizontal(line, (int) event.getX());
			Selection.setSelection(getEditableText(), off);
			break;
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			line = layout.getLineForVertical(getScrollY() + (int) event.getY());
			int curOff = layout.getOffsetForHorizontal(line, (int) event.getX());
			Selection.setSelection(getEditableText(), off, curOff);
			break;
		}
		return true;
	}
	
}
