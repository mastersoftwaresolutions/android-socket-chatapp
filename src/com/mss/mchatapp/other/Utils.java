package com.mss.mchatapp.other;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mss.mchatapp.MainActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Utils {

	private Context context;
	private SharedPreferences sharedPref;

	private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
	private static final int KEY_MODE_PRIVATE = 0;
	private static final String KEY_SESSION_ID = "sessionId",
			FLAG_MESSAGE = "message";

	public Utils(Context context) {
		this.context = context;
		sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF,
				KEY_MODE_PRIVATE);
	}

	public void storeSessionId(String sessionId) {
		Editor editor = sharedPref.edit();
		editor.putString(KEY_SESSION_ID, sessionId);
		editor.commit();
	}

	public String getSessionId() {
		return sharedPref.getString(KEY_SESSION_ID, null);
	}

	public String getSendMessageJSON(String message) {
		String json = null;

		try {
			JSONObject jObj = new JSONObject();
			jObj.put("flag", FLAG_MESSAGE);
			jObj.put("sessionId", getSessionId());
			jObj.put("message", message);

			json = jObj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	
	
	public List<Message> getChatFromSdCard(Activity  activity,final File yourFile, final String tableName) {
		final List<Message> list = new ArrayList<Message>();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(yourFile, null);
				Cursor cursor1 = db.rawQuery("SELECT DISTINCT * FROM " + tableName, null);
				if (cursor1 != null) {
					if (cursor1.moveToFirst()) {
						do {
							String userName = cursor1.getString(cursor1.getColumnIndex("userName"));
							String message = cursor1.getString(cursor1.getColumnIndex("message"));
							String isself = cursor1.getString(cursor1.getColumnIndex("itself"));
							String color = cursor1.getString(cursor1.getColumnIndex("color"));
							Message msg = new Message();
							msg.setFromName(userName);
							msg.setMessage(message);
							msg.setSelf(Boolean.parseBoolean(isself));
							msg.setColor(color);

							list.add(msg);
							System.out.println("Message: " + msg.getFromName() + " " + msg.getMessage());
						} while (cursor1.moveToNext());
					}
				}
			}
		});
		return list;
	}
	
	public  void copyDatabase( String DATABASE_NAME) {
		String databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
		File f = new File(databasePath);
		OutputStream myOutput = null;
		InputStream myInput = null;
		if (f.exists()) {
			try {
				File directory = new File("/mnt/sdcard/MssChatApp");
				System.out.println("directory.exists(): " + directory.exists());
				if (!directory.exists())
					directory.mkdir();
				myOutput = new FileOutputStream(directory.getAbsolutePath() + "/" + DATABASE_NAME);
				myInput = new FileInputStream(databasePath);

				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}
				myOutput.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (myOutput != null) {
						myOutput.close();
						myOutput = null;
					}
					if (myInput != null) {
						myInput.close();
						myInput = null;
					}
				} catch (Exception e) {
				}
			}
		}
	}

}
