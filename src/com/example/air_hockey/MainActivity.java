package com.example.air_hockey;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {

	View handle1, handle2, puck, field, goal1, goal2;
	View wholeView;
	String msg;
	TextView score1, score2;
	AlertDialog.Builder scoreDialog;
	float xStep, yStep;
	float dist1, dist2;
	float mStatusAndActionBarOffset;
	float[] prevHX, prevHY, hXStep, hYStep, hXStepTouch, hYStepTouch;
	boolean[] touchingHandle, scoredGoal;
	int[] scoreVal;
	final static int MAX_SCORE = 7;
	final static float INITIAL_X_STEP = 0.5f;
	final static float INITIAL_Y_STEP = 0.5f;
	final static long TIMESTEP = 10L;
	final static float PRAD = 15f;
	final static float HRAD = 20f;
	final static float PDIAM = 30f;
	final static float HDIAM = 40f;
	final static float PMASS = 1f;
	final static float HMASS = 5f;
	final static float PDECEL = 0.0001f;
	final static float HDECEL = 0.00002f;
	static float FIELD_LEFT = 0f;
	static float FIELD_TOP = 0f;
	static float FIELD_RIGHT;
	static float FIELD_BOTTOM;
	static float FIELD_CENTER;
	static float GOAL_LEFT;
	static float GOAL_RIGHT;
	boolean gameFinished;
	float initX, initY;
	float h1_left, h1_top, h1_bottom, h1_right, h1_cent_x, h1_cent_y;
	float h2_left, h2_top, h2_bottom, h2_right, h2_cent_x, h2_cent_y;
	float puck_left, puck_top, puck_bottom, puck_right, puck_cent_x, puck_cent_y;

	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        // ........
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	// Handles circle-circle collisions when puck collides with handle.
	private int puckHandleCollisionResponse(int id, float cent_x, float cent_y, float h_cent_x, float h_cent_y, float collisionDist) {
		float tMass = PMASS + HMASS;
		float dMass = PMASS - HMASS;
		float ndMass = HMASS - PMASS;
		if (!touchingHandle[id]) {
			float xStepTemp = xStep;
			float yStepTemp = yStep;
			xStep = (xStep * dMass + (2 * HMASS * hXStep[id])) / tMass;
			yStep = (yStep * dMass + (2 * HMASS * hYStep[id])) / tMass;
			hXStep[id] = (hXStep[id] * ndMass + (2 * PMASS * xStepTemp)) / tMass;
			hYStep[id] = (hYStep[id] * ndMass + (2 * PMASS * yStepTemp)) / tMass;
		} else {
			xStep = (xStep * dMass + (2 * HMASS * hXStepTouch[id])) / tMass;
			yStep = (yStep * dMass + (2 * HMASS * hYStepTouch[id])) / tMass;

		}

		return 0;
	}
	


	// Handles puck entering either goal.
	private int puckGoalCollisionResponse(int id, float initX, float initY) {
		scoreVal[id] = scoreVal[id] + 1;
		if (id == 0)
			score1.setText(Integer.toString(scoreVal[id], 10));
		else
			score2.setText(Integer.toString(scoreVal[id], 10));
		scoreDialog.setTitle("Player " + Integer.toString(id+1, 10) + " scored!");
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
		hXStepTouch[0] = 0;
		hYStepTouch[0] = 0;
		hXStepTouch[1] = 0;
		hYStepTouch[1] = 0;
		return 0;
	}

	// Handles deceleration of freely moving puck and handle.
	// Called every time step.
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

	// Touch listener for either paddle. Only action_move is important,
	// as we need to adjust paddle position every time this is called.
	private final class MyTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_MOVE:
				if (v.getId() == R.id.handle1) {
					touchingHandle[0] = true;
				} else if (v.getId() == R.id.handle2) {
					touchingHandle[1] = true;
				}
				float xraw = event.getRawX();
				float yraw = event.getRawY();
				yraw -= mStatusAndActionBarOffset;
				v.setX(xraw - v.getWidth() / 2);
				v.setY(yraw - v.getWidth() / 2);
				break;
			case MotionEvent.ACTION_UP:
				Log.d("RMS", "action_up");
				if (v.getId() == R.id.handle1) {
					touchingHandle[0] = false;
					hXStep[0] = 0f;
					hYStep[0] = 0f;
				} else if (v.getId() == R.id.handle2) {
					touchingHandle[1] = false;
					hXStep[1] = 0f;
					hYStep[1] = 0f;
				}
				break;
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
		hXStepTouch = new float[] { 0, 0 };
		hYStepTouch = new float[] { 0, 0 };
		prevHX = new float[] { 0, 0 };
		prevHY = new float[] { 0, 0 };
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
		gameFinished = false;
		touchingHandle = new boolean[] { false, false };
		handle1.setOnTouchListener(new MyTouchListener());
		handle2.setOnTouchListener(new MyTouchListener());

		goal1.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				goal1.getViewTreeObserver().removeOnPreDrawListener(this);
				ActionBar actionBar = getActionBar();

				Rect rectgle = new Rect();
				Window window = getWindow();
				window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
				int statusBarHeight = rectgle.top;
				int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
				int titleBarHeight = contentViewTop - statusBarHeight;

				mStatusAndActionBarOffset = actionBar.getHeight() + titleBarHeight;
				return true;
			}
		});
	}

	// Called when the window comes into focus. This method is where all
	// calculations associated with view dimensions and positions are calculated.
	@Override
	public void onWindowFocusChanged(boolean focus) {
		super.onWindowFocusChanged(focus);
		if (!focus)
			return;

		// Declare a bunch of variables we are going to need for physics
		FIELD_RIGHT = field.getWidth() - 20f;
		FIELD_BOTTOM = field.getHeight() - 20f;
		FIELD_CENTER = field.getHeight() / 2f;
		GOAL_LEFT = goal1.getX();
		GOAL_RIGHT = goal1.getX() + goal1.getWidth();
		initX = puck.getX();
		initY = puck.getY();
		dist1 = 10000f;
		dist2 = 10000f;

		// Create dialog that will be used for scores and for declaring a winner
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
				if (gameFinished) {
					scoreVal[0] = 0;
					scoreVal[1] = 0;
					score1.setText("0");
					score2.setText("0");
					gameFinished = false;
				}
			}
		});

		// Create the timer, and set it to tick every 10 milliseconds
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
							h1_right = h1_left + HDIAM;
							h1_cent_x = h1_left + HRAD;
							h1_top = handle1.getY();
							h1_bottom = h1_top + HDIAM;
							h1_cent_y = h1_top + HRAD;
							h2_left = handle2.getX();
							h2_right = h2_left + HDIAM;
							h2_cent_x = h2_left + HRAD;
							h2_top = handle2.getY();
							h2_bottom = h2_top + HDIAM;
							h2_cent_y = h2_top + HRAD;
							puck_left = puck.getX();
							puck_right = puck_left + PDIAM;
							puck_cent_x = puck_left + PRAD;
							puck_top = puck.getY();
							puck_bottom = puck_top + PDIAM;
							puck_cent_y = puck_top + PRAD;

							// Get carry speed if touching handle
							if (touchingHandle[0]) {
								hXStepTouch[0] = (h1_left - prevHX[0])/10f;
								hYStepTouch[0] = (h1_top - prevHY[0])/10f;

							}
							if (touchingHandle[1]) {
								hXStepTouch[1] = (h2_left - prevHX[1])/10f;
								hYStepTouch[1] = (h2_top - prevHY[1])/10f;

							}

							// Check if puck hit left or right wall
							if (puck_right > FIELD_RIGHT || puck_left < FIELD_LEFT) {
								xStep = -1f * xStep;
							}

							// Check if puck hit bottom wall and maybe goal. React if
							// goal.
							if (puck_bottom > FIELD_BOTTOM) {
								if (puck_left > GOAL_LEFT && puck_right < GOAL_RIGHT) {
									scoredGoal[0] = true;
								} else {
									yStep = -1f * yStep;
								}
							}

							// Check if puck hit top wall and maybe goal. React if
							// goal.
							if (puck_top < FIELD_TOP) {
								if (puck_left > GOAL_LEFT && puck_right < GOAL_RIGHT) {
									scoredGoal[1] = true;
								} else {
									yStep = -1f * yStep;
								}
							}

							// Get distance between center of puck and centers of each
							// handle
							prevDist1 = dist1;
							prevDist2 = dist2;

							dist1 = (float) Math.sqrt(Math.pow(puck_cent_x - h1_cent_x, 2.0) + Math.pow(puck_cent_y - h1_cent_y, 2.0));
							dist2 = (float) Math.sqrt(Math.pow(puck_cent_x - h2_cent_x, 2.0) + Math.pow(puck_cent_y - h2_cent_y, 2.0));

							// If distance smaller than PRAD and HRAD, collision has
							// occurred. Take care of it.
							if (dist1 <= PRAD + HRAD && dist1 <= prevDist1) {
								puckHandleCollisionResponse(0, puck_cent_x, puck_cent_y, h1_cent_x, h1_cent_y, dist1);
							}
							if (dist2 <= PRAD + HRAD && dist2 <= prevDist2) {
								puckHandleCollisionResponse(1, puck_cent_x, puck_cent_y, h2_cent_x, h2_cent_y, dist2);

							}

							// Take care of handle collisions with walls and
							// centerline.
							if (h1_left < FIELD_LEFT || h1_right > FIELD_RIGHT) {
								hXStep[0] = -1f * hXStep[0];
							}
							if (h2_left < FIELD_LEFT || h2_right > FIELD_RIGHT) {
								hXStep[1] = -1f * hXStep[1];
							}
							if (h1_top < FIELD_TOP) {
								hYStep[0] = -1f * hYStep[0];
							}
							if (h2_bottom > FIELD_BOTTOM) {
								hYStep[1] = -1f * hYStep[1];
							}
							if (h1_bottom > FIELD_CENTER) {
								hYStep[0] = 0f;
							}
							if (h2_top < FIELD_CENTER) {
								hYStep[1] = 0f;
							}

							// Slow down moving handles
							if (!touchingHandle[0]) {
								hXStep[0] = decelerate(hXStep[0], HDECEL);
								hYStep[0] = decelerate(hYStep[0], HDECEL);
							}
							if (!touchingHandle[1]) {
								hXStep[1] = decelerate(hXStep[1], HDECEL);
								hYStep[1] = decelerate(hYStep[1], HDECEL);
							}

							// Slow down moving puck
							xStep = decelerate(xStep, PDECEL);
							yStep = decelerate(yStep, PDECEL);

							// Make necessary positional changes after x and y step
							// sizes have been established
							// for each object.
							if (!touchingHandle[0]) {
								handle1.setX(h1_left + hXStep[0]);
								handle1.setY(h1_top + hYStep[0]);
							}
							if (!touchingHandle[0]) {
								handle2.setX(h2_left + hXStep[1]);
								handle2.setY(h2_top + hYStep[1]);
							}
							puck.setX(puck_left + xStep);
							puck.setY(puck_top + yStep);

						} else {
							// If player 1 scored do necessary stuff.
							if (scoredGoal[1]) {
								scoredGoal[1] = false;
								puckGoalCollisionResponse(1, initX, initY);
								if (scoreVal[1] == MAX_SCORE) {
									gameFinished = true;
									scoreDialog.setTitle("Player " + Integer.toString(2, 10) + " wins! Close to start a new game.");
									AlertDialog player2Scored = scoreDialog.create();
									player2Scored.show();
								}
							} if (scoredGoal[0]) {
								// If player 2 scored do necessary stuff.
								scoredGoal[0] = false;
								puckGoalCollisionResponse(0, initX, initY);
								if (scoreVal[0] == MAX_SCORE) {
									gameFinished = true;
									scoreDialog.setTitle("Player " + Integer.toString(1, 10) + " wins! Close to start a new game.");
									AlertDialog player2Scored = scoreDialog.create();
									player2Scored.show();
								}
							}
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, TIMESTEP);
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
