// Generated by view binder compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class TruckDeliveryAlertDialogBoxBinding implements ViewBinding {
  @NonNull
  private final LinearLayoutCompat rootView;

  @NonNull
  public final Button button5;

  @NonNull
  public final Button button6;

  @NonNull
  public final ConstraintLayout constraintLayout;

  @NonNull
  public final EditText delInfo;

  @NonNull
  public final View divider12;

  @NonNull
  public final ImageView imageView10;

  @NonNull
  public final ImageView imageView11;

  @NonNull
  public final ConstraintLayout outerParent;

  @NonNull
  public final Spinner spinner;

  @NonNull
  public final TextView textView26;

  @NonNull
  public final TextView textView27;

  @NonNull
  public final TextView textView33;

  @NonNull
  public final TextView textView35;

  @NonNull
  public final TextView textView36;

  @NonNull
  public final TextView textView49;

  private TruckDeliveryAlertDialogBoxBinding(@NonNull LinearLayoutCompat rootView,
      @NonNull Button button5, @NonNull Button button6, @NonNull ConstraintLayout constraintLayout,
      @NonNull EditText delInfo, @NonNull View divider12, @NonNull ImageView imageView10,
      @NonNull ImageView imageView11, @NonNull ConstraintLayout outerParent,
      @NonNull Spinner spinner, @NonNull TextView textView26, @NonNull TextView textView27,
      @NonNull TextView textView33, @NonNull TextView textView35, @NonNull TextView textView36,
      @NonNull TextView textView49) {
    this.rootView = rootView;
    this.button5 = button5;
    this.button6 = button6;
    this.constraintLayout = constraintLayout;
    this.delInfo = delInfo;
    this.divider12 = divider12;
    this.imageView10 = imageView10;
    this.imageView11 = imageView11;
    this.outerParent = outerParent;
    this.spinner = spinner;
    this.textView26 = textView26;
    this.textView27 = textView27;
    this.textView33 = textView33;
    this.textView35 = textView35;
    this.textView36 = textView36;
    this.textView49 = textView49;
  }

  @Override
  @NonNull
  public LinearLayoutCompat getRoot() {
    return rootView;
  }

  @NonNull
  public static TruckDeliveryAlertDialogBoxBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static TruckDeliveryAlertDialogBoxBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.truck_delivery_alert_dialog_box, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static TruckDeliveryAlertDialogBoxBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.button5;
      Button button5 = rootView.findViewById(id);
      if (button5 == null) {
        break missingId;
      }

      id = R.id.button6;
      Button button6 = rootView.findViewById(id);
      if (button6 == null) {
        break missingId;
      }

      id = R.id.constraintLayout;
      ConstraintLayout constraintLayout = rootView.findViewById(id);
      if (constraintLayout == null) {
        break missingId;
      }

      id = R.id.delInfo;
      EditText delInfo = rootView.findViewById(id);
      if (delInfo == null) {
        break missingId;
      }

      id = R.id.divider12;
      View divider12 = rootView.findViewById(id);
      if (divider12 == null) {
        break missingId;
      }

      id = R.id.imageView10;
      ImageView imageView10 = rootView.findViewById(id);
      if (imageView10 == null) {
        break missingId;
      }

      id = R.id.imageView11;
      ImageView imageView11 = rootView.findViewById(id);
      if (imageView11 == null) {
        break missingId;
      }

      id = R.id.outer_parent;
      ConstraintLayout outerParent = rootView.findViewById(id);
      if (outerParent == null) {
        break missingId;
      }

      id = R.id.spinner;
      Spinner spinner = rootView.findViewById(id);
      if (spinner == null) {
        break missingId;
      }

      id = R.id.textView26;
      TextView textView26 = rootView.findViewById(id);
      if (textView26 == null) {
        break missingId;
      }

      id = R.id.textView27;
      TextView textView27 = rootView.findViewById(id);
      if (textView27 == null) {
        break missingId;
      }

      id = R.id.textView33;
      TextView textView33 = rootView.findViewById(id);
      if (textView33 == null) {
        break missingId;
      }

      id = R.id.textView35;
      TextView textView35 = rootView.findViewById(id);
      if (textView35 == null) {
        break missingId;
      }

      id = R.id.textView36;
      TextView textView36 = rootView.findViewById(id);
      if (textView36 == null) {
        break missingId;
      }

      id = R.id.textView49;
      TextView textView49 = rootView.findViewById(id);
      if (textView49 == null) {
        break missingId;
      }

      return new TruckDeliveryAlertDialogBoxBinding((LinearLayoutCompat) rootView, button5, button6,
          constraintLayout, delInfo, divider12, imageView10, imageView11, outerParent, spinner,
          textView26, textView27, textView33, textView35, textView36, textView49);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
