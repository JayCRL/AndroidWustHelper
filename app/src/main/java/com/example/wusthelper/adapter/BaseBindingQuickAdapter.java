package com.example.wusthelper.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.base.ViewBindingUtil;

import org.jetbrains.annotations.NotNull;

/**
 * 封装思路，来自于
 * https://github.com/DylanCaiCoding/ViewBindingKtx/wiki/改造基类-(Java)
 * 中 关于 BaseBindingQuickAdapter的封装
 * */
public abstract class BaseBindingQuickAdapter<T, VB extends ViewBinding>
        extends BaseQuickAdapter<T, BaseBindingQuickAdapter.BaseBindingHolder> {

    public BaseBindingQuickAdapter() {
        this(-1);
    }

    public BaseBindingQuickAdapter(@LayoutRes int layoutResId) {
        super(layoutResId);
    }



    @NotNull
    @Override
    public BaseBindingHolder onCreateDefViewHolder(@NotNull ViewGroup parent, int viewType) {

        VB viewBinding = ViewBindingUtil.inflateWithGeneric(this, parent);

        return new BaseBindingHolder(viewBinding);
    }

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