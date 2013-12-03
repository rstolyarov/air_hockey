package com.example.air_hockey;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MainActivity extends Activity {

	View handle1;
	View handle2;
	View puck;
	View field;
	String msg;
	private android.widget.RelativeLayout.LayoutParams layoutParams;
	private static final String HANDLE_TAG1 = "handle1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handle1 = findViewById(R.id.handle1);
	      handle1.setTag(HANDLE_TAG1);

		handle2 = findViewById(R.id.handle2);
		puck = findViewById(R.id.puck);
		field = findViewById(R.id.field);
		handle1.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipData.Item item = new ClipData.Item((CharSequence) v
						.getTag());

				String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
				ClipData dragData = new ClipData(v.getTag().toString(),
						mimeTypes, item);

				// Instantiates the drag shadow builder.
				View.DragShadowBuilder myShadow = new DragShadowBuilder(
						handle1);

				// Starts the drag
				v.startDrag(dragData, // the data to be dragged
						myShadow, // the drag shadow builder
						null, // no need to use local data
						0 // flags (not currently used, set to 0)
				);
				return true;
			}
		});

		// Create and set the drag event listener for the View
		handle1.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch (event.getAction()) {
				case DragEvent.ACTION_DRAG_STARTED:
					layoutParams = (RelativeLayout.LayoutParams) v
							.getLayoutParams();
					Log.d(msg,
							"Action is DragEvent.ACTION_DRAG_STARTED");
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_ENTERED:
					Log.d(msg,
							"Action is DragEvent.ACTION_DRAG_ENTERED");
					int x_cord = (int) event.getX();
					int y_cord = (int) event.getY();
					break;
				case DragEvent.ACTION_DRAG_EXITED:
					Log.d(msg,
							"Action is DragEvent.ACTION_DRAG_EXITED");
					x_cord = (int) event.getX();
					y_cord = (int) event.getY();
					layoutParams.leftMargin = x_cord;
					layoutParams.topMargin = y_cord;
					v.setLayoutParams(layoutParams);
					break;
				case DragEvent.ACTION_DRAG_LOCATION:
					Log.d(msg,
							"Action is DragEvent.ACTION_DRAG_LOCATION");
					x_cord = (int) event.getX();
					y_cord = (int) event.getY();
					break;
				case DragEvent.ACTION_DRAG_ENDED:
					Log.d(msg,
							"Action is DragEvent.ACTION_DRAG_ENDED");
					// Do nothing
					break;
				case DragEvent.ACTION_DROP:
					Log.d(msg, "ACTION_DROP event");
					// Do nothing
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
