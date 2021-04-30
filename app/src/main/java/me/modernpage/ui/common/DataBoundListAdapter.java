package me.modernpage.ui.common;
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A generic RecyclerView adapter that uses Data Binding & DiffUtil.
 *
 * @param <T> Type of the items in the list
 * @param <V> The type of the ViewDataBinding
 */
public abstract class DataBoundListAdapter<T, V extends ViewDataBinding>
        extends RecyclerView.Adapter<DataBoundViewHolder<V>> {
    @Nullable
    private List<T> items;
    // each time data is set, we update this variable so that if DiffUtil calculation returns
    // after repetitive updates, we can ignore the old calculation
    private int dataVersion = 0;


    @Override
    public final DataBoundViewHolder<V> onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        V binding = createBinding(parent, viewType);
        DataBoundViewHolder<V> holder = new DataBoundViewHolder<>(binding);
        holder.mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        return holder;
    }

    protected abstract V createBinding(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(DataBoundViewHolder<V> holder, int position) {
        //noinspection ConstantConditions
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickView(holder.binding, position);
            }
        });
        holder.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        bind(holder.binding, items.get(position), position);
        holder.binding.executePendingBindings();
    }

    @SuppressLint("StaticFieldLeak")
    @MainThread
    public void replace(List<T> update) {
        dataVersion++;
        if (items == null) {
            if (update == null) {
                return;
            }
            items = update;
            notifyDataSetChanged();
        } else if (update == null) {
            int oldSize = items.size();
            items = null;
            notifyItemRangeRemoved(0, oldSize);
        } else {
            final int startVersion = dataVersion;
            final List<T> oldItems = items;
            new AsyncTask<Void, Void, DiffUtil.DiffResult>() {
                @Override
                protected DiffUtil.DiffResult doInBackground(Void... voids) {
                    return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return oldItems.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return update.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            T oldItem = oldItems.get(oldItemPosition);
                            T newItem = update.get(newItemPosition);
                            return DataBoundListAdapter.this.areItemsTheSame(oldItem, oldItemPosition, newItem, newItemPosition);
                        }


                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            T oldItem = oldItems.get(oldItemPosition);
                            T newItem = update.get(newItemPosition);
                            return DataBoundListAdapter.this.areContentsTheSame(oldItem, oldItemPosition, newItem, newItemPosition);
                        }

                    });
                }

                @Override
                protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                    if (startVersion != dataVersion) {
                        // ignore update
                        return;
                    }
                    items = update;
                    diffResult.dispatchUpdatesTo(DataBoundListAdapter.this);

                }
            }.execute();
        }
    }

    public abstract void onClickView(V binding, int position);

    @Override
    public void onViewRecycled(@NonNull DataBoundViewHolder<V> holder) {
        super.onViewRecycled(holder);
        holder.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(items.get(position));
    }


    protected abstract int getItemViewType(T item);

    protected abstract void bind(V binding, T item, int position);

    /**
     * areItemsTheSame(int oldItemPosition, int newItemPosition) :
     * Called by the DiffUtil to decide whether two object represent the same Item.
     * If your items have unique ids, this method should check their id equality.
     *
     * @param oldItem
     * @param oldItemPosition
     * @param newItem
     * @param newItemPosition
     * @return
     */
    protected abstract boolean areItemsTheSame(T oldItem, int oldItemPosition, T newItem, int newItemPosition);

    /**
     * areContentsTheSame(int oldItemPosition, int newItemPosition) :
     * Checks whether two items have the same data.You can change its behavior depending on your UI.
     * This method is called by DiffUtil only if areItemsTheSame returns true.
     *
     * @param oldItem
     * @param oldItemPosition
     * @param newItem
     * @param newItemPosition
     * @return
     */
    protected abstract boolean areContentsTheSame(T oldItem, int oldItemPosition, T newItem, int newItemPosition);

//    /**
//     * getChangePayload(int oldItemPosition, int newItemPosition) :
//     * If areItemTheSame return true and areContentsTheSame returns false
//     * DiffUtil calls this method to get a payload about the change.
//     *
//     * @param oldItem
//     * @param oldItemPosition
//     * @param newItem
//     * @param newItemPosition
//     * @return
//     */
//    protected abstract boolean getChangePayload(T oldItem, int oldItemPosition, T newItem, int newItemPosition);

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull DataBoundViewHolder<V> holder) {
        holder.mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull DataBoundViewHolder<V> holder) {
        holder.mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
    }
}
