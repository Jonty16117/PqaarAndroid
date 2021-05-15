// Generated by view binder compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class TruckItemBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final CardView cardview;

  @NonNull
  public final ImageView imageView;

  @NonNull
  public final ImageView imageView12;

  @NonNull
  public final TextView textView2;

  @NonNull
  public final TextView textView3;

  @NonNull
  public final TextView truckNo;

  private TruckItemBinding(@NonNull CardView rootView, @NonNull CardView cardview,
      @NonNull ImageView imageView, @NonNull ImageView imageView12, @NonNull TextView textView2,
      @NonNull TextView textView3, @NonNull TextView truckNo) {
    this.rootView = rootView;
    this.cardview = cardview;
    this.imageView = imageView;
    this.imageView12 = imageView12;
    this.textView2 = textView2;
    this.textView3 = textView3;
    this.truckNo = truckNo;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static TruckItemBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static TruckItemBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.truck_item, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static TruckItemBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      CardView cardview = (CardView) rootView;

      id = R.id.imageView;
      ImageView imageView = rootView.findViewById(id);
      if (imageView == null) {
        break missingId;
      }

      id = R.id.imageView12;
      ImageView imageView12 = rootView.findViewById(id);
      if (imageView12 == null) {
        break missingId;
      }

      id = R.id.textView2;
      TextView textView2 = rootView.findViewById(id);
      if (textView2 == null) {
        break missingId;
      }

      id = R.id.textView3;
      TextView textView3 = rootView.findViewById(id);
      if (textView3 == null) {
        break missingId;
      }

      id = R.id.truck_no;
      TextView truckNo = rootView.findViewById(id);
      if (truckNo == null) {
        break missingId;
      }

      return new TruckItemBinding((CardView) rootView, cardview, imageView, imageView12, textView2,
          textView3, truckNo);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}