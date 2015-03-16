package com.mss.mchatapp;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mss.mchatapp.other.Message;
import com.mss.mchatapp.other.Utils;
import com.mss.mchatapp.other.WsConfig;
import com.mss.mchatapp.websocketconnectionpackage.WebSocketClient;

public class MainActivity extends Activity implements OnClickListener {
	private static final String		TAG			= MainActivity.class.getSimpleName();
	private Button					btnSend;
	private EditText				inputMsg;
	private WebSocketClient			client;
	private MessagesListAdapter		adapter;
	private List<Message>			listMessages;
	private ListView				listViewMessages;
	private Utils					utils;
	private final String			dbName		= "MssChatDataBase";
	private static SQLiteDatabase	sqliteDB	= null;
	private final String			tableName	= "mssChatTable";
	private String					name		= null;
	File							yourFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		yourFile = new File(Environment.getExternalStorageDirectory(), "/MssChatApp/" + dbName);
		initUI();
		adapter = new MessagesListAdapter(MainActivity.this, listMessages);
		listViewMessages.setAdapter(adapter);
		btnSend.setOnClickListener(this);
		createWebSocketClient();

	}

	/**
	 * Initialize UI Part
	 */
	private void initUI() {
		btnSend = (Button) findViewById(R.id.btnSend);
		inputMsg = (EditText) findViewById(R.id.inputMsg);
		listViewMessages = (ListView) findViewById(R.id.list_view_messages);
		utils = new Utils(getApplicationContext());
		Intent i = getIntent();
		name = i.getStringExtra("name");
		utils.storeSessionId(name);
		listMessages = new ArrayList<Message>();

		getChatFromDatabase();
		if (yourFile.exists() && listMessages.size() == 0) {
			showAlertDialog();
		}
	}

	@Override
	public void onClick(View v) {
		sendMessageToServer(inputMsg.getText().toString());
		inputMsg.setText("");
	}

	/**
	 * This method responsible for display chat from database
	 */
	private void getChatFromDatabase() {
		sqliteDB = null;
		sqliteDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
		sqliteDB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName
				+ " (userName VARCHAR, message VARCHAR, itself VARCHAR, color VARCHAR);");
		Cursor cursor = sqliteDB.rawQuery("SELECT DISTINCT * FROM " + tableName, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					String userName = cursor.getString(cursor.getColumnIndex("userName"));
					String message = cursor.getString(cursor.getColumnIndex("message"));
					String isself = cursor.getString(cursor.getColumnIndex("itself"));
					String color = cursor.getString(cursor.getColumnIndex("color"));
					Message msg = new Message();
					msg.setFromName(userName);
					msg.setMessage(message);
					msg.setSelf(Boolean.parseBoolean(isself));
					msg.setColor(color);

					listMessages.add(msg);
				} while (cursor.moveToNext());
			}
		}
	}

	/**
 * 
 */
	void showAlertDialog() {
		final List<Message> list = utils.getChatFromSdCard(MainActivity.this, yourFile, tableName);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
		alertDialog.setTitle("Mss Chat App");
		alertDialog.setMessage("Do you want to restore your previous chat..?");
		alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listMessages.addAll(list);
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
		alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	/**
	 * Creating web socket client. This will have callback methods
	 * */
	private void createWebSocketClient() {
		client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET), new WebSocketClient.Listener() {
			@Override
			public void onConnect() {
			}

			/**
			 * On receiving the message from web socket server
			 * */
			@Override
			public void onMessage(String message) {
				Log.d(TAG, String.format("Got string message! %s", message));
				System.out.println("Message Recieved: " + message);
				parseMessage(message);

			}

			@Override
			public void onMessage(byte[] data) {
				Log.d(TAG, String.format("Got binary message! %s", bytesToHex(data)));
				parseMessage(bytesToHex(data));
			}

			/**
			 * Called when the connection is terminated
			 * */
			@Override
			public void onDisconnect(int code, String reason) {
				// String message = String.format(Locale.US,
				// "Disconnected! Code: %d Reason: %s", code, reason);
				// showToast(message);
			}

			@Override
			public void onError(Exception error) {
				Log.e(TAG, "Error! : " + error);
				// showToast("Error! : " + error);
			}
		}, null);

		client.connect();
	}

	/**
	 * Method to send message to web socket server
	 * */
	private void sendMessageToServer(String message) {
		if (client != null && client.isConnected()) {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("message", message);
				jsonObj.put("name", name);
				jsonObj.put("color", "#eb543b");
			} catch (JSONException e) {
				e.printStackTrace();

			}
			client.send(jsonObj.toString());
		}
	}

	private void parseMessage(final String msg) {

		try {
			JSONObject jObj = new JSONObject(msg);
			String flag = "nothing";
			flag = jObj.getString("type");
			if (flag.equals("usermsg")) {
				String fromName;
				String message = jObj.getString("message");
				String color = jObj.getString("color");
				boolean isSelf = true;

				if (!name.equals(jObj.getString("name"))) {
					fromName = jObj.getString("name");
					isSelf = false;
				} else {
					fromName = name;
					isSelf = true;
				}
				Message m = new Message(fromName, message, isSelf, color);
				appendMessage(m);
			} else {
				String fromName = name;
				String message = jObj.getString("message");
				boolean isSelf = true;
				Message m = new Message(fromName, message, isSelf, "#eb543b");
				appendMessage(m);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (client != null & client.isConnected()) {
			client.disconnect();
		}
	}

	/**
	 * Appending message to list view
	 * */
	private void appendMessage(final Message m) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listMessages.add(m);
				try {
					for (Message ver : listMessages) {
						sqliteDB.execSQL("INSERT INTO " + tableName + " Values ('" + ver.getFromName() + "','"
								+ ver.getMessage() + "','" + ver.isSelf() + "','" + ver.getColor() + "');");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				utils.copyDatabase(dbName);
				adapter.notifyDataSetChanged();
				playBeep();
			}
		});
	}

	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			}
		});

	}

	/**
	 * Plays device's default notification sound
	 * */
	public void playBeep() {
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final protected static char[]	hexArray	= "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
