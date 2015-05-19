package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vincent.massivelist.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends Activity
{
	private ExpandableListView exList;
	private ExAdapter exAdapter;
	
	private EditText textInput;
	private TextView showInputText;
	private LinearLayout showInputLayout;
	
	private EditText numberInput;
	
	private int defualtNum = 500;
	private int number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setTitle(getResources().getString(R.string.app_name) + "_v" + getResources().getString(R.string.Version));
		
		final String testingText = getResources().getString(R.string.TestingText);
		
		createList(defualtNum, testingText);
		
		showInputText = (TextView) findViewById(R.id.showInput);
		showInputLayout = (LinearLayout) findViewById(R.id.showInputLayout);
		
		textInput = (EditText) findViewById(R.id.textInput);
		textInput.setFocusable(true);
		textInput.setFocusableInTouchMode(true);
		textInput.setOnKeyListener(goKey);
		
		numberInput = (EditText) findViewById(R.id.numberInput);
		numberInput.setFocusable(true);
		numberInput.setFocusableInTouchMode(true);
		
		numberInput.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count){
				if (numberInput.getText().toString().isEmpty())
					createList(defualtNum, testingText);
				else {
					number = Integer.parseInt(s.toString());
					createList(number, testingText);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		collapseOther();
	}
	
	public void createList(int count, String listText)
	{
		exList = (ExpandableListView) findViewById(R.id.sampleExList);
		
		List<Map<String, String>> listGroup = new ArrayList<Map<String, String>>();
		List<List<Map<String, String>>> listChild = new ArrayList<List<Map<String, String>>>();
		
		/*--------------�q�o��--------------------------�@����------------------------ */
		
		ArrayList<Integer> ranNumList = new ArrayList<Integer>();		//�Ψ��x�s ranNum
		ArrayList<Integer> ranMultiList = new ArrayList<Integer>();		//�Ψ��x�s ranMulti
		int ranCount = (int) (count * 0.1);								//�s�W�@��int�A�Ȭ��`���(count)��10����1
		Random ran = new Random();
		
		for (int i = 0; i < ranCount; i++)			//����"ranCount"�Ӫ��ü�
		{
			int ranNum = ran.nextInt(count)+1;			//�üƽd��1~count
			int ranMulti = ran.nextInt(16)+5;			//����5~20���üơA�ΨӨM�wsb�n�[�X��
			Log.i("ranNumber",""+ranNum);
			Log.i("ranMultiple",""+ranMulti);
			
			ranNumList.add(ranNum);
			ranMultiList.add(ranMulti);
		}
		
		for (int i = 1; i <= count; i++)
		{
			Map<String, String> listGroupItem = new HashMap<String, String>();
			
			listGroupItem.put("groupSample", listText);		//���`put�i�T�w�����e
			listGroupItem.put("groupNumber", " "+i);
			
			for (int j = 0; j < ranCount; j++)						//�q�o�̶}�lrun "ranCount" �����j��
			{
				if (i == ranNumList.get(j))							//�p�G�Ӧ��� i ����ranNumList�䤤�@�ӼƦr����...
				{													//�ѩ� i �O�q 1 �}�l�hrun�A�ҥH�@�w�O�qranNumList���̤p���ȶ}�l���
					StringBuilder sb = new StringBuilder();			
					Log.i("ranNumberList",""+ranNumList.get(j));	//��Ӧ����쪺��Log�X�ӡA�q�̤p��̤j...
																	//�ҥH�b�o�̤]���K�� ranNumList �����F�Ƨ�...
																	//�N�~�o�{ Bubble Sort ���~���t�@�ӱƧǪk���I
					for (int k = 0; k < ranMultiList.get(j); k++)
					{
						sb.append("This is the Chosen One! ");		//�ݸӦ���ranMultiList���ȬO�h�֡A�Nrun�X��
					}
					listGroupItem.put("groupSample", sb.toString());	//��Ӧ������eput�ihashMap�̡A�л\�쥻put����
				}
			}
			listGroup.add(listGroupItem);
			
			/*----------��o�̡A�ӵ��b�J����Group�ƶq�U�A���ͤ@�w��Ҫ��ü�(�D�X10����1��)�A
			 * �M��Q�襤�����X�ӡA�A��ranMultiList�����ȡA���P���w���ƪ������I-----------I'm fucking Brilliant!-----*/
			
			List<Map<String, String>> listChildItems = new ArrayList<Map<String, String>>();
			Map<String, String> listChildItem = new HashMap<String, String>();
			
			listChildItem.put("childSample", ""+i);
			listChildItems.add(listChildItem);
			listChild.add(listChildItems);
		}
		exAdapter = new ExAdapter(this, listGroup, listChild);
		//exList.setIndicatorBounds(0,100);
		exList.setAdapter(exAdapter);
	}
	
	public void showMemory()
	{
		TextView memTip = (TextView) findViewById(R.id.memTip);
		
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		//int freeMemory = (int) (Runtime.getRuntime().freeMemory() / 1024);
		long availableMem = mi.availMem;
		//String freeMem = String.valueOf(freeMemory) + "MB"; 
		String avaMem = String.valueOf(Formatter.formatFileSize(this, availableMem));
		
		memTip.setText("Memory: " + avaMem);
	}
	
	public void collapseOther()
	{
		exList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				// TODO Auto-generated method stub
				int totalCount = exList.getExpandableListAdapter().getGroupCount();
				for (int i = 0; i < totalCount; i++)
				{
					if (i != groupPosition)
						exList.collapseGroup(i);		//���F�i�}�����Ӧ�m�A��l��m�q�qcollapse!
				}
			}
		});
	}
	
	public void sendClick(View view)
	{
		SmileysParser.init(this);
		SmileysParser parser = SmileysParser.getInstance();
		
		showInputLayout.setVisibility(View.VISIBLE);
		
		String input = textInput.getText().toString();
		showInputText.setText(parser.addSmileySpans(input));
		
		textInput.setText("");
		
		new CountDownTimer(5000, 1000) {
			@Override
			public void onFinish() {
				showInputLayout.setVisibility(View.GONE);
			}
			@Override
			public void onTick(long millisUntilFinished) {}
		}.start();
	}
	
	OnKeyListener goKey = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {
    				sendClick(v);
    				//input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
	
	public void shortMessage(String msg)
	{
		Toast.makeText(MainListActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
}
