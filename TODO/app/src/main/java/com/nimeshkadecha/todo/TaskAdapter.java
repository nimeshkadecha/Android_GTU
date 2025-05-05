	package com.nimeshkadecha.todo;

	import android.content.Context;
	import android.view.LayoutInflater;
	import android.view.View;
	import android.view.ViewGroup;
	import android.widget.TextView;

	import androidx.annotation.NonNull;
	import androidx.recyclerview.widget.RecyclerView;

	import java.util.List;

	public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
		private Context context;
		private List<Task> taskList;
		private OnItemClickListener listener;

		public interface OnItemClickListener {
			void onItemClick(Task task);
		}

		public void setOnItemClickListener(OnItemClickListener listener) {
			this.listener = listener;
		}

		public TaskAdapter(Context context, List<Task> tasks) {
			this.context = context;
			this.taskList = tasks;
		}

		@NonNull
		@Override
		public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
			return new TaskViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
			Task task = taskList.get(position);
			holder.tvTitle.setText(task.getTitle());
			holder.tvDueDate.setText(task.getDueDate());
			holder.tvPriority.setText(task.getPriority());
			holder.tvStatus.setText(task.getStatus());

			holder.itemView.setOnClickListener(v -> {
				if (listener != null) {
					listener.onItemClick(task);
				}
			});
		}

		@Override
		public int getItemCount() {
			return taskList.size();
		}

		public void updateTasks(List<Task> tasks) {
			this.taskList = tasks;
			notifyDataSetChanged();
		}

		static class TaskViewHolder extends RecyclerView.ViewHolder {
			TextView tvTitle, tvDueDate, tvPriority, tvStatus;

			public TaskViewHolder(@NonNull View itemView) {
				super(itemView);
				tvTitle = itemView.findViewById(R.id.tvTitle);
				tvDueDate = itemView.findViewById(R.id.tvDueDate);
				tvPriority = itemView.findViewById(R.id.tvPriority);
				tvStatus = itemView.findViewById(R.id.tvStatus);
			}
		}
	}
