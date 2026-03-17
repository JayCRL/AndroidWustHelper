package com.example.wusthelper.adapter;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

public abstract class BaseBindMultiItemQuickAdapter<T extends MultiItemEntity, VB extends ViewBinding>
        extends BaseMultiItemQuickAdapter<T ,BaseBindMultiItemQuickAdapter.BaseBindingHolder> {


    public static class BaseBindingHolder extends BaseViewHolder {
        private final ViewBinding binding;

        public BaseBindingHolder(@NotNull View view) {
            this(() -> view);
        }

        public BaseBindingHolder(@NotNull ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        @NonNull
        @SuppressWarnings("unchecked")
        public <VB extends ViewBinding> VB getViewBinding() {
            return (VB) binding;
        }
    }
}
