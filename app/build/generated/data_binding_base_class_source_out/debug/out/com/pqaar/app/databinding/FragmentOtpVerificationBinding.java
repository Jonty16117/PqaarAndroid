// Generated by view binder compiler. Do not edit!
package com.pqaar.app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.pqaar.app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentOtpVerificationBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final Button btnVerifyOtp;

  @NonNull
  public final Button button;

  @NonNull
  public final ImageView imageView2;

  @NonNull
  public final ImageView imageView4;

  @NonNull
  public final ProgressBar progressBar4;

  @NonNull
  public final ScrollView scrollView2;

  @NonNull
  public final EditText textInputOtp;

  @NonNull
  public final TextView titleText;

  @NonNull
  public final TextView titleText7;

  private FragmentOtpVerificationBinding(@NonNull ScrollView rootView, @NonNull Button btnVerifyOtp,
      @NonNull Button button, @NonNull ImageView imageView2, @NonNull ImageView imageView4,
      @NonNull ProgressBar progressBar4, @NonNull ScrollView scrollView2,
      @NonNull EditText textInputOtp, @NonNull TextView titleText, @NonNull TextView titleText7) {
    this.rootView = rootView;
    this.btnVerifyOtp = btnVerifyOtp;
    this.button = button;
    this.imageView2 = imageView2;
    this.imageView4 = imageView4;
    this.progressBar4 = progressBar4;
    this.scrollView2 = scrollView2;
    this.textInputOtp = textInputOtp;
    this.titleText = titleText;
    this.titleText7 = titleText7;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentOtpVerificationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentOtpVerificationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_otp_verification, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentOtpVerificationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnVerifyOtp;
      Button btnVerifyOtp = rootView.findViewById(id);
      if (btnVerifyOtp == null) {
        break missingId;
      }

      id = R.id.button;
      Button button = rootView.findViewById(id);
      if (button == null) {
        break missingId;
      }

      id = R.id.imageView2;
      ImageView imageView2 = rootView.findViewById(id);
      if (imageView2 == null) {
        break missingId;
      }

      id = R.id.imageView4;
      ImageView imageView4 = rootView.findViewById(id);
      if (imageView4 == null) {
        break missingId;
      }

      id = R.id.progressBar4;
      ProgressBar progressBar4 = rootView.findViewById(id);
      if (progressBar4 == null) {
        break missingId;
      }

      ScrollView scrollView2 = (ScrollView) rootView;

      id = R.id.textInputOtp;
      EditText textInputOtp = rootView.findViewById(id);
      if (textInputOtp == null) {
        break missingId;
      }

      id = R.id.title_text;
      TextView titleText = rootView.findViewById(id);
      if (titleText == null) {
        break missingId;
      }

      id = R.id.title_text7;
      TextView titleText7 = rootView.findViewById(id);
      if (titleText7 == null) {
        break missingId;
      }

      return new FragmentOtpVerificationBinding((ScrollView) rootView, btnVerifyOtp, button,
          imageView2, imageView4, progressBar4, scrollView2, textInputOtp, titleText, titleText7);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
