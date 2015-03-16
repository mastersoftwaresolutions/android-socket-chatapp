package com.mss.mchatapp;

import com.mss.mchatapp.other.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NameActivity extends Activity {

	private Button btnJoin;
	private EditText txtName;
	private Utils utils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_name);

		btnJoin = (Button) findViewById(R.id.btnJoin);
		txtName = (EditText) findViewById(R.id.name);
		utils = new Utils(getApplicationContext());
		// Hiding the action bar
		getActionBar().hide();
		
		if(utils.getSessionId()!=null){
			Intent intent = new Intent(NameActivity.this,
					MainActivity.class);
			intent.putExtra("name", utils.getSessionId());
			startActivity(intent);
			finish();
		}

		btnJoin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (txtName.getText().toString().trim().length() > 0) {
					String name = txtName.getText().toString().trim();
					Intent intent = new Intent(NameActivity.this,
							MainActivity.class);
					intent.putExtra("name", name);
					startActivity(intent);
					finish();

				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter your name", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
