package com.vincent.massivelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkTextView extends TextView
{
	private boolean linkHit;	// �O�_�����U�s��
	
	// -----�غc�l-----
	public LinkTextView(Context context)
	{
		super(context);
	}
	
	public LinkTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public LinkTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

		// -----�����k-----
		/**
		 * �мgonTouchEvent�A�Ϩ䤣�|�û��Ǧ^true�A�Y��true�A�h�L�k�Ntouch�ƥ�ǥX���W�h��View�C
		 *
		 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		return linkHit;
	}
	
	/**
	 * �]�wMovementMethod����ATextView�|����Focusable�A�ҥHLinkTextView�мg����k�A�û��Ǧ^false�C
	 */
	
	@Override
	public boolean hasFocusable()
	{
		return false;
	}
	
	/**
	 * �~��LinkMovementMethod��LinkTextViewMovementMethod�A�N�|�w��s���I���i��B�z�A
	 * ��LinkTextView���D�ثe�I���O�_���s���I���C
	 */
	
	public static class LinkTextViewMovementMethod extends LinkMovementMethod
	{
		private static LinkTextViewMovementMethod sInstance;	// �x�s�ߤ@������Ѧ�
		
		// -----�����k-----
		/**
		 * ���oLinkTextViewMovementMethod���ߤ@����ѦҡC
		 * 
		 * @return
		 */
		public static LinkTextViewMovementMethod getInstance()
		{
			if (sInstance == null)
				sInstance = new LinkTextViewMovementMethod();	// �إ߷s������
			return sInstance;
		}
		
		/**
		 * �мgĲ���ƥ�A����X�O�_���s�����I���C
		 */
		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
		{
			int action = event.getAction();		// ���o�ƥ�����
			
			if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN)
			{
				int x = (int) event.getX();
				int y = (int) event.getY();
				
				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();
 
				x += widget.getScrollX();
				y += widget.getScrollY();
 
				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);
 
				ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
				
				if (link.length != 0)	// �O�s���I��
				{
					if (widget instanceof LinkTextView)		// �p�G����OLinkTextView
					{
						((LinkTextView) widget).linkHit = true;
					}
					if (action == MotionEvent.ACTION_UP)	// ��}��
					{
						link[0].onClick(widget); // �}�ҳs��
					}
					else if (action == MotionEvent.ACTION_DOWN)	// ���U��
					{
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));	// ��ܳs��
						
						String linkedText = widget.getText().toString()
								.substring(buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
						
						if (linkedText.contains(".png") || linkedText.contains(".jpg") || 
								linkedText.contains(".gif") || linkedText.contains("bmp"))
						{
							
						}
						//link[0].onClick(widget);
					}
					Log.i("LINK EVENT!!", "Got Click!!!!");
					return true;
				}
				else	// ���O�s���I��
				{
					Log.i("LINK EVENT!!", "It's NOT a Link!!!");
					if (widget instanceof LinkTextView)		// �p�G����OLinkTextView
					{
						((LinkTextView) widget).linkHit = false;
					}
					Selection.removeSelection(buffer);
					Touch.onTouchEvent(widget, buffer, event);
					return false;
				}
			}
			return Touch.onTouchEvent(widget, buffer, event);
		}
	}
}