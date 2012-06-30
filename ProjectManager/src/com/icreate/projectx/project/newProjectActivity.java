package com.icreate.projectx.project;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.icreate.projectx.R;
import com.icreate.projectx.homeActivity;
import com.icreate.projectx.datamodel.ProjectxGlobalState;
import com.icreate.projectx.datepicker.DateSlider;
import com.icreate.projectx.datepicker.DefaultDateSlider;

public class newProjectActivity extends Activity implements AdapterView.OnItemSelectedListener {

	protected static final String WIDGET_REQ_CODE = null;
	private final int subActivityID = 23987;

	private EditText nameTextBox, aboutTextBox, deadlineTextBox;
	private TextView newProjectDeadlinetext, newProjectNametext, newProjectAbouttext, newProjectMemberstext, logoText;
	private Spinner moduleTextBox;
	private Button createProjectButton, addMemberButton;
	private ListView selectedMemberList;
	private ImageButton logoButton;
	private ProgressDialog dialog;

	private final List<String> moduleList = new ArrayList<String>();
	private final ArrayList<String> members = new ArrayList<String>();
	private final ArrayList<String> memberid = new ArrayList<String>();
	private final List<String> moduleId = new ArrayList<String>();

	private ArrayAdapter<String> dataAdapter;

	private Activity currentActivity;
	private Context cont;

	static final int DEFAULTDATESELECTOR_ID = 0;

	Intent addMemberIntent;
	String text;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		/*
		 * StrictMode.setThreadPolicy(new
		 * StrictMode.ThreadPolicy.Builder().detectAll
		 * ().penaltyLog().penaltyDeath().build()); StrictMode.setVmPolicy(new
		 * StrictMode
		 * .VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
		 */

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.newproject);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.logo1);

		cont = this;
		currentActivity = this;
		addMemberIntent = new Intent(cont, AddMemberActivity.class);
		dialog = new ProgressDialog(cont);

		Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");

		moduleTextBox = (Spinner) findViewById(R.id.moduleTextBox);
		nameTextBox = (EditText) findViewById(R.id.nameTextBox);
		aboutTextBox = (EditText) findViewById(R.id.aboutTextBox);
		deadlineTextBox = (EditText) findViewById(R.id.deadlineTextBox);
		createProjectButton = (Button) findViewById(R.id.loginButton);
		addMemberButton = (Button) findViewById(R.id.addMemberButton);
		selectedMemberList = (ListView) findViewById(R.id.selectedMemberList);

		newProjectDeadlinetext = (TextView) findViewById(R.id.newProjectDeadlinetext);
		newProjectAbouttext = (TextView) findViewById(R.id.newProjectAbouttext);
		newProjectNametext = (TextView) findViewById(R.id.newProjectNametext);
		newProjectMemberstext = (TextView) findViewById(R.id.newProjectMemberstext);
		logoText = (TextView) findViewById(R.id.logoText);
		logoButton = (ImageButton) findViewById(R.id.logoImageButton);

		newProjectDeadlinetext.setTypeface(font);
		newProjectAbouttext.setTypeface(font);
		newProjectNametext.setTypeface(font);
		newProjectMemberstext.setTypeface(font);
		nameTextBox.setTypeface(font);
		aboutTextBox.setTypeface(font);
		deadlineTextBox.setTypeface(font);
		logoText.setTypeface(font);

		logoText.setText("New Project");
		logoButton.setBackgroundResource(R.drawable.home_button);

		selectedMemberList.setAdapter(new SelectedMemberBaseAdapter(newProjectActivity.this));

		String[] items = new String[2];
		items[0] = "Something1";
		items[1] = "Something2";
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.newproject, items) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);

				Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");
				((TextView) v).setTypeface(font);

				return v;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);

				Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");
				((TextView) v).setTypeface(font);
				v.setBackgroundColor(Color.GREEN);

				return v;
			}

		};

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moduleTextBox.setAdapter(adapter);

		logoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(cont, homeActivity.class));

			}
		});

		addMemberButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(addMemberIntent, subActivityID);
			}
		});

		dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, moduleList) {
		};
		;
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moduleTextBox.setAdapter(dataAdapter);

		GetModuleList task = new GetModuleList(this.cont, this.currentActivity, this.dialog);
		task.execute();

		ProjectxGlobalState globalData = (ProjectxGlobalState) getApplication();
		globalData.setModuleId(moduleId);
		moduleTextBox.setOnItemSelectedListener(this);

		deadlineTextBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DEFAULTDATESELECTOR_ID);
			}

		});

		createProjectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject json1 = new JSONObject();
				JSONArray json_array = new JSONArray();
				try {
					json1.put("name", nameTextBox.getText());
					json1.put("description", aboutTextBox.getText());
					ProjectxGlobalState Gs = (ProjectxGlobalState) getApplication();
					json1.put("leader", Gs.getUserid());
					json1.put("moduleCode", moduleTextBox.getSelectedItem());
					json1.put("duedate", deadlineTextBox.getText());
					for (int i = 0; i < members.size(); i++) {
						JSONObject json2 = new JSONObject();
						json2.put("member_id", memberid.get(i));
						json2.put("member_name", members.get(i));
						json_array.put(json2);
					}
					json1.put("members", json_array);

					Log.d("JSON string", json1.toString());
					ProgressDialog dialog = new ProgressDialog(cont);
					dialog.setMessage("Create Project...");
					CreateProjectTask createProjectTask = new CreateProjectTask(cont, currentActivity, json1, dialog);
					createProjectTask.execute("http://ec2-54-251-4-64.ap-southeast-1.compute.amazonaws.com/api/createProject.php");
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});
	}

	private final DateSlider.OnDateSetListener mDateSetListener = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			deadlineTextBox.setText(selectedDate.get(Calendar.DATE) + "-" + selectedDate.get(Calendar.MONTH) + "-" + selectedDate.get(Calendar.YEAR));
		}
	};

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		String selectedFromList = (String) moduleTextBox.getSelectedItem();
		int moduleIndex = moduleList.indexOf(selectedFromList);
		GetStudentList task2 = new GetStudentList();
		task2.execute(moduleIndex);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	protected void onActivityResult(int correlationId, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_CANCELED) {
			// TODO Auto-generated method stub
		} else
			switch (correlationId) {
			case subActivityID:
				Bundle b = data.getExtras();
				memberid.addAll(b.getStringArrayList("MemberIdList"));
				members.clear();
				members.addAll(b.getStringArrayList("MemberNameList"));
				selectedMemberList.setAdapter(new SelectedMemberBaseAdapter(newProjectActivity.this));
				break;
			}

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DEFAULTDATESELECTOR_ID:
			final Calendar c = Calendar.getInstance();
			return new DefaultDateSlider(this, mDateSetListener, c);
		}
		return null;
	}

	private class GetModuleList extends AsyncTask<Void, Void, String> {
		private final ProgressDialog dialog;

		public GetModuleList(Context context, Activity callingActivity, ProgressDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		protected void onPreExecute() {
			if (!this.dialog.isShowing()) {
				this.dialog.setMessage("Retrieving Module List...");
				this.dialog.show();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			String content = "";
			try {
				HttpClient client = new DefaultHttpClient();
				ProjectxGlobalState globalData = (ProjectxGlobalState) getApplication();
				String getURL = "https://ivle.nus.edu.sg/api/Lapi.svc/Modules_Student?APIKey=tlXXFhEsNoTIVTJQruS2o&AuthToken=" + globalData.getAuthToken() + "&Duration=0&IncludeAllInfo=false";
				HttpGet get = new HttpGet(getURL);
				HttpResponse responseGet = client.execute(get);
				HttpEntity mResEntityGet = responseGet.getEntity();
				if (mResEntityGet != null) {
					content = EntityUtils.toString(mResEntityGet);
					Log.d("response", content);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("module-error", "could not get modules");
			}
			return content;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			try {
				JSONObject json = new JSONObject();
				json = new JSONObject(result);
				String xyz = json.getString("Results");
				JSONArray arr = new JSONArray(xyz);

				for (int i = 0; i < arr.length(); i++) {
					JSONObject obj = arr.getJSONObject(i);
					String courseid = obj.getString("CourseCode");
					dataAdapter.add(courseid);

					moduleId.add(obj.getString("ID"));
					Log.d("module - result", courseid);
				}
			} catch (Exception e) {
				Log.e("module-error", "could not get modules");
			}

		}
	}

	public class CreateProjectTask extends AsyncTask<String, Void, String> {
		private final Context context;
		private final Activity callingActivity;
		private final ProgressDialog dialog;
		private final JSONObject requestJson;

		public CreateProjectTask(Context context, Activity callingActivity, JSONObject requestData, ProgressDialog dialog) {
			this.context = context;
			this.callingActivity = callingActivity;
			this.requestJson = requestData;
			this.dialog = dialog;
		}

		@Override
		protected void onPreExecute() {
			System.out.println(this.dialog.isShowing());
			if (!(this.dialog.isShowing())) {
				this.dialog.show();
			}
		}

		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				HttpClient client = new DefaultHttpClient();
				HttpPut httpPut = new HttpPut(url);
				try {
					httpPut.setEntity(new StringEntity(requestJson.toString()));
					HttpResponse execute = client.execute(httpPut);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			System.out.println(result);
			try {
				JSONObject resultJson = new JSONObject(result);
				System.out.println(resultJson.toString());
				if (resultJson.getString("msg").equals("success")) {
					context.startActivity(new Intent(context, homeActivity.class));
				} else {
					Toast.makeText(context, R.string.login_error, Toast.LENGTH_LONG).show();
				}
				callingActivity.finish();
			} catch (JSONException e) {
				Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}

	private class GetStudentList extends AsyncTask<Integer, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Integer... modules) {
			for (Integer module : modules) {
				try {

					HttpClient client = new DefaultHttpClient();
					ProjectxGlobalState globalData = (ProjectxGlobalState) getApplication();
					String getURL = "https://ivle.nus.edu.sg/API/Lapi.svc/Class_Roster?APIKey=tlXXFhEsNoTIVTJQruS2o&AuthToken=" + globalData.getAuthToken() + "&CourseID=" + moduleId.get(module);

					HttpGet get = new HttpGet(getURL);
					HttpResponse responseGet = client.execute(get);
					HttpEntity mResEntityGet = responseGet.getEntity();
					String content;

					JSONObject json = new JSONObject();

					if (mResEntityGet != null) {
						content = EntityUtils.toString(mResEntityGet);
						Log.d("response", content);
						json = new JSONObject(content);
						String xyz = json.getString("Results");
						JSONArray arr = null;
						ArrayList<String> studentList = new ArrayList<String>();
						ArrayList<String> student_id_list = new ArrayList<String>();
						try {
							arr = new JSONArray(xyz);
							for (int i = 0; i < arr.length(); i++) {
								JSONObject obj = arr.getJSONObject(i);
								String name = obj.getString("Name").toLowerCase();
								StringTokenizer st = new StringTokenizer(name, " ", true);
								String token;
								name = "";
								while (st.hasMoreTokens()) {
									token = st.nextToken();
									name += token.substring(0, 1).toUpperCase() + token.substring(1);
								}

								ProjectxGlobalState globalData1 = (ProjectxGlobalState) getApplication();
								if (!obj.getString("UserID").equals(globalData1.getUserid())) {
									studentList.add(name);
									student_id_list.add(obj.getString("UserID"));
								}

							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Bundle b = new Bundle();
						b.putStringArrayList("studentList", studentList);
						b.putStringArrayList("student_id_list", student_id_list);
						b.putStringArrayList("selectedMembers", members);
						addMemberIntent.putExtras(b);

					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("project members-error", "could not get members");
				}
			}
			return null;
		}
	}

	public class SelectedMemberBaseAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;

		public SelectedMemberBaseAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return members.size();
		}

		@Override
		public Object getItem(int position) {
			return members.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.selectedmemberlistitem, null);
				holder = new ViewHolder();
				holder.studentName = (TextView) convertView.findViewById(R.id.selectedmemberitemTextView);
				holder.removeButton = (Button) convertView.findViewById(R.id.selectedmemberitemButton);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.studentName.setText(members.get(position));
			holder.studentName.setTypeface(font);
			holder.removeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					members.remove(position);
					SelectedMemberBaseAdapter.this.notifyDataSetChanged();
				}
			});
			return convertView;
		}

		class ViewHolder {
			TextView studentName;
			Button removeButton;
		}

	}
}