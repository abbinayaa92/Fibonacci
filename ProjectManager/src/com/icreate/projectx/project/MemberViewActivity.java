package com.icreate.projectx.project;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.icreate.projectx.R;
import com.icreate.projectx.homeActivity;
import com.icreate.projectx.datamodel.Project;
import com.icreate.projectx.datamodel.ProjectMembers;
import com.icreate.projectx.datamodel.ProjectxGlobalState;
import com.icreate.projectx.datamodel.Task;
import com.icreate.projectx.task.TaskListBaseAdapter;
import com.icreate.projectx.task.TaskViewActivity;
import com.icreate.projectx.task.editTaskActivity;
import com.icreate.projectx.task.newTaskActivity;

public class MemberViewActivity extends Activity {
	private TextView logoText, progressnumber, tasksCompletedView, totaltasksView;
	private Button assignTaskbutton, createTaskButton;
	private ImageButton logoButton;
	private ProgressBar mem_progress;
	private ProjectxGlobalState globalState;
	private Project project;
	private String projectString;
	private ProjectMembers currentMember;
	private Context cont;
	private Activity currentActivity;
	private int memberPosition;
	double memberProgress;
	double totaltasks;
	double totalcompletedtasks;
	private ListView taskListView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.memberview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.logo1);

		cont = this;
		currentActivity = this;

		totaltasksView = (TextView) findViewById(R.id.noTotalTasks);
		tasksCompletedView = (TextView) findViewById(R.id.noCompletedTasks);
		Typeface font = Typeface.createFromAsset(getAssets(), "EraserDust.ttf");
		logoText = (TextView) findViewById(R.id.projectlogoText);
		logoText.setTypeface(font);
		logoText.setSelected(true);
		tasksCompletedView.setTypeface(font);
		totaltasksView.setTypeface(font);
		logoButton = (ImageButton) findViewById(R.id.projectlogoImageButton);
		logoButton.setBackgroundResource(R.drawable.home_button);
		logoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent HomeIntent = new Intent(cont, homeActivity.class);
				HomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(HomeIntent);
				currentActivity.finish();
			}
		});

		progressnumber = (TextView) findViewById(R.id.taskprogressnumber);
		taskListView = (ListView) findViewById(R.id.assigneetasklist);
		taskListView.setTextFilterEnabled(true);
		registerForContextMenu(taskListView);

		Bundle extras = getIntent().getExtras();
		globalState = (ProjectxGlobalState) getApplication();
		if (extras != null) {
			memberPosition = extras.getInt("memberPosition", -1);
			memberProgress = extras.getDouble("memberProgress", -1);
			totaltasks = extras.getDouble("totaltasks", -1);
			totalcompletedtasks = extras.getDouble("totalcompletedtasks", -1);
			currentMember = null;
			int inttotaltasks = (int) totaltasks;
			int inttotalcompletedtasks = (int) totalcompletedtasks;
			totaltasksView.setText("Tasks Assigned : " + inttotaltasks);
			tasksCompletedView.setText("Tasks Completed : " + inttotalcompletedtasks);

		}
		globalState = (ProjectxGlobalState) getApplication();
		project = globalState.getProject();
		currentMember = project.getMembers().get(memberPosition);
		if (currentMember != null) {
			logoText.setText(currentMember.getUser_name());
		}
		mem_progress = (ProgressBar) findViewById(R.id.taskProgress);
		progressnumber.setText((int) memberProgress + "%");

		mem_progress.setProgress((int) memberProgress);
		taskListView.setAdapter(new TaskListBaseAdapter(cont, (ArrayList<Task>) project.getTasks(currentMember.getMember_id())));
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

		assignTaskbutton = (Button) findViewById(R.id.assignTaskButton);
		createTaskButton = (Button) findViewById(R.id.membernewtaskButton);

		assignTaskbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newTaskIntent = new Intent(cont, editTaskActivity.class);
				newTaskIntent.putExtra("member", currentMember.getMember_id());
				startActivity(newTaskIntent);
				currentActivity.finish();
			}
		});

		createTaskButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newTaskIntent2 = new Intent(cont, newTaskActivity.class);
				newTaskIntent2.putExtra("member", currentMember.getMember_id());
				startActivity(newTaskIntent2);
				currentActivity.finish();
			}
		});
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();

		View empty = findViewById(R.id.assigneetasklistempty);
		ListView list = (ListView) findViewById(R.id.assigneetasklist);
		list.setEmptyView(empty);
	}
}
