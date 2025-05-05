package com.nimeshkadecha.todo;

public class Task {
	private long id;
	private long userId;
	private String title;
	private String description;
	private String dueDate;
	private String date; // Example: "2025-05-05"
	private String priority;
	private String status;
	private String category;

	public Task(long id, long userId, String title, String description, String dueDate, String priority, String status, String category) {
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.priority = priority;
		this.status = status;
		this.category = category;
	}

	// Getters and setters
	public long getId() { return id; }
	public void setId(long id) { this.id = id; }

	public String getDate() { return date; }


	public long getUserId() { return userId; }
	public void setUserId(long userId) { this.userId = userId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getDueDate() { return dueDate; }
	public void setDueDate(String dueDate) { this.dueDate = dueDate; }

	public String getPriority() { return priority; }
	public void setPriority(String priority) { this.priority = priority; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
}
