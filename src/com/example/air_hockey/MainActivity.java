package com.example.air_hockey;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	View handle1, handle2, puck, field, goal1, goal2;
	View wholeView;
	String msg;
	TextView score1, score2;
	AlertDialog.Builder scoreDialog;
	float xStep, yStep;
	float dist1,dist2;
	boolean justTouched[];
	float prevHX[], prevHY[];

	float[] hXStep, hYStep;
	boolean[] touchingHandle;
	boolean[] scoredGoal;
	int[] scoreVal;
	final static int MAX_SCORE = 7;
	final static float INITIAL_X_STEP = 1.5f;
	final static float INITIAL_Y_STEP = 1.5f;

	boolean handleCollision, gameFinished;
	float pDiam, pRad, x0, y0, xf, yf, ycent, g1_left, g1_right, g2_left, g2_right, initX, initY, hDiam, hRad, pMass, hMass, pDecel, hDecel;
	float h1_left, h1_top, h1_bottom, h1_right, h1_cent_x, h1_cent_y;
	float h2_left, h2_top, h2_bottom, h2_right, h2_cent_x, h2_cent_y;
	long timeStepMillis;

	//Handles circle-circle collisions when puck collides with handle.
	private int puckHandleCollisionResponse(int id, float pMass, float hMass, float cent_x, float cent_y, float h_cent_x, float h_cent_y, float collisionDist) {
		handleCollision = true;
		if (!touchingHandle[id - 1]) {
			xStep = (xStep * (pMass - hMass) + (2 * hMass * hXStep[id - 1])) / (pMass + hMass);
			yStep = (yStep * (pMass - hMass) + (2 * hMass * hYStep[id - 1])) / (pMass + hMass);
			hXStep[id - 1] = (hXStep[id - 1] * (hMass - pMass) - (2 * pMass * xStep)) / (pMass + hMass);
			hYStep[id - 1] = (hYStep[id - 1] * (hMass - pMass) - (2 * pMass * yStep)) / (pMass + hMass);
		} else {
			float n_x = (h_cent_x - cent_x) / collisionDist;
			float n_y = (h_cent_y - cent_y) / collisionDist;
			float p = 2 * (cent_x * n_x + cent_y * n_y) / (pMass + hMass);
			xStep = xStep - p * pMass * n_x - p * hMass * n_x;
			yStep = yStep - p * pMass * n_y - p * hMass * n_y;
		}
		return 0;
	}

	//Handles puck entering either goal.
	private int puckGoalCollisionResponse(int id, float initX, float initY) {
		scoreVal[id] = scoreVal[id] + 1;
		score2.setText(Integer.toString(scoreVal[id], 10));
		scoreDialog.setTitle("Player " + Integer.toString(id, 10) + " scored!");
		AlertDialog player2Scored = scoreDialog.create();
		player2Scored.show();
		puck.setX(initX);
		puck.setY(initY);
		xStep = 0;
		yStep = 0;
		hXStep[0] = 0;
		hXStep[1] = 0;
		hYStep[0] = 0;
		hYStep[1] = 0;
		return 0;
	}

	//Handles deceleration of freely moving puck and handle. 
	//Called every time step.
	private float decelerate(float step, float decel) {
		if (Math.abs(step) > decel) {
			if (step > 0)
				step = step - decel;
			else
				step = step + decel;
		} else {
			step = 0;
		}
		return step;
	}

	
	//Touch listener for either paddle. Only action_move is important,
	//as we need to adjust paddle position every time this is called.
	private final class MyTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		    // TODO Auto-generated method stub
		    switch(event.getAction() & MotionEvent.ACTION_MASK) {
		    case MotionEvent.ACTION_MOVE:
		   	  if (v.getId() == R.id.handle1){
		   		  touchingHandle[0] = true;
		   	  }else if (v.getId() == R.id.handle2){
		   		  touchingHandle[1] = true;
		   	  }
		        float x = event.getX();
		        float y = event.getY();
		        float xraw = event.getRawX();
		        float yraw = event.getRawY();
		        v.setX(x);
		        v.setY(y-200f);
		        Log.d("RMS", "X = " + x + " Y = " + y + "\n");
		        Log.d("RMS", "Xraw = " + xraw + " Yraw = " + yraw + "\n");
		        return true;
		    case MotionEvent.ACTION_UP:
		   	 Log.d("RMS", "action_up");
		   	  if (v.getId() == R.id.handle1){
		   		  touchingHandle[0] = false;
		   	  }else if (v.getId() == R.id.handle2){
		   		  touchingHandle[1] = false;
		   	  }
		   	  return false;
		    }
		    return true;
		}
}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("RMS", "hello world");
		setContentView(R.layout.activity_main);

		xStep = INITIAL_X_STEP;
		yStep = INITIAL_Y_STEP;
		hXStep = new float[] { 0, 0 };
		hYStep = new float[] { 0, 0 };
		prevHX = new float[]{0,0};
		prevHY = new float[]{0,0};
		handle1 = findViewById(R.id.handle1);
		handle2 = findViewById(R.id.handle2);
		puck = findViewById(R.id.puck);
		field = findViewById(R.id.field);
		goal1 = findViewById(R.id.goal1);
		goal2 = findViewById(R.id.goal2);
		score1 = (TextView) findViewById(R.id.score1);
		score2 = (TextView) findViewById(R.id.score2);
		scoreVal = new int[] { 0, 0 };
		score1.setText(Integer.toString(scoreVal[0]));
		score2.setText(Integer.toString(scoreVal[1]));
		wholeView = findViewById(R.id.wholeView);
		scoredGoal = new boolean[] { false, false };
		handleCollision = false;
		gameFinished = false;
		touchingHandle = new boolean[] { false, false };
		justTouched = new boolean[] {true, true};
		handle1.setOnTouchListener(new MyTouchListener());
		handle2.setOnTouchListener(new MyTouchListener());


	}

	@Override
	public void onWindowFocusChanged(boolean focus) {
		super.onWindowFocusChanged(focus);
		if (!focus)
			return;
		pDiam = puck.getWidth();
		pRad = pDiam / 2;
		x0 = pRad;
		y0 = pRad;
		xf = field.getWidth() - x0;
		yf = field.getHeight() - y0;
		ycent = field.getHeight() / 2f;
		g1_left = goal1.getX();
		g1_right = goal1.getX() + goal1.getWidth();
		g2_left = goal2.getX();
		g2_right = goal2.getX() + goal2.getWidth();
		initX = puck.getX();
		initY = puck.getY();
		hDiam = handle1.getWidth();
		hRad = hDiam / 2f;
		pMass = 1f;
		hMass = 5f;
		dist1 = 10000f;
		dist2 = 10000f;
		timeStepMillis = 10L;
		pDecel = (float)timeStepMillis / 100000f;
		hDecel = (float)pDecel / 5f;
		scoreDialog = new AlertDialog.Builder(this);
		scoreDialog.setCancelable(true);
		scoreDialog.setInverseBackgroundForced(true);
		scoreDialog.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				puck.setX(initX);
				puck.setY(initY);
				xStep = INITIAL_X_STEP;
				yStep = INITIAL_Y_STEP;
				hXStep[0] = 0;
				hXStep[1] = 0;
				hYStep[0] = 0;
				hYStep[1] = 0;
				if (gameFinished){
					scoreVal[0] = 0;
					scoreVal[1] = 0;
					score1.setText("0");
					score2.setText("0");
					gameFinished = false;
				}
			}
		});

		final Handler handler = new Handler();
		Timer timer = new Timer(false);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if (!scoredGoal[1] && !scoredGoal[0]) {
							float prevDist1, prevDist2;
							prevHX[0] = h1_left;
							prevHX[1] = h2_left;
							prevHY[0] = h1_top;
							prevHY[1] = h2_top;

							
							h1_left = handle1.getX();
							h1_right = h1_left + hDiam;
							h1_cent_x = h1_left + hDiam / 2;
							h1_top = handle1.getY();
							h1_bottom = h1_top + hDiam;
							h1_cent_y = h1_top + hDiam / 2;

							h2_left = handle2.getX();
							h2_right = h2_left + hDiam;
							h2_cent_x = h2_left + hDiam / 2;
							h2_top = handle2.getY();
							h2_bottom = h2_top + hDiam;
							h2_cent_y = h2_top + hDiam / 2;

							//Get carry speed if touching handle
							if (touchingHandle[0]){
									hXStep[0] = h1_left - prevHX[0];
									hYStep[0] = h1_left - prevHY[0];
									
							}
							if (touchingHandle[1]){
								hXStep[1] = h1_left - prevHX[1];
								hYStep[1] = h1_left - prevHY[1];
								
						}
							
							
							float x = puck.getX();
							float y = puck.getY();
							float cent_x = x + pDiam / 2;
							float cent_y = y + pDiam / 2;

							// Check if puck hit left or right wall
							if (x > xf || x < x0) {
								xStep = -1f * xStep;
							}

							// Check if puck hit bottom wall and maybe goal. React if
							// goal.
							if (y > yf) {
								if (x > g2_left && x < g2_right) {
									Log.d("RMS", "goalcollision2");
									scoredGoal[1] = true;
								} else {
									yStep = -1f * yStep;
								}
							}

							// Check if puck hit top wall and maybe goal. React if
							// goal.
							if (y < y0) {
								if (x > g1_left && x < g1_right) {
									Log.d("RMS", "goalcollision1!");
									scoredGoal[0] = true;
								} else {
									yStep = -1f * yStep;
								}
							}
							

							
							
							// Get distance between center of puck and centers of each
							// handle
							prevDist1 = dist1;
							prevDist2 = dist2;

							dist1 = (float) Math.sqrt(Math.pow(cent_x - h1_cent_x, 2.0) + Math.pow(cent_y - h1_cent_y, 2.0));
							dist2 = (float) Math.sqrt(Math.pow(cent_x - h2_cent_x, 2.0) + Math.pow(cent_y - h2_cent_y, 2.0));


							// If distance smaller than pRad and hRad, collision has
							// occurred. Take care of it.
							if (dist1 <= pRad + hRad && dist1 <= prevDist1) {
								puckHandleCollisionResponse(1, pMass, hMass, cent_x, cent_y, h1_cent_x, h1_cent_y, dist1);
							}
							if (dist2 <= pRad + hRad && dist2 <= prevDist2) {
								puckHandleCollisionResponse(2, pMass, hMass, cent_x, cent_y, h2_cent_x, h2_cent_y, dist2);

							}

							// Take care of handle collisions with walls and
							// centerline.
							if (h1_left < x0 || h1_right > xf) {
								hXStep[0] = -1f * hXStep[0];
							}
							if (h2_left < x0 || h2_right > xf) {
								hXStep[1] = -1f * hXStep[1];
							}
							if (h1_top < y0) {
								hYStep[0] = -1f * hYStep[0];
							}
							if (h2_bottom > yf) {
								hYStep[1] = -1f * hYStep[1];
							}
							if (h1_bottom > ycent) {
								hYStep[0] = 0f;
							}
							if (h2_top < ycent) {
								hYStep[1] = 0f;
							}

							// Slow down moving handles
							for (int a = 0; a < 2; a++) {
								hXStep[a] = decelerate(hXStep[a], hDecel);
								hYStep[a] = decelerate(hYStep[a], hDecel);
							}

							// Slow down moving puck
							xStep = decelerate(xStep, pDecel);
							yStep = decelerate(yStep, pDecel);

							// Make necessary positional changes after x and y step
							// sizes have been established
							// for each object.
							handle1.setX(h1_left + hXStep[0]);
							handle1.setY(h1_top + hYStep[0]);
							handle2.setX(h2_left + hXStep[1]);
							handle2.setY(h2_top + hYStep[1]);
							puck.setX(x + xStep);
							puck.setY(y + yStep);
							
						}
						else{
							if (scoredGoal[1]){
								scoredGoal[1] = false;
								puckGoalCollisionResponse(0, initX, initY);
								if (scoreVal[1] == MAX_SCORE){
									gameFinished = true;
									scoreDialog.setTitle("Player " + Integer.toString(1, 10) + " wins! Close to start a new game.");
									AlertDialog player2Scored = scoreDialog.create();
									player2Scored.show();
								}
							}
							else{
								scoredGoal[0] = false;
								puckGoalCollisionResponse(1,initX,initY);
								if (scoreVal[0] == MAX_SCORE){
									gameFinished = true;
									scoreDialog.setTitle("Player " + Integer.toString(2, 10) + " wins! Close to start a new game.");
									AlertDialog player2Scored = scoreDialog.create();
									player2Scored.show();
								}
							}
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, timeStepMillis);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
