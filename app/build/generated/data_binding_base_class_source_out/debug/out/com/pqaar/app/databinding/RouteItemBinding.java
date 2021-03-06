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
import androidx.constraintlayout.widget.Guideline;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class RouteItemBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final CardView cardview;

  @NonNull
  public final Guideline guideline2;

  @NonNull
  public final Guideline guideline3;

  @NonNull
  public final ProgressBar progress;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView textView17;

  @NonNull
  public final TextView textView18;

  @NonNull
  public final TextView textView22;

  @NonNull
  public final TextView textView4;

  @NonNull
  public final TextView textView9;

  private RouteItemBinding(@NonNull CardView rootView, @NonNull CardView cardview,
      @NonNull Guideline guideline2, @NonNull Guideline guideline3, @NonNull ProgressBar progress,
      @NonNull TextView textView, @NonNull TextView textView17, @NonNull TextView textView18,
      @NonNull TextView textView22, @NonNull TextView textView4, @NonNull TextView textView9) {
    this.rootView = rootView;
    this.cardview = cardview;
    this.guideline2 = guideline2;
    this.guideline3 = guideline3;
    this.progress = progress;
    this.textView = textView;
    this.textView17 = textView17;
    this.textView18 = textView18;
    this.textView22 = textView22;
    this.textView4 = textView4;
    this.textView9 = textView9;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static RouteItemBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static RouteItemBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.route_item, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static RouteItemBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      CardView cardview = (CardView) rootView;

      id = R.id.guideline2;
      Guideline guideline2 = rootView.findViewById(id);
      if (guideline2 == null) {
        break missingId;
      }

      id = R.id.guideline3;
      Guideline guideline3 = rootView.findViewById(id);
      if (guideline3 == null) {
        break missingId;
      }

      id = R.id.progress;
      ProgressBar progress = rootView.findViewById(id);
      if (progress == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = rootView.findViewById(id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.textView17;
      TextView textView17 = rootView.findViewById(id);
      if (textView17 == null) {
        break missingId;
      }

      id = R.id.textView18;
      TextView textView18 = rootView.findViewById(id);
      if (textView18 == null) {
        break missingId;
      }

      id = R.id.textView22;
      TextView textView22 = rootView.findViewById(id);
      if (textView22 == null) {
        break missingId;
      }

      id = R.id.textView4;
      TextView textView4 = rootView.findViewById(id);
      if (textView4 == null) {
        break missingId;
      }

      id = R.id.textView9;
      TextView textView9 = rootView.findViewById(id);
      if (textView9 == null) {
        break missingId;
      }

      return new RouteItemBinding((CardView) rootView, cardview, guideline2, guideline3, progress,
          textView, textView17, textView18, textView22, textView4, textView9);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
