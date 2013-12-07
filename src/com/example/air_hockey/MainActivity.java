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
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	View handle1, handle2, puck, field, goal1, goal2;
	String msg;
	TextView score1, score2;
	AlertDialog.Builder scoreDialog;
	float xStep, yStep;
	float[] hXStep, hYStep;
	boolean[] touchingHandle;
	boolean[] scoredGoal;
	int[] scoreVal;
	boolean handleCollision;
	float pDiam, pRad, x0, y0, xf, yf, 
	g1_left, g1_right, g2_left, g2_right, 
	initX, initY, hDiam, hRad, pMass, hMass;

	private int puckHandleCollisionResponse(int id, float pMass, float hMass, float cent_x, float cent_y, float h_cent_x, float h_cent_y, float hRad, float pRad, float collisionDist) {
			handleCollision = true;
			if (!touchingHandle[id-1]) {
				xStep = (xStep * (pMass - hMass) + (2 * hMass * hXStep[id-1])) / (pMass + hMass);
				yStep = (yStep * (pMass - hMass) + (2 * hMass * hYStep[id-1])) / (pMass + hMass);
				hXStep[id-1] = (hXStep[id-1] * (hMass - pMass) + (2 * pMass * xStep)) / (pMass + hMass);
				hYStep[id-1] = (hYStep[id-1] * (hMass - pMass) + (2 * pMass * yStep)) / (pMass + hMass);
			} else {
				float n_x = (h_cent_x - cent_x) / collisionDist;
				float n_y = (h_cent_y - cent_y) / collisionDist;
				float p = 2 * (cent_x * n_x + cent_y * n_y) / (pMass + hMass);
				xStep = xStep - p * pMass * n_x - p * hMass * n_x;
				yStep = yStep - p * pMass * n_y - p * hMass * n_y;
			}
			return 0;
	}

	private int puckGoalCollisionResponse(int id, float initX, float initY) {
		scoredGoal[2 - id] = true;
		scoreVal[2 - id]++;
		score2.setText(Integer.toString(scoreVal[2 - id]));
		scoreDialog.setTitle("Player " + Integer.toString(2 - id) + " scored!");
		AlertDialog player2Scored = scoreDialog.create();
		player2Scored.show();
		puck.setX(initX);
		puck.setY(initY);
		yStep = -1 * yStep;
		hXStep[0] = 0;
		hXStep[1] = 0;
		hYStep[0] = 0;
		hYStep[1] = 0;
		return 0;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("RMS","hello world");
		setContentView(R.layout.activity_main);

		xStep = 1;
		yStep = 1;
		hXStep = new float[] { 0, 0 };
		hYStep = new float[] { 0, 0 };
		handle1 = findViewById(R.id.handle1);
		handle2 = findViewById(R.id.handle2);
		puck = findViewById(R.id.puck);
		field = findViewById(R.id.field);
		goal1 = findViewById(R.id.goal1);
		goal2 = findViewById(R.id.goal2);
		score1 = (TextView) findViewById(R.id.score1);
		score2 = (TextView) findViewById(R.id.score2);
		scoredGoal = new boolean[] { false, false };
		handleCollision = false;
		touchingHandle = new boolean[] { false, false };
		scoreVal = new int[] { 2, 2 };
	}
	
	 @Override
	 public void onWindowFocusChanged(boolean focus) {
	    super.onWindowFocusChanged(focus);
			pDiam = puck.getWidth();
			pRad = pDiam / 2;
			x0 = pRad;
			y0 = pRad;
			xf = field.getWidth() - x0;
			yf = field.getHeight() - y0;
			g1_left = goal1.getX();
			g1_right = goal1.getX() + goal1.getWidth();
			g2_left = goal2.getX();
			g2_right = goal2.getX() + goal2.getWidth();
			initX = puck.getX();
			initY = puck.getY();
			hDiam = handle1.getWidth();
			hRad = hDiam / 2;
			pMass = 1;
			hMass = 5;
			score1.setText(Integer.toString(scoreVal[0]));
			score2.setText(Integer.toString(scoreVal[1]));
	 }
	
	@Override
	public void onStart(){
		super.onStart();
		scoreDialog = new AlertDialog.Builder(this);
		scoreDialog.setCancelable(true);
		scoreDialog.setInverseBackgroundForced(true);
		scoreDialog.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
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

							float h1_left = handle1.getX();
							float h1_cent_x = h1_left + hDiam / 2;
							float h1_top = handle1.getY();
							float h1_cent_y = h1_top + hDiam / 2;

							float h2_left = handle2.getX();
							float h2_cent_x = h2_left + hDiam / 2;
							float h2_top = handle2.getY();
							float h2_cent_y = h2_top + hDiam / 2;

							float x = puck.getX();
							float y = puck.getY();
							float cent_x = x + pDiam / 2;
							float cent_y = y + pDiam / 2;

							if (x > xf || x < x0) {
								xStep = -1 * xStep;
							}
							if (y > yf) {
								if (x > g2_left && x < g2_right) {
									Log.d("RMS","goalcollision2");

									puckGoalCollisionResponse(2, initX, initY);

								} else {
									yStep = -1 * yStep;
								}

								if (y < y0) {
									if (x > g1_left && x < g1_right) {
										Log.d("RMS","goalcollision1!");

										puckGoalCollisionResponse(1, initX, initY);
									} else {
										yStep = -1 * yStep;
									}
								}

							}
							
							float dist1 = (float) Math.sqrt(Math.pow(cent_x - h1_cent_x, 2.0) + Math.pow(cent_y - h1_cent_y, 2.0));
							float dist2 = (float) Math.sqrt(Math.pow(cent_x - h2_cent_x, 2.0) + Math.pow(cent_y - h2_cent_y, 2.0));

							if (dist1 <= pRad + hRad){
								puckHandleCollisionResponse(1, pMass, hMass, cent_x, cent_y, h1_cent_x, h1_cent_y, hRad, pRad, dist1);
								Log.d("RMS","collision1!");
							}
							if (dist2 <= pRad + hRad){
								puckHandleCollisionResponse(2, pMass, hMass, cent_x, cent_y, h2_cent_x, h2_cent_y, hRad, pRad, dist2);
								Log.d("RMS","collision2!");
	
							}
							handle1.setX(h1_left + hXStep[0]);
							handle1.setY(h1_top + hYStep[0]);
							handle2.setX(h2_left + hXStep[1]);
							handle2.setY(h2_top + hYStep[1]);
							puck.setX(x + xStep);
							puck.setY(y + yStep);
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 20);
	}
	
	@Override
	public void onResume(){
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
