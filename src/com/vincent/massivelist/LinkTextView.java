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
	private boolean linkHit;	// 是否為按下連結
	
	// -----建構子-----
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

		// -----物件方法-----
		/**
		 * 覆寫onTouchEvent，使其不會永遠傳回true，若為true，則無法將touch事件傳出給上層的View。
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
	 * 設定MovementMethod之後，TextView會成為Focusable，所以LinkTextView覆寫此方法，永遠傳回false。
	 */
	
	@Override
	public boolean hasFocusable()
	{
		return false;
	}
	
	/**
	 * 繼承LinkMovementMethod的LinkTextViewMovementMethod，將會針對連結點擊進行處理，
	 * 讓LinkTextView知道目前點擊是否為連結點擊。
	 */
	
	public static class LinkTextViewMovementMethod extends LinkMovementMethod
	{
		private static LinkTextViewMovementMethod sInstance;	// 儲存唯一的實體參考
		
		// -----物件方法-----
		/**
		 * 取得LinkTextViewMovementMethod的唯一實體參考。
		 * 
		 * @return
		 */
		public static LinkTextViewMovementMethod getInstance()
		{
			if (sInstance == null)
				sInstance = new LinkTextViewMovementMethod();	// 建立新的實體
			return sInstance;
		}
		
		/**
		 * 覆寫觸控事件，分辨出是否為連結的點擊。
		 */
		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
		{
			int action = event.getAction();		// 取得事件類型
			
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
				
				if (link.length != 0)	// 是連結點擊
				{
					String linkedText = widget.getText().toString()
							.substring(buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
					
					if (widget instanceof LinkTextView)		// 如果實體是LinkTextView
					{
						((LinkTextView) widget).linkHit = true;
					}
					if (action == MotionEvent.ACTION_UP)	// 放開時
					{
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));	// 選擇連結
						
						if (linkedText.contains(".png") || linkedText.contains(".jpg") || 
								linkedText.contains(".gif") || linkedText.contains("bmp"))
						{
							Context context = widget.getContext();
							((MainListActivity) context).popImageWindow(linkedText);
						}
						else
							link[0].onClick(widget); // 開啟連結
					}
					/*	
					 * ---4.2以上的版本，只會偵測到一次動作，因此若有判斷 ACTION_DOWN，就判斷不到 ACTION_UP 了!!!...
					 * 
					 * 到底是什麼原因...這BUG究竟是.....目前我依然不懂阿~~~~~可惡!想知道!
					 * 
					else if (action == MotionEvent.ACTION_DOWN)	// 按下時
					{
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));	
						Log.i("LINK EVENT!!", linkedText);
						//link[0].onClick(widget);
					}
					*/
					Log.i("LINK EVENT!!", "Got Click!!!!");
					return true;
				}
				else	// 不是連結點擊
				{
					Log.i("LINK EVENT!!", "It's NOT a Link!!!");
					if (widget instanceof LinkTextView)		// 如果實體是LinkTextView
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