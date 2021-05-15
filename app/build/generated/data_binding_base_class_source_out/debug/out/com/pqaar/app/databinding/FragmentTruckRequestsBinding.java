// Generated by view binder compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentTruckRequestsBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final RecyclerView recyclerView;

  @NonNull
  public final TextView textView13;

  private FragmentTruckRequestsBinding(@NonNull CoordinatorLayout rootView,
      @NonNull RecyclerView recyclerView, @NonNull TextView textView13) {
    this.rootView = rootView;
    this.recyclerView = recyclerView;
    this.textView13 = textView13;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentTruckRequestsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentTruckRequestsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_truck_requests, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentTruckRequestsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.recyclerView;
      RecyclerView recyclerView = rootView.findViewById(id);
      if (recyclerView == null) {
        break missingId;
      }

      id = R.id.textView13;
      TextView textView13 = rootView.findViewById(id);
      if (textView13 == null) {
        break missingId;
      }

      return new FragmentTruckRequestsBinding((CoordinatorLayout) rootView, recyclerView,
          textView13);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
