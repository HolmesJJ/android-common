package com.example.common.ui.widget.game;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.common.R;
import com.example.common.callback.ITouchControl;
import com.example.common.model.game.Ipa;
import com.example.common.model.game.Rectangle;
import com.example.common.ui.activity.GameActivity;
import com.example.common.ui.widget.RoundImageView;
import com.example.common.utils.DensityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pl.droidsonroids.gif.GifImageView;

public class MoveControl implements ITouchControl {

    private final GameActivity mActivity;
    private final float mWidth, mBigWidth;

    private final ArrayList<Ipa> mIpas = new ArrayList<>();
    private final ArrayList<View> mLeftRectangles = new ArrayList<>();
    private final ArrayList<View> mMiddleRectangles = new ArrayList<>();
    private final ArrayList<View> mRightRectangles = new ArrayList<>();

    private View mVTopLeftCube;
    private View mVTopMiddleCube;
    private View mVTopRightCube;

    private View mVBottomLeftCube;
    private View mVBottomMiddleCube;
    private View mVBottomRightCube;

    private MoveTextView mMtvLeft;
    private MoveTextView mMtvMiddle;
    private MoveTextView mMtvRight;

    private RoundImageView mRivStart;
    private TextView mTvScore;
    private TextView mTvStatus;
    private TextView mTvTimer;
    private RelativeLayout mRlGifContainer;
    private GifImageView mGivWellDone;
    private GifImageView mGivTryAgain;

    private CountDownTimer mTimer;

    private final boolean[] mIsViewCorrect;
    private Handler mHandler;

    private boolean isGameStart = false;
    private int score = 0;

    public MoveControl(GameActivity activity) {
        mActivity = activity;
        mWidth = DensityUtils.dip2px(60);
        mBigWidth = DensityUtils.dip2px(70);
        mHandler = new Handler();
        mIsViewCorrect = new boolean[] {false, false ,false};
        intView();
    }

    private void intView() {
        mVTopLeftCube = mActivity.findViewById(R.id.v_top_left_cube);
        mVTopRightCube = mActivity.findViewById(R.id.v_top_right_cube);
        mVTopMiddleCube = mActivity.findViewById(R.id.v_top_middle_cube);
        mVBottomLeftCube = mActivity.findViewById(R.id.v_bottom_left_cube);
        mVBottomRightCube = mActivity.findViewById(R.id.v_bottom_right_cube);
        mVBottomMiddleCube = mActivity.findViewById(R.id.v_bottom_middle_cube);
        mMtvLeft = mActivity.findViewById(R.id.mtv_left);
        mMtvLeft.setControlOnTouch(this);
        mMtvMiddle = mActivity.findViewById(R.id.mtv_middle);
        mMtvMiddle.setControlOnTouch(this);
        mMtvRight = mActivity.findViewById(R.id.mtv_right);
        mMtvRight.setControlOnTouch(this);

        mRivStart = (RoundImageView) mActivity.findViewById(R.id.riv_start);
        mTvScore = (TextView) mActivity.findViewById(R.id.tv_score);
        mTvStatus = (TextView) mActivity.findViewById(R.id.tv_status);
        mTvTimer = (TextView) mActivity.findViewById(R.id.tv_timer);
        mRlGifContainer = (RelativeLayout) mActivity.findViewById(R.id.gif_container);
        mGivWellDone = (GifImageView) mActivity.findViewById(R.id.well_done);
        mGivTryAgain = (GifImageView) mActivity.findViewById(R.id.try_again);
    }

    public void showAllCubes() {
        mMtvLeft.setVisibility(View.VISIBLE);
        mMtvMiddle.setVisibility(View.VISIBLE);
        mMtvRight.setVisibility(View.VISIBLE);
    }

    public void hideAllCubes() {
        mMtvLeft.setVisibility(View.GONE);
        mMtvMiddle.setVisibility(View.GONE);
        mMtvRight.setVisibility(View.GONE);
    }

    public void setCubeLeft(String ipa) {
        mMtvLeft.setText(ipa);
    }

    public void setCubeMiddle(String ipa) {
        mMtvMiddle.setText(ipa);
    }

    public void setCubeRight(String ipa) {
        mMtvRight.setText(ipa);
    }

    public boolean isGameStart() {
        return isGameStart;
    }

    public void setGameStart(boolean gameStart) {
        isGameStart = gameStart;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void init() {
        mLeftRectangles.clear();
        mLeftRectangles.add(mVBottomLeftCube);
        mLeftRectangles.add(mVBottomMiddleCube);
        mLeftRectangles.add(mVBottomRightCube);

        mMiddleRectangles.clear();
        mMiddleRectangles.add(mVBottomLeftCube);
        mMiddleRectangles.add(mVBottomMiddleCube);
        mMiddleRectangles.add(mVBottomRightCube);

        mRightRectangles.clear();
        mRightRectangles.add(mVBottomLeftCube);
        mRightRectangles.add(mVBottomMiddleCube);
        mRightRectangles.add(mVBottomRightCube);

        // view加载完成时回调
        mVTopLeftCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(mMtvLeft, mVTopLeftCube);
                mVTopLeftCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mVTopMiddleCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(mMtvMiddle, mVTopMiddleCube);
                mVTopMiddleCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mVTopRightCube.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveViewWithAnim(mMtvRight, mVTopRightCube);
                mVTopRightCube.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void startGame() {
        int randomNumber = (int) (Math.random() * 10);
        mIpas.clear();

        if (randomNumber == 0) {
            mTvStatus.setText("apple");
            mIpas.add(new Ipa(1, "æ"));
            mIpas.add(new Ipa(2, "p"));
            mIpas.add(new Ipa(3, "l"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 1) {
            mTvStatus.setText("fly");
            mIpas.add(new Ipa(1, "f"));
            mIpas.add(new Ipa(2, "l"));
            mIpas.add(new Ipa(3, "aɪ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 2) {
            mTvStatus.setText("cry");
            mIpas.add(new Ipa(1, "k"));
            mIpas.add(new Ipa(2, "r"));
            mIpas.add(new Ipa(3, "aɪ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 3) {
            mTvStatus.setText("sky");
            mIpas.add(new Ipa(1, "s"));
            mIpas.add(new Ipa(2, "k"));
            mIpas.add(new Ipa(3, "aɪ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 4) {
            mTvStatus.setText("sign");
            mIpas.add(new Ipa(1, "s"));
            mIpas.add(new Ipa(2, "aɪ"));
            mIpas.add(new Ipa(3, "n"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 5) {
            mTvStatus.setText("away");
            mIpas.add(new Ipa(1, "ə"));
            mIpas.add(new Ipa(2, "w"));
            mIpas.add(new Ipa(3, "eɪ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 6) {
            mTvStatus.setText("okay");
            mIpas.add(new Ipa(1, "əu"));
            mIpas.add(new Ipa(2, "k"));
            mIpas.add(new Ipa(3, "eɪ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 7) {
            mTvStatus.setText("flow");
            mIpas.add(new Ipa(1, "f"));
            mIpas.add(new Ipa(2, "l"));
            mIpas.add(new Ipa(3, "əʊ"));
            Collections.shuffle(mIpas);
        } else if (randomNumber == 8) {
            mTvStatus.setText("crow");
            mIpas.add(new Ipa(1, "k"));
            mIpas.add(new Ipa(2, "r"));
            mIpas.add(new Ipa(3, "əʊ"));
            Collections.shuffle(mIpas);
        } else {
            mTvStatus.setText("allow");
            mIpas.add(new Ipa(1, "ə"));
            mIpas.add(new Ipa(2, "l"));
            mIpas.add(new Ipa(3, "aʊ"));
            Collections.shuffle(mIpas);
        }
        setCubeLeft(mIpas.get(0).getIpaContent());
        setCubeMiddle(mIpas.get(1).getIpaContent());
        setCubeRight(mIpas.get(2).getIpaContent());

        showAllCubes();
        init();

        mRivStart.setImageResource(R.drawable.ic_unplay);
        isGameStart = true;

        mTvTimer.setVisibility(View.VISIBLE);
        mTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 - 1 > 10) {
                    String time = "00:" + (millisUntilFinished / 1000);
                    mTvTimer.setText(time);
                } else {
                    String time = "00:0" + (millisUntilFinished / 1000);
                    mTvTimer.setText(time);
                }
            }
            @Override
            public void onFinish() {
                mTvTimer.setText(R.string.end_time);
                mRlGifContainer.setVisibility(View.VISIBLE);
                mGivTryAgain.setVisibility(View.VISIBLE);
                mActivity.updateStatusBar(true);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.updateStatusBar(false);
                        mGivTryAgain.setVisibility(View.GONE);
                        mRlGifContainer.setVisibility(View.GONE);
                        stopGame();
                        mHandler.removeCallbacksAndMessages(null);
                    }
                };
                mHandler.postDelayed(runnable, 2000);
            }
        }.start();
    }

    public void stopGame() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTvStatus.setText(R.string.ready);
        mTvTimer.setText(R.string.end_time);
        mRivStart.setImageResource(R.drawable.ic_play_game);
        hideAllCubes();

        isGameStart = false;
    }

    private void moveViewWithAnim(View currentView, View view) {
        float nextY = view.getY() + ((float) view.getHeight() - currentView.getHeight()) / 2;
        float nextX = view.getX() + ((float) view.getWidth() - currentView.getWidth()) / 2;
        ((MoveTextView) currentView).setCurrentInView(view);
        // 属性动画移动
        ObjectAnimator moveY = ObjectAnimator.ofFloat(currentView, "y", view.getY(), nextY);
        ObjectAnimator moveX = ObjectAnimator.ofFloat(currentView, "x", view.getX(), nextX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    @Override
    public void onTouchControl(float lastX, float lastY, View view) {
        Rectangle nowRectangle = new Rectangle(mWidth, lastX, lastY);

        int position = -1;
        float maxArea = 0;
        View currentViewLeft = mMtvLeft.getCurrentInView(); // Left Cube所在框的位置
        View currentViewMiddle = mMtvMiddle.getCurrentInView(); // Middle Cube所在框的位置
        View currentViewRight = mMtvRight.getCurrentInView(); // Right Cube所在框的位置

        ArrayList<View> rectangles; // 选择对应的可移动的集合
        if (view.getId() == R.id.mtv_left) {
            rectangles = mLeftRectangles;
        } else if (view.getId() == R.id.mtv_middle) {
            rectangles = mMiddleRectangles;
        } else {
            rectangles = mRightRectangles;
        }

        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle rectangle = new Rectangle(mBigWidth, rectangles.get(i).getX(), rectangles.get(i).getY());

            if (rectangle.calculatedArea(nowRectangle) > 0) {
                if ((position < 0) || maxArea < rectangle.getArea()) {
                    position = i;
                    maxArea = rectangle.getArea();
                }
            }
        }

        // 在所有框框的范围内，并且A和B所在的框不能重复
        if (position > -1) {
            View nextView = rectangles.get(position);
            if (nextView == currentViewLeft) {
                moveViewWithAnim(mMtvLeft, mVTopLeftCube);
            } else if (nextView == currentViewMiddle) {
                moveViewWithAnim(mMtvMiddle, mVTopMiddleCube);
            } else if (nextView == currentViewRight) {
                moveViewWithAnim(mMtvRight, mVTopRightCube);
            }

            moveViewWithAnim(view, nextView);

            if (view.getId() == R.id.mtv_left) {
                if (mIpas.get(0).getIpaId() == (position + 1)) {
                    mIsViewCorrect[position] = true;
                } else {
                    mIsViewCorrect[position] = false;
                }
            } else if (view.getId() == R.id.mtv_middle) {
                if (mIpas.get(1).getIpaId() == (position + 1)) {
                    mIsViewCorrect[position] = true;
                } else {
                    mIsViewCorrect[position] = false;
                }
            } else {
                if (mIpas.get(2).getIpaId() == (position + 1)) {
                    mIsViewCorrect[position] = true;
                } else {
                    mIsViewCorrect[position] = false;
                }
            }
        }
        else {
            if (view.getId() == R.id.mtv_left) {
                moveViewWithAnim(view, mVTopLeftCube);
                if (currentViewLeft == mVBottomLeftCube) {
                    mIsViewCorrect[0] = false;
                }
                else if (currentViewLeft == mVBottomMiddleCube) {
                    mIsViewCorrect[1] = false;
                }
                else if (currentViewLeft == mVBottomRightCube) {
                    mIsViewCorrect[2] = false;
                }
            } else if (view.getId() == R.id.mtv_middle) {
                moveViewWithAnim(view, mVTopMiddleCube);
            }
            else {
                moveViewWithAnim(view, mVTopRightCube);
            }
        }

        if(((mMtvLeft.getCurrentInView() == mVBottomLeftCube) || (mMtvLeft.getCurrentInView() == mVBottomMiddleCube) || (mMtvLeft.getCurrentInView() == mVBottomRightCube)) &&
                ((mMtvMiddle.getCurrentInView() == mVBottomLeftCube) || (mMtvMiddle.getCurrentInView() == mVBottomMiddleCube) || (mMtvMiddle.getCurrentInView() == mVBottomRightCube)) &&
                ((mMtvRight.getCurrentInView() == mVBottomLeftCube) || (mMtvRight.getCurrentInView() == mVBottomMiddleCube) || (mMtvRight.getCurrentInView() == mVBottomRightCube))) {

            boolean hasWrong = false;
            for (boolean x : mIsViewCorrect) {
                if (!x) {
                    hasWrong = true;
                    break;
                }
            }

            mTimer.cancel();
            mTimer = null;
            if (hasWrong) {
                // updateGameLeaderboard();
                mRlGifContainer.setVisibility(View.VISIBLE);
                mGivTryAgain.setVisibility(View.VISIBLE);
                mActivity.updateStatusBar(true);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.updateStatusBar(false);
                        mGivTryAgain.setVisibility(View.GONE);
                        mRlGifContainer.setVisibility(View.GONE);
                        score = 0;
                        mTvScore.setText(String.valueOf(score));
                        stopGame();
                        mHandler.removeCallbacksAndMessages(null);
                    }
                };
                mHandler.postDelayed(runnable, 2000);
            }
            else {
                mRlGifContainer.setVisibility(View.VISIBLE);
                mGivWellDone.setVisibility(View.VISIBLE);
                mActivity.updateStatusBar(true);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mActivity.updateStatusBar(false);
                        mGivWellDone.setVisibility(View.GONE);
                        mRlGifContainer.setVisibility(View.GONE);
                        score++;
                        mTvScore.setText(String.valueOf(score));
                        startGame();
                        mHandler.removeCallbacksAndMessages(null);
                    }
                };
                mHandler.postDelayed(runnable, 2000);
            }

            Arrays.fill(mIsViewCorrect, false);
        }
    }

    public void release() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}
