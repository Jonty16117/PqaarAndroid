// Generated by data binding compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pqaar.app.R;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class FragmentAddTrucksBinding extends ViewDataBinding {
  @NonNull
  public final Button button;

  @NonNull
  public final FloatingActionButton fab;

  @NonNull
  public final ConstraintLayout header;

  @NonNull
  public final ImageView imageView2;

  @NonNull
  public final ImageView imageView4;

  @NonNull
  public final MotionLayout motionLayout;

  @NonNull
  public final RecyclerView recyclerView;

  @NonNull
  public final TextView titleText;

  @NonNull
  public final TextView trucksAdded;

  @NonNull
  public final TextView trucksAddedCount;

  protected FragmentAddTrucksBinding(Object _bindingComponent, View _root, int _localFieldCount,
      Button button, FloatingActionButton fab, ConstraintLayout header, ImageView imageView2,
      ImageView imageView4, MotionLayout motionLayout, RecyclerView recyclerView,
      TextView titleText, TextView trucksAdded, TextView trucksAddedCount) {
    super(_bindingComponent, _root, _localFieldCount);
    this.button = button;
    this.fab = fab;
    this.header = header;
    this.imageView2 = imageView2;
    this.imageView4 = imageView4;
    this.motionLayout = motionLayout;
    this.recyclerView = recyclerView;
    this.titleText = titleText;
    this.trucksAdded = trucksAdded;
    this.trucksAddedCount = trucksAddedCount;
  }

  @NonNull
  public static FragmentAddTrucksBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.fragment_add_trucks, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static FragmentAddTrucksBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<FragmentAddTrucksBinding>inflateInternal(inflater, R.layout.fragment_add_trucks, root, attachToRoot, component);
  }

  @NonNull
  public static FragmentAddTrucksBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.fragment_add_trucks, null, false, component)
   */
  @NonNull
  @Deprecated
  public static FragmentAddTrucksBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<FragmentAddTrucksBinding>inflateInternal(inflater, R.layout.fragment_add_trucks, null, false, component);
  }

  public static FragmentAddTrucksBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static FragmentAddTrucksBinding bind(@NonNull View view, @Nullable Object component) {
    return (FragmentAddTrucksBinding)bind(component, view, R.layout.fragment_add_trucks);
  }
}