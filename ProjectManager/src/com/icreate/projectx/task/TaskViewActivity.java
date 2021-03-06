package com.icreate.projectx.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.icreate.projectx.AlarmReceiver;
import com.icreate.projectx.CommentBaseAdapter;
import com.icreate.projectx.MyHorizontalScrollView;
import com.icreate.projectx.MyHorizontalScrollView.SizeCallback;
import com.icreate.projectx.ProjectXPreferences;
import com.icreate.projectx.R;
import com.icreate.projectx.homeActivity;
import com.icreate.projectx.datamodel.Comment;
import com.icreate.projectx.datamodel.CommentList;
import com.icreate.projectx.datamodel.Project;
import com.icreate.projectx.datamodel.ProjectMembers;
import com.icreate.projectx.datamodel.ProjectxGlobalState;
import com.icreate.projectx.datamodel.Task;
import com.icreate.projectx.net.GetProjectTask;
import com.icreate.projectx.project.projectViewActivity;
import com.icreate.projectx.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.icreate.projectx.pulltorefresh.library.PullToRefreshListView;

public class TaskViewActivity extends Activity {

	private TextView logoText, TaskDesc, TaskDeadline, ProjectName, TaskName, TaskAssigneeName, TaskCreatorName, TaskStatus;
	private TextView Assignee, Reporter;
	private ImageView TaskPriority;
	private ImageView slide;
	private Spinner statusSpinner;
	private EditText commentTextBox;
	private Button sendComment, createTask, setAlarm, editTaskbutton;
	private ProjectxGlobalState globalState;
	private MyHorizontalScrollView scrollView;
	private PullToRefreshListView commentListViewWrapper;
	private static ListView taskListView, commentListView;
	private Context cont;
	private Activity currentActivity;
	private String projectString;
	private Project project;
	private Task task;
	private ArrayList<Task> subTasks;
	private final ArrayList<String> status = new ArrayList<String>();
	private View taskview, commentview, logoView;
	private ArrayList<Comment> comments;
	private Bundle extras;
	private static Button parentTaskButton;
	private ArrayAdapter<String> dataAdapter;
	private TextView empty;

	static boolean menuOut = false;
	int btnWidth, task_id = 0;
	boolean isFirst = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = LayoutInflater.from(this);
		setContentView(inflater.inflate(R.layout.scrollview_comment, null));

		scrollView = (MyHorizontalScrollView) findViewById(R.id.myScrollView);
		taskview = inflater.inflate(R.layout.taskview, null);

		commentview = inflater.inflate(R.layout.task_commentview, null);
		cont = this;
		currentActivity = this;

		ViewGroup slidelayout = (ViewGroup) taskview.findViewById(R.id.slidelayout);
		slide = (ImageView) slidelayout.findViewById(R.id.rightlogoImageButtontaskview);
		slide.setOnClickListener(new ClickListenerForScrolling(scrollView, commentview));

		globalState = (ProjectxGlobalState) getApplication();

		Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");
		logoText = (TextView) taskview.findViewById(R.id.projectlogoText);
		logoText.setTypeface(font);
		logoText.setText("Task View");
		logoText.setSelected(true);

		ImageButton homeButton = (ImageButton) taskview.findViewById(R.id.projectlogoImageButton);
		homeButton.setBackgroundResource(R.drawable.home_button);

		homeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent HomeIntent = new Intent(cont, homeActivity.class);
				HomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(HomeIntent);
				currentActivity.finish();
			}
		});

		taskListView = (ListView) taskview.findViewById(R.id.subTaskList);
		empty = (TextView) taskview.findViewById(R.id.subTaskempty);
		empty.setTypeface(font);
		TaskDesc = (TextView) taskview.findViewById(R.id.taskDesc);
		TaskDesc.setTypeface(font);
		TaskDeadline = (TextView) taskview.findViewById(R.id.taskDeadline);
		TaskDeadline.setTypeface(font);
		TaskAssigneeName = (TextView) taskview.findViewById(R.id.taskAssignedToView);
		TaskAssigneeName.setTypeface(font);
		TaskCreatorName = (TextView) taskview.findViewById(R.id.taskCreatedByView);
		TaskCreatorName.setTypeface(font);
		TaskStatus = (TextView) taskview.findViewById(R.id.taskstatusView);
		TaskStatus.setTypeface(font);
		statusSpinner = (Spinner) taskview.findViewById(R.id.taskstatusSpinner);
		TaskPriority = (ImageView) taskview.findViewById(R.id.taskPriorityView);
		ProjectName = (TextView) taskview.findViewById(R.id.ProjectNameTaskView);
		ProjectName.setTypeface(font);
		Assignee = (TextView) taskview.findViewById(R.id.taskView_assignee);
		Assignee.setTypeface(font);
		Reporter = (TextView) taskview.findViewById(R.id.taskView_reporter);
		Reporter.setTypeface(font);
		commentListViewWrapper = (PullToRefreshListView) commentview.findViewById(R.id.commentList);
		commentListView = commentListViewWrapper.getRefreshableView();
		commentTextBox = (EditText) commentview.findViewById(R.id.commentTextBox);
		commentTextBox.setTypeface(font);
		sendComment = (Button) commentview.findViewById(R.id.sendCommentButton);
		createTask = (Button) taskview.findViewById(R.id.createSubTaskButton);
		setAlarm = (Button) taskview.findViewById(R.id.setAlarmButton);
		setAlarm.setEnabled(false);
		extras = getIntent().getExtras();
		setAlarm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent alarmintent = new Intent(getApplicationContext(), AlarmReceiver.class);
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				if (PendingIntent.getBroadcast(getApplicationContext(), task_id, alarmintent, PendingIntent.FLAG_NO_CREATE) != null) {
					// Alarm was set now it is removed
					PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), task_id, alarmintent, PendingIntent.FLAG_UPDATE_CURRENT);
					try {
						am.cancel(sender);
						sender.cancel();
						// setAlarm.setText("Alarm not Set");
						setAlarm.setBackgroundResource(R.drawable.alarmoff);
					} catch (Exception e) {
						Log.e("Error", "AlarmManager update was not canceled. " + e.toString());
					}
				} else {
					// Alarm not set now it is set
					DateFormat formatter;
					Date date;
					formatter = new SimpleDateFormat("yyyy-MM-dd");
					try {
						date = formatter.parse(task.getDue_date());
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						// cal.setTimeInMillis(System.currentTimeMillis());
						// cal.add(Calendar.SECOND, 10);
						alarmintent.putExtra("task_name", task.getTask_name());
						alarmintent.putExtra("description", task.getDescription());
						alarmintent.putExtra("requestCode", task_id);
						alarmintent.putExtra("project_id", project.getProject_id());
						alarmintent.putExtra("project_name", project.getProject_name());
						PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), task_id, alarmintent, PendingIntent.FLAG_UPDATE_CURRENT);
						am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
						// setAlarm.setText("Alarm set");
						setAlarm.setBackgroundResource(R.drawable.alarmon);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		if (extras != null) {
			task_id = extras.getInt("task_id");
			Intent alarmintent = new Intent(getApplicationContext(), AlarmReceiver.class);
			if (PendingIntent.getBroadcast(getApplicationContext(), task_id, alarmintent, PendingIntent.FLAG_NO_CREATE) != null) {
				// setAlarm.setText("Alarm Set");
				setAlarm.setBackgroundResource(R.drawable.alarmon);
			} else {
				// setAlarm.setText("Alarm not set");
				setAlarm.setBackgroundResource(R.drawable.alarmoff);
			}
			project = globalState.getProject();
			ArrayList<Task> alltasks = (ArrayList<Task>) project.getTasks();
			ArrayList<ProjectMembers> member = (ArrayList<ProjectMembers>) project.getMembers();
			subTasks = new ArrayList<Task>();
			for (int i = 0; i < alltasks.size(); i++) {
				if (alltasks.get(i).getTask_id() == task_id) {
					task = alltasks.get(i);
					break;
				}
			}
			setAlarm.setEnabled(true);
			status.add("OPEN");
			status.add("ASSIGNED");
			status.add("IN PROGRESS");
			status.add("COMPLETE");
			dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, status);
			/*
			 * {
			 * 
			 * @Override public boolean isEnabled(int position) { if (position
			 * == 0 || position == 1) { return false; } else { return true; } }
			 * 
			 * @Override public boolean areAllItemsEnabled() { return false; }
			 * }; ;
			 */

			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			statusSpinner.setAdapter(dataAdapter);

			for (int i = 0; i < status.size(); i++) {
				if (status.get(i).equalsIgnoreCase(task.getTask_status()))
					statusSpinner.setSelection(i);
			}
			if (task.getDescription() != null) {
				TaskDesc.setText(task.getDescription());
			} else
				TaskDesc.setVisibility(View.GONE);
			TaskDeadline.setText(task.getDue_date());
			logoText.setText(task.getTask_name());
			for (int i = 0; i < member.size(); i++) {
				if (!(task.getTask_status().equals("OPEN"))) {
					if (member.get(i).getMember_id() == task.getAssignee()) {
						TaskAssigneeName.setText(member.get(i).getUser_name());
					}
				} else
					TaskAssigneeName.setVisibility(View.GONE);
			}
			for (int i = 0; i < member.size(); i++) {
				if (member.get(i).getMember_id() == task.getCreatedBy()) {
					TaskCreatorName.setText(member.get(i).getUser_name());
				}
			}
			String priority = task.getTask_priority();
			if (priority.equalsIgnoreCase("LOW")) {
				TaskPriority.setImageResource(R.drawable.icon_priority_low);
			} else if (priority.equalsIgnoreCase("MEDIUM")) {
				TaskPriority.setImageResource(R.drawable.icon_priority_medium);
			} else if (priority.equalsIgnoreCase("HIGH")) {
				TaskPriority.setImageResource(R.drawable.icon_priority_high);
			} else if (priority.equalsIgnoreCase("CRITICAL")) {
				TaskPriority.setImageResource(R.drawable.icon_priority_critical);
			}
			// TaskPriority.setText(task.getTask_priority());
			TaskStatus.setText(task.getTask_status());
			if (!(task.getTask_status().equals("OPEN"))) {
				for (int i = 0; i < member.size(); i++) {
					if (member.get(i).getMember_id() == task.getAssignee()
							&& ProjectXPreferences.readString(cont, ProjectXPreferences.USER, globalState.getUserid()).equalsIgnoreCase(member.get(i).getUser_id()) && task.getSubTasks().size() == 0) {
						statusSpinner.setVisibility(View.VISIBLE);
						TaskStatus.setVisibility(View.GONE);
					}

				}
			}
			ProjectName.setText(project.getProject_name());
			int sub_taskid;
			for (int i = 0; i < task.getTopSubTasks().size(); i++) {
				sub_taskid = task.getTopSubTasks().get(i);
				for (int j = 0; j < alltasks.size(); j++) {
					if (sub_taskid == alltasks.get(j).getTask_id()) {
						subTasks.add(alltasks.get(j));
						break;
					}

				}
			}

			if (subTasks.size() == 0) {
				taskListView.setVisibility(View.GONE);
			} else {
				taskListView.setAdapter(new subtaskBaseAdapter(cont, subTasks));
			}

			String url = ProjectxGlobalState.urlPrefix + "commentList.php";
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("task_id", new Integer(task_id).toString()));
			String paramString = URLEncodedUtils.format(params, "utf-8");
			url += "?" + paramString;
			ProgressDialog dialog = new ProgressDialog(cont);
			dialog.setMessage("Getting Comments");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			ListComment ListComments = new ListComment(cont, currentActivity, dialog, commentListView);
			ListComments.execute(url);

		}

		// View transparent = new TextView(this);
		// transparent.setBackgroundColor(android.R.color.transparent);

		final View[] children = new View[] { commentview, taskview };

		// Scroll to app (view[1]) when layout finished.
		int scrollToViewIdx = 1;
		scrollView.initViews(children, scrollToViewIdx, new SizeCallbackForMenu(slide));

		sendComment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject json1 = new JSONObject();
				JSONArray json_array = new JSONArray();
				try {
					json1.put("comment", commentTextBox.getText());
					json1.put("taskId", task_id);
					ProjectxGlobalState Gs = (ProjectxGlobalState) getApplication();
					json1.put("createdBy", ProjectXPreferences.readString(cont, ProjectXPreferences.USER, Gs.getUserid()));

					Log.d("JSON string", json1.toString());
					ProgressDialog dialog = new ProgressDialog(cont);
					dialog.setMessage("Create Comments...");
					dialog.setCancelable(false);
					dialog.setCanceledOnTouchOutside(false);
					CreateCommentTask createCommentTask = new CreateCommentTask(cont, currentActivity, json1, dialog);
					createCommentTask.execute(ProjectxGlobalState.urlPrefix + "createComment.php");

					/*
					 * String url = urlPrefix + "commentList.php" ;
					 * List<NameValuePair> params = new
					 * LinkedList<NameValuePair>(); params.add(new
					 * BasicNameValuePair("task_id", new
					 * Integer(task_id).toString())); String paramString =
					 * URLEncodedUtils.format(params, "utf-8"); url += "?" +
					 * paramString; ProgressDialog dialog1 = new
					 * ProgressDialog(cont);
					 * dialog1.setMessage("Getting Comments"); ListComment
					 * ListComments = new ListComment(cont, currentActivity,
					 * dialog1, commentListView); hoola.println(url);
					 * ListComments.execute(url);
					 */

					commentTextBox.setText("");

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});

		editTaskbutton = (Button) taskview.findViewById(R.id.taskeditButton);
		editTaskbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newTaskIntent = new Intent(cont, editTaskActivity.class);
				newTaskIntent.putExtra("task_id", task_id);
				startActivity(newTaskIntent);
				currentActivity.finish();
			}
		});

		parentTaskButton = (Button) taskview.findViewById(R.id.goToParent);
		parentTaskButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<Task> alltasks = (ArrayList<Task>) project.getTasks();
				Intent parentTaskIntent;
				for (Task taskItem : alltasks) {
					if (taskItem.getTask_id() == extras.getInt("task_id")) {
						if (taskItem.getParentId() != 0) {
							parentTaskIntent = new Intent(cont, TaskViewActivity.class);
							Log.d("taskview to parent", "" + taskItem.getParentId());
							// parentTaskIntent.putExtra("task_id",
							// ""+taskItem.getParentId());
							parentTaskIntent.putExtra("task_id", taskItem.getParentId());
							startActivity(parentTaskIntent);
							currentActivity.finish();
						} else {
							parentTaskIntent = new Intent(cont, projectViewActivity.class);
							parentTaskIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(parentTaskIntent);
							currentActivity.finish();
						}

					}
				}
			}
		});

		taskListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object o = taskListView.getItemAtPosition(position);
				Task selectedTask = (Task) o;

				Intent TaskViewIntent = new Intent(cont, TaskViewActivity.class);
				TaskViewIntent.putExtra("task_id", selectedTask.getTask_id());
				startActivity(TaskViewIntent);
				currentActivity.finish();
			}
		});

		statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				JSONObject json1 = new JSONObject();
				ProjectxGlobalState glob_data = (ProjectxGlobalState) getApplication();
				try {
					json1.put("taskId", task.getTask_id());
					json1.put("user", ProjectXPreferences.readString(cont, ProjectXPreferences.USER, glob_data.getUserid()));
					json1.put("projectId", project.getProject_id());
					json1.put("name", task.getTask_name());
					if (task.getParentId() != 0)
						json1.put("parentId", task.getParentId());
					json1.put("description", TaskDesc.getText());
					for (int i = 0; i < project.getMembers().size(); i++) {
						if (task.getCreatedBy() == project.getMembers().get(i).getMember_id())
							json1.put("createdBy", project.getMembers().get(i).getUser_id());
					}

					json1.put("duedate", TaskDeadline.getText());

					if (!(status.get(position).equals("OPEN")))
						json1.put("assignee", task.getAssignee());
					json1.put("status", status.get(position));
					json1.put("priority", task.getTask_priority());

					Log.d("JSON string", json1.toString());
					ProgressDialog dialog = new ProgressDialog(cont);
					dialog.setMessage("Create Task...");
					dialog.setCancelable(false);
					dialog.setCanceledOnTouchOutside(false);
					CreateTask createTask = new CreateTask(cont, currentActivity, json1, dialog);
					createTask.execute(ProjectxGlobalState.urlPrefix + "createTask_not.php");
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		createTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent NewTaskIntent = new Intent(cont, newTaskActivity.class);
				NewTaskIntent.putExtra("parent", task_id);
				startActivity(NewTaskIntent);
				currentActivity.finish();
			}
		});

		logoText.setSelected(true);

		logoText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logoText.setSelected(true);
			}
		});

		commentListViewWrapper.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				String url = ProjectxGlobalState.urlPrefix + "commentList.php";
				List<NameValuePair> params = new LinkedList<NameValuePair>();
				params.add(new BasicNameValuePair("task_id", new Integer(task_id).toString()));
				String paramString = URLEncodedUtils.format(params, "utf-8");
				url += "?" + paramString;
				ProgressDialog dialog = new ProgressDialog(cont);
				dialog.setMessage("Getting Comments");
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				ListComment ListComments = new ListComment(cont, currentActivity, commentListView);
				ListComments.execute(url);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isFirst) {
			menuOut = true;
			slide.performClick();
		} else {
			isFirst = true;
		}

		logoText.setFocusable(true);
		logoText.requestFocus();
		logoText.setFocusableInTouchMode(true);
	}

	public class CreateCommentTask extends AsyncTask<String, Void, String> {
		private final Context context;
		private final Activity callingActivity;
		private final ProgressDialog dialog;
		private final JSONObject requestJson;

		public CreateCommentTask(Context context, Activity callingActivity, JSONObject requestData, ProgressDialog dialog) {
			this.context = context;
			this.callingActivity = callingActivity;
			this.requestJson = requestData;
			this.dialog = dialog;
		}

		@Override
		protected void onPreExecute() {
			if (!(this.dialog.isShowing())) {
				this.dialog.setCancelable(false);
				this.dialog.setCanceledOnTouchOutside(false);
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
			try {
				JSONObject resultJson = new JSONObject(result);
				if (resultJson.getString("msg").equals("success")) {
					// context.startActivity(new Intent(context,
					// homeActivity.class));
					String url = ProjectxGlobalState.urlPrefix + "commentList.php";
					List<NameValuePair> params = new LinkedList<NameValuePair>();
					params.add(new BasicNameValuePair("task_id", new Integer(task_id).toString()));
					String paramString = URLEncodedUtils.format(params, "utf-8");
					url += "?" + paramString;
					ProgressDialog dialog1 = new ProgressDialog(cont);
					dialog1.setMessage("Getting Comments");
					dialog.setCancelable(false);
					dialog.setCanceledOnTouchOutside(false);
					ListComment ListComments = new ListComment(context, callingActivity, dialog, commentListView);
					ListComments.execute(url);
				} else {

				}
				// callingActivity.finish();
			} catch (JSONException e) {

			}
		}
	}

	/**
	 * Helper for examples with a HSV that should be scrolled by a menu View's
	 * width.
	 */
	private static class ClickListenerForScrolling implements OnClickListener {
		HorizontalScrollView scrollView;
		View menu;

		/**
		 * Menu must NOT be out/shown to start with.
		 */

		public ClickListenerForScrolling(HorizontalScrollView scrollView, View menu) {
			super();
			this.scrollView = scrollView;
			this.menu = menu;
		}

		@Override
		public void onClick(View v) {
			Context context = menu.getContext();

			int menuWidth = menu.getMeasuredWidth();

			// Ensure menu is visible
			// if (menu.getVisibility() == View.INVISIBLE)
			menu.setVisibility(View.VISIBLE);
			// else
			// menu.setVisibility(View.INVISIBLE);

			if (!menuOut) {
				// Scroll to 0 to reveal menu
				int left = 0;
				scrollView.smoothScrollTo(left, 0);
				taskListView.setClickable(false);
				taskListView.setEnabled(false);
				parentTaskButton.setClickable(false);
				parentTaskButton.setEnabled(false);
			} else {
				// Scroll to menuWidth so menu isn't on screen.
				int left = menuWidth;
				scrollView.smoothScrollTo(left, 0);
				taskListView.setClickable(true);
				taskListView.setEnabled(true);
				parentTaskButton.setClickable(true);
				parentTaskButton.setEnabled(true);
			}
			menuOut = !menuOut;
		}
	}

	/**
	 * Helper that remembers the width of the 'slide' button, so that the
	 * 'slide' button remains in view, even when the menu is showing.
	 */
	private static class SizeCallbackForMenu implements SizeCallback {
		int btnWidth;
		View btnSlide;

		public SizeCallbackForMenu(View btnSlide) {
			super();
			this.btnSlide = btnSlide;
		}

		@Override
		public void onGlobalLayout() {
			btnWidth = btnSlide.getMeasuredWidth() + 50;
		}

		@Override
		public void getViewSize(int idx, int w, int h, int[] dims) {
			dims[0] = w;
			dims[1] = h;
			final int menuIdx = 0;
			if (idx == menuIdx) {
				dims[0] = w - btnWidth;
			}
		}
	}

	private class ListComment extends AsyncTask<String, Void, String> {
		private final Context context;
		private final Activity callingActivity;
		private final ProgressDialog dialog;
		private final ListView commentListView;

		public ListComment(Context context, Activity callingActivity, ProgressDialog dialog, ListView commentListView) {
			this.context = context;
			this.callingActivity = callingActivity;
			this.dialog = dialog;
			this.commentListView = commentListView;
		}

		public ListComment(Context context, Activity callingActivity, ListView commentListView) {
			this.context = context;
			this.callingActivity = callingActivity;
			this.dialog = null;
			this.commentListView = commentListView;
		}

		@Override
		protected void onPreExecute() {
			if (this.dialog != null) {
				if (!this.dialog.isShowing()) {
					this.dialog.setMessage("Getting Comments...");
					this.dialog.setCancelable(false);
					this.dialog.setCanceledOnTouchOutside(false);
					this.dialog.show();
				}
			}
		}

		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				HttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
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
			if (this.dialog != null) {
				if (this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
			}
			try {
				JSONObject resultJson = new JSONObject(result);
				Log.d("CommentList", resultJson.toString());
				if (resultJson.getString("msg").equals("success")) {
					Gson gson = new Gson();
					CommentList commentsContainer = gson.fromJson(result, CommentList.class);
					globalState.setCommentList(commentsContainer);
					comments = commentsContainer.getComments();
					commentListView.setAdapter(new CommentBaseAdapter(context, comments));
					commentListView.setSelection(commentListView.getCount() - 1);
					if (this.dialog == null) {
						commentListViewWrapper.onRefreshComplete();
					}
				} else {

				}
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
	}

	public class CreateTask extends AsyncTask<String, Void, String> {
		private final Context context;
		private final Activity callingActivity;
		private final ProgressDialog dialog;
		private final JSONObject requestJson;

		public CreateTask(Context context, Activity callingActivity, JSONObject requestData, ProgressDialog dialog) {
			this.context = context;
			this.callingActivity = callingActivity;
			this.requestJson = requestData;
			this.dialog = dialog;
		}

		@Override
		protected void onPreExecute() {
			if (!(this.dialog.isShowing())) {
				this.dialog.show();
				this.dialog.setCanceledOnTouchOutside(false);
				this.dialog.setCancelable(false);
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
					Log.d("inside", requestJson.toString());
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
			try {
				JSONObject resultJson = new JSONObject(result);
				if (resultJson.getString("msg").equals("success")) {
					int projectId = project.getProject_id();
					int taskId = resultJson.getInt("task_id");
					String url = ProjectxGlobalState.urlPrefix + "getProject.php?project_id=" + projectId;
					ProgressDialog dialog = new ProgressDialog(context);
					dialog.setMessage("Updating Status...");
					dialog.setCancelable(false);
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
					GetProjectTask getProjectTask = new GetProjectTask(context, callingActivity, dialog, taskId, true);
					getProjectTask.execute(url);
				} else {

				}
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
	}
}
