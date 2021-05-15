// Generated by view binder compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class LiveRouteListSubItemBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final CardView cardview;

  @NonNull
  public final View divider10;

  @NonNull
  public final View divider11;

  @NonNull
  public final View divider7;

  @NonNull
  public final View divider8;

  @NonNull
  public final TextView godown;

  @NonNull
  public final ProgressBar progress;

  @NonNull
  public final TextView textView18;

  @NonNull
  public final TextView textView28;

  @NonNull
  public final TextView textView29;

  @NonNull
  public final TextView textView8;

  @NonNull
  public final TextView textView9;

  private LiveRouteListSubItemBinding(@NonNull CardView rootView, @NonNull CardView cardview,
      @NonNull View divider10, @NonNull View divider11, @NonNull View divider7,
      @NonNull View divider8, @NonNull TextView godown, @NonNull ProgressBar progress,
      @NonNull TextView textView18, @NonNull TextView textView28, @NonNull TextView textView29,
      @NonNull TextView textView8, @NonNull TextView textView9) {
    this.rootView = rootView;
    this.cardview = cardview;
    this.divider10 = divider10;
    this.divider11 = divider11;
    this.divider7 = divider7;
    this.divider8 = divider8;
    this.godown = godown;
    this.progress = progress;
    this.textView18 = textView18;
    this.textView28 = textView28;
    this.textView29 = textView29;
    this.textView8 = textView8;
    this.textView9 = textView9;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static LiveRouteListSubItemBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static LiveRouteListSubItemBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.live_route_list_sub_item, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static LiveRouteListSubItemBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      CardView cardview = (CardView) rootView;

      id = R.id.divider10;
      View divider10 = rootView.findViewById(id);
      if (divider10 == null) {
        break missingId;
      }

      id = R.id.divider11;
      View divider11 = rootView.findViewById(id);
      if (divider11 == null) {
        break missingId;
      }

      id = R.id.divider7;
      View divider7 = rootView.findViewById(id);
      if (divider7 == null) {
        break missingId;
      }

      id = R.id.divider8;
      View divider8 = rootView.findViewById(id);
      if (divider8 == null) {
        break missingId;
      }

      id = R.id.godown;
      TextView godown = rootView.findViewById(id);
      if (godown == null) {
        break missingId;
      }

      id = R.id.progress;
      ProgressBar progress = rootView.findViewById(id);
      if (progress == null) {
        break missingId;
      }

      id = R.id.textView18;
      TextView textView18 = rootView.findViewById(id);
      if (textView18 == null) {
        break missingId;
      }

      id = R.id.textView28;
      TextView textView28 = rootView.findViewById(id);
      if (textView28 == null) {
        break missingId;
      }

      id = R.id.textView29;
      TextView textView29 = rootView.findViewById(id);
      if (textView29 == null) {
        break missingId;
      }

      id = R.id.textView8;
      TextView textView8 = rootView.findViewById(id);
      if (textView8 == null) {
        break missingId;
      }

      id = R.id.textView9;
      TextView textView9 = rootView.findViewById(id);
      if (textView9 == null) {
        break missingId;
      }

      return new LiveRouteListSubItemBinding((CardView) rootView, cardview, divider10, divider11,
          divider7, divider8, godown, progress, textView18, textView28, textView29, textView8,
          textView9);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
