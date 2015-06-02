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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ActionMode.Callback;
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
		private static boolean motionCanceled;
		
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
		@SuppressLint("NewApi") @Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
		{
			int action = event.getAction();		// ���o�ƥ�����
			
			if (action == MotionEvent.ACTION_CANCEL)
			{
				Log.d("MotionEvent", "Motion Canceled!");
				return false;
			}
			
			if (action == MotionEvent.ACTION_UP)
			{
				if (motionCanceled) {
					Log.d("MotionEvent", "Motion Canceled!");
					return false;
				}
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
					String linkedText = widget.getText().toString()
							.substring(buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
					
					if (widget instanceof LinkTextView)		// �p�G����OLinkTextView
					{
						((LinkTextView) widget).linkHit = true;
					}
					if (action == MotionEvent.ACTION_UP)	// ��}��
					{
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));	// ��ܳs��
						
						if (linkedText.contains(".png") || linkedText.contains(".jpg") || 
								linkedText.contains(".gif") || linkedText.contains("bmp"))
						{
							Context context = widget.getContext();
							((MainListActivity) context).popImageWindow(linkedText);
						}
						else
							link[0].onClick(widget);	// �}�ҳs��
					}
					/*	
					 * ---4.2�H�W�������A�u�|������@���ʧ@�A�]���Y���P�_ ACTION_DOWN�A�N�P�_���� ACTION_UP �F!!!...
					 * 
					 * �o���D�s���O.......������~~~~~~~~�i�c!!�Q���D!!
					 * 
					else if (action == MotionEvent.ACTION_DOWN)	// ���U��
					{
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
						Log.i("LINK EVENT!!", linkedText);
						//link[0].onClick(widget);
					}
					*/
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
			if (action == MotionEvent.ACTION_DOWN)
			{
				Log.i("MotionEvent", "Down Down Down~~");
				motionCanceled = false;
				//tv = widget;
				//widget.setCustomSelectionActionModeCallback(mySelect);
			}
			return Touch.onTouchEvent(widget, buffer, event);
		}
		
		private TextView tv;
		
		@SuppressLint("NewApi")
		Callback mySelect = new Callback()
		{
			  @Override
		        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		            // Remove the "select all" option
		            menu.removeItem(android.R.id.selectAll);
		            // Remove the "cut" option
		            menu.removeItem(android.R.id.cut);
		            // Remove the "copy all" option
		            menu.removeItem(android.R.id.copy);
		            return true;
		        }

		        @Override
		        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		            // Called when action mode is first created. The menu supplied
		            // will be used to generate action buttons for the action mode

		            // Here is an example MenuItem
		            menu.add(0, 1, 0, "Definition").setIcon(android.R.drawable.ic_btn_speak_now);
		            return true;
		        }

		        @Override
		        public void onDestroyActionMode(ActionMode mode) {
		            // Called when an action mode is about to be exited and
		            // destroyed
		        }

		        @Override
		        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		            switch (item.getItemId()) {
		                case 1:
		                    int min = 0;
		                    int max = tv.getText().length();
		                    if (tv.isFocused()) {
		                        final int selStart = tv.getSelectionStart();
		                        final int selEnd = tv.getSelectionEnd();

		                        min = Math.max(0, Math.min(selStart, selEnd));
		                        max = Math.max(0, Math.max(selStart, selEnd));
		                    }
		                    // Perform your definition lookup with the selected text
		                    final CharSequence selectedText = tv.getText().subSequence(min, max);
		                    // Finish and close the ActionMode
		                    Log.i("TextSelected", selectedText.toString());
		                    mode.finish();
		                    return true;
		                default:
		                    break;
		            }
		            return false;
		        }
		};
		public static void cancelMotion()
		{
			motionCanceled = true;
		}
	}
	
}