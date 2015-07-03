package com.example.sunchaser.app.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.example.sunchaser.R;
import com.example.sunchaser.app.data.ImageModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by smee on 10/06/15.
 */
public class ImageViewPagerFragment extends Fragment {

    private static final String LOG_TAG = ImageViewPagerFragment.class.getSimpleName();

    private static final String SAVED_STATE_KEY_CURRENT_IMAGE = "current_image_id";

    private ArrayList<ImageModel> imageModels;
    private ImageSwitcher imageSwitcher;
    private View progressView;
    private View previousImageButtonView;
    private View nextImageButtonView;
    private ImageSwitcherTarget switcherTarget = new ImageSwitcherTarget();
    private int currentImageIndex = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_image_switcher, container, false);

        imageSwitcher = (ImageSwitcher) inflatedView.findViewById(R.id.image_switcher);
        progressView = inflatedView.findViewById(R.id.image_switcher_progress);
        previousImageButtonView = inflatedView.findViewById(R.id.image_switcher_previous);
        nextImageButtonView = inflatedView.findViewById(R.id.image_switcher_next);

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView myView = new ImageView(getActivity().getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return myView;
            }
        });

        previousImageButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchImage(false);
            }
        });

        nextImageButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchImage(true);
            }
        });

        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setOutAnimation(out);

        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new MyGestureDetector());
        imageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        if (savedInstanceState != null) {
            currentImageIndex = savedInstanceState.getInt(SAVED_STATE_KEY_CURRENT_IMAGE, 0);
        }

        return inflatedView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_STATE_KEY_CURRENT_IMAGE, currentImageIndex);
        super.onSaveInstanceState(outState);
    }

    public void setImageInfo(Collection<? extends ImageModel> imageModels) {
        this.imageModels = new ArrayList<>(imageModels);
        if (imageModels.isEmpty()) {
            imageSwitcher.setVisibility(View.GONE);
            return;
        }
        loadImage(this.imageModels.get(currentImageIndex), switcherTarget);
//        if (currentImageIndex < imageModels.size()-1) {
//            loadImage(this.imageModels.get(currentImageIndex + 1), null);
//        }
    }

    private void loadImage(ImageModel imageModel, Target target) {

        progressView.setVisibility(View.VISIBLE);
        if (currentImageIndex > 0) {
            previousImageButtonView.setVisibility(View.VISIBLE);
        }
        if (currentImageIndex < imageModels.size() - 1) {
            nextImageButtonView.setVisibility(View.VISIBLE);
        }

        int width = imageModel.getWidth();
        int height = imageModel.getHeight();
        int boundingWidth = dpToPx(imageSwitcher.getWidth());
        int boundingHeight = dpToPx(imageSwitcher.getHeight());

        float xScale = ((float) boundingWidth) / width;
        float yScale = ((float) boundingHeight) / height;

        float scale = (xScale <= yScale) ? xScale : yScale;

        int scaledWidth = (int)(width * scale);
        int scaledHeight = (int)(height * scale);

        String url = imageModel.getUrl(scaledWidth, scaledHeight);
//        if (target == null) {
//            Log.d(LOG_TAG, "Prefetching bitmap from " + url);
//            Picasso.with(getActivity()).load(url);
//        } else {
            Log.d(LOG_TAG, "Loading bitmap from " + url);
            Picasso.with(getActivity()).load(url).resize(scaledWidth, scaledHeight).centerInside().into(target);
//        }
    }

    private int dpToPx(int dp) {
        float density = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    void switchImage(Boolean forwards) {
        if (imageModels == null || imageModels.isEmpty()) {
            return;
        }
        if(forwards) {
            if (currentImageIndex < imageModels.size() - 1) {
                currentImageIndex++;
                ImageModel imageModel = imageModels.get(currentImageIndex);
                loadImage(imageModel, switcherTarget);

                previousImageButtonView.setVisibility(View.VISIBLE);
                if (currentImageIndex == imageModels.size() - 1) {
                    nextImageButtonView.setVisibility(View.GONE);
                }

                // Precache the next image to make things smoother
                //                if (currentImageIndex < imageModels.size() - 1) {
                //                    imageModel = imageModels.get(currentImageIndex + 1);
                //                    loadImage(imageModel, null);
                //                }
            }
        } else if (currentImageIndex > 0) {
            currentImageIndex--;
            ImageModel imageModel = imageModels.get(currentImageIndex);
            loadImage(imageModel, switcherTarget);

            nextImageButtonView.setVisibility(View.VISIBLE);
            if (currentImageIndex == 0) {
                previousImageButtonView.setVisibility(View.GONE);
            }
        }
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        final String TAG = MyGestureDetector.class.getSimpleName();

        // for touch left or touch right events
        private static final int SWIPE_MIN_DISTANCE = 80;   //default is 120
        private static final int SWIPE_MAX_OFF_PATH = 400;
        private static final int SWIPE_THRESHOLD_VELOCITY = 50;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // TODO: Open full-sized image?
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, " on fling event, first velocityX :" + velocityX +
                    " second velocityY" + velocityY);
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX()
                        > SWIPE_MIN_DISTANCE && Math.abs(velocityX)
                        > SWIPE_THRESHOLD_VELOCITY) {
                    switchImage(true);  // left
                }  else if (e2.getX() - e1.getX()
                        > SWIPE_MIN_DISTANCE && Math.abs(velocityX)
                        > SWIPE_THRESHOLD_VELOCITY) {
                    switchImage(false); // right
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }


    private class ImageSwitcherTarget implements Target {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.d(LOG_TAG, "Bitmap loaded");
            // TODO: Resize and centre the image
            progressView.setVisibility(View.GONE);
            imageSwitcher.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            progressView.setVisibility(View.GONE);
            // TODO: Set an error placeholder
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }
}
