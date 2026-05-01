package com.example.yanagh.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yanagh.databinding.ItemChatUserBinding;
import com.example.yanagh.databinding.ItemChatAssistantBinding;


public class ChatAdapter extends ListAdapter<ChatItem, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ASSISTANT = 1;

    public ChatAdapter() {
        super(new ChatDiffCallback());
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            ItemChatUserBinding binding = ItemChatUserBinding.inflate(inflater, parent, false);
            return new UserViewHolder(binding);
        } else {
            ItemChatAssistantBinding binding = ItemChatAssistantBinding.inflate(inflater, parent, false);
            return new AssistantViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = getItem(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(item);
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).bind(item);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatUserBinding binding;

        public UserViewHolder(ItemChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatItem item) {
            binding.tvMessage.setText(item.getContent());
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatAssistantBinding binding;

        public AssistantViewHolder(ItemChatAssistantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatItem item) {
            binding.tvMessage.setText(item.getContent());
        }
    }

    static class ChatDiffCallback extends DiffUtil.ItemCallback<ChatItem> {
        @Override
        public boolean areItemsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.equals(newItem);
        }
    }
}
