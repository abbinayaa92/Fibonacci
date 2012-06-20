package com.icreate.projectx.datamodel;

import java.util.List;

public class Project {
	private int project_id;
	private String module_code;
	private String project_name;
	private String due_date;
	private int leader_id;
	private String leader_name;
	private String project_desc;
	private List<ProjectMembers> members;
	private List<Task> tasks;
	private int totalTasks;
	private int tasksAssigned;
	private int tasksCompleted;
	private int tasksInProgress;
	private int tasksOpen;
	private double progress;

	public int getLeader_id() {
		return leader_id;
	}

	public void setLeader_id(int leader_id) {
		this.leader_id = leader_id;
	}

	public String getDue_date() {
		return due_date;
	}

	public void setDue_date(String due_date) {
		this.due_date = due_date;
	}

	public String getProject_name() {
		return project_name;
	}

	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}

	public String getModule_code() {
		return module_code;
	}

	public void setModule_code(String module_code) {
		this.module_code = module_code;
	}

	public int getProject_id() {
		return project_id;
	}

	public void setProject_id(int project_id) {
		this.project_id = project_id;
	}

	public String getProject_desc() {
		return project_desc;
	}

	public void setProject_desc(String project_desc) {
		this.project_desc = project_desc;
	}

	public List<ProjectMembers> getMembers() {
		return members;
	}

	public void setMembers(List<ProjectMembers> members) {
		this.members = members;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTotalTasks(int totalTasks) {
		this.totalTasks = totalTasks;
	}

	public int getTotalTasks() {
		return totalTasks;
	}

	public void setTasksAssigned(int tasksAssigned) {
		this.tasksAssigned = tasksAssigned;
	}

	public int getTasksAssigned() {
		return tasksAssigned;
	}

	public void setTasksCompleted(int tasksCompleted) {
		this.tasksCompleted = tasksCompleted;
	}

	public int getTasksCompleted() {
		return tasksCompleted;
	}

	public void setTasksInProgress(int tasksInProgress) {
		this.tasksInProgress = tasksInProgress;
	}

	public int getTasksInProgress() {
		return tasksInProgress;
	}

	public void setTasksOpen(int tasksOpen) {
		this.tasksOpen = tasksOpen;
	}

	public int getTasksOpen() {
		return tasksOpen;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public double getProgress() {
		return progress;
	}

	public void setLeader_name(String leader_name) {
		this.leader_name = leader_name;
	}

	public String getLeader_name() {
		return leader_name;
	}
}