package com.example.air_hockey;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	View handle1,handle2,puck,field,goal1,goal2;
	String msg;
	TextView score1,score2;
	float xStep,yStep;
	int score1Val,score2Val;
	boolean scoredGoal1,scoredGoal2;
	private android.widget.RelativeLayout.LayoutParams layoutParams;
	private static final String HANDLE_TAG1 = "handle1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		xStep = (float)1.0;
		yStep = (float)1.0;
		handle1 = findViewById(R.id.handle1);
		handle2 = findViewById(R.id.handle2);
		puck = findViewById(R.id.puck);
		field = findViewById(R.id.field);
		goal1 = findViewById(R.id.goal1);
		goal2 = findViewById(R.id.goal2);
		score1 = (TextView)findViewById(R.id.score1);
		score2 = (TextView)findViewById(R.id.score2);
		scoredGoal1 = false;
		scoredGoal2 = false;
		score1Val = 0;
		score2Val = 0;
		score1.setText(Integer.toString(score1Val));
		score2.setText(Integer.toString(score2Val));
		
		final int xcut = puck.getWidth()/2 + 5;
		final int ycut = puck.getHeight()/2 + 5;
		final int x0 = 0+xcut;
		final int xf = field.getWidth()-xcut;
		final int y0 = 0+ycut;
		final int yf = field.getHeight() - ycut;
		final int g1_left = (int) (goal1.getX()-goal1.getWidth()/2);
		final int g1_right = (int) (goal1.getX()+goal1.getWidth()/2);
		final int g2_left = (int) (goal2.getX()-goal2.getWidth()/2);
		final int g2_right = (int) (goal2.getX()+goal2.getWidth()/2);
		
		final Handler handler = new Handler();
		Timer timer = new Timer(false);
		TimerTask timerTask = new TimerTask(){
			@Override
			public void run(){
				handler.post(new Runnable(){
					@Override
					public void run(){
						if (!scoredGoal1 && !scoredGoal2){
						int x = (int)puck.getX();
						int y = (int)puck.getY();

						if (x > xf || x < x0){
							xStep = -1*xStep;
						}
						if (y > yf){
							if (x > g2_left && x < g2_right){
								scoredGoal2 = true;
								score1Val++;
							}else{
								yStep = -1*yStep;
							}
						}
						if (y < y0){
							if (x > g1_left && x < g1_right){
								scoredGoal1 = true;
								score2Val++;
							}else{
								yStep = -1*yStep;
							}
						}
						puck.setX(x+xStep);
						puck.setY(y+yStep);
						}else{
							
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 20);
		

//		handle1.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				ClipData.Item item = new ClipData.Item((CharSequence) v
//						.getTag());
//
//				String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
//				ClipData dragData = new ClipData(v.getTag().toString(),
//						mimeTypes, item);
//
//				// Instantiates the drag shadow builder.
//				View.DragShadowBuilder myShadow = new DragShadowBuilder(
//						handle1);
//
//				// Starts the drag
//				v.startDrag(dragData, // the data to be dragged
//						myShadow, // the drag shadow builder
//						null, // no need to use local data
//						0 // flags (not currently used, set to 0)
//				);
//				return true;
//			}
//		});
//
//		// Create and set the drag event listener for the View
//		handle1.setOnDragListener(new OnDragListener() {
//			@Override
//			public boolean onDrag(View v, DragEvent event) {
//				switch (event.getAction()) {
//				case DragEvent.ACTION_DRAG_STARTED:
//					layoutParams = (RelativeLayout.LayoutParams) v
//							.getLayoutParams();
//					Log.d(msg,
//							"Action is DragEvent.ACTION_DRAG_STARTED");
//					// Do nothing
//					break;
//				case DragEvent.ACTION_DRAG_ENTERED:
//					Log.d(msg,
//							"Action is DragEvent.ACTION_DRAG_ENTERED");
//					int x_cord = (int) event.getX();
//					int y_cord = (int) event.getY();
//					break;
//				case DragEvent.ACTION_DRAG_EXITED:
//					Log.d(msg,
//							"Action is DragEvent.ACTION_DRAG_EXITED");
//					x_cord = (int) event.getX();
//					y_cord = (int) event.getY();
//					layoutParams.leftMargin = x_cord;
//					layoutParams.topMargin = y_cord;
//					v.setLayoutParams(layoutParams);
//					break;
//				case DragEvent.ACTION_DRAG_LOCATION:
//					Log.d(msg,
//							"Action is DragEvent.ACTION_DRAG_LOCATION");
//					x_cord = (int) event.getX();
//					y_cord = (int) event.getY();
//					break;
//				case DragEvent.ACTION_DRAG_ENDED:
//					Log.d(msg,
//							"Action is DragEvent.ACTION_DRAG_ENDED");
//					// Do nothing
//					break;
//				case DragEvent.ACTION_DROP:
//					Log.d(msg, "ACTION_DROP event");
//					// Do nothing
//					break;
//				default:
//					break;
//				}
//				return true;
//			}
//		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
