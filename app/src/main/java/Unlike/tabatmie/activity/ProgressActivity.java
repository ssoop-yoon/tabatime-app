package Unlike.tabatmie.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;

import Unlike.tabatmie.Dto.ProgressDTO;
import Unlike.tabatmie.R;
import Unlike.tabatmie.adapter.ProgressAdapter;
import Unlike.tabatmie.dialog.TabatimeDialog;
import Unlike.tabatmie.util.Applications;
import Unlike.tabatmie.util.CommonUtil;
import Unlike.tabatmie.util.Preference;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgressActivity extends AppCompatActivity {

    private String TAG = this.getClass().toString();

    String getTitle, type;

    @BindView(R.id.btn_back)
    RelativeLayout btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.tv_cnt_title)
    TextView tv_cnt_title;
    @BindView(R.id.layer_cnt)
    RecyclerView layer_cnt;

    @BindView(R.id.btn_self)
    RelativeLayout btn_self;

    @BindView(R.id.btn_apply)
    TextView btn_apply;

    private TabatimeDialog tabatimeDialog;

    private ProgressAdapter progressAdapter;
    private ArrayList<ProgressDTO> progressList;
    private SnapHelper helper;
    private int snapPosision = RecyclerView.NO_POSITION;
    private int min, max;
    private int start, center, center_data = 0;
    private int selectCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        ButterKnife.bind(this);

        init();
    }

    public void init() {
        try {
            getTitle = getIntent().getStringExtra("title");
            type = getIntent().getStringExtra("dialog_type");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getTitle.equals("exercise")) {
            tv_title.setText(getResources().getString(R.string.title_exercise));
            center_data = Applications.preference.getValue(Preference.EXERCISE, CommonUtil.D_EXERCISE);
        } else if (getTitle.equals("rest")) {
            tv_title.setText(getResources().getString(R.string.title_rest));
            center_data = Applications.preference.getValue(Preference.REST, CommonUtil.D_REST);
        } else if (getTitle.equals("set")) {
            tv_title.setText(getResources().getString(R.string.title_set));
            center_data = Applications.preference.getValue(Preference.SET, CommonUtil.D_SET);
        } else if (getTitle.equals("round")) {
            tv_title.setText(getResources().getString(R.string.title_round));
            center_data = Applications.preference.getValue(Preference.ROUND, CommonUtil.D_ROUND);
        } else if (getTitle.equals("round_reset")) {
            tv_title.setText(getResources().getString(R.string.title_round_reset));
            center_data = Applications.preference.getValue(Preference.ROUND_RESET, CommonUtil.D_ROUND_RESET);
        }

        if (type.equals("1")) {
            tv_cnt_title.setText(getResources().getString(R.string.desc_sec));
            min = CommonUtil.D_SEC_MIN;
            max = CommonUtil.D_SEC_MAX;
        } else {
            tv_cnt_title.setText(getResources().getString(R.string.desc_cnt));
            min = CommonUtil.D_COUNT_MIN;
            max = CommonUtil.D_COUNT_MAX;
        }

        if (center_data != 0) {
            start = center_data;
            if (type.equals("1")) {
                center = center_data + 1;
            } else {
                center = center_data + 5;
            }
        } else {
            start = min;
            center = 6;
        }

        setCntList(start);

        progressAdapter = new ProgressAdapter(progressList);
        layer_cnt.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        layer_cnt.setAdapter(progressAdapter);
        layer_cnt.post(new Runnable() {
            @Override
            public void run() {
                selectCnt = min;
                scrollTo(center, false);
            }
        });

        helper = new LinearSnapHelper();
        helper.attachToRecyclerView(layer_cnt);
        layer_cnt.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    try {
                        View centerView = helper.findSnapView(recyclerView.getLayoutManager());
                        int pos = recyclerView.getLayoutManager().getPosition(centerView);
                        boolean changed = pos != snapPosision;
                        if (changed) {
                            scrollTo(pos, true);
                            selectCnt = progressList.get(pos).getCnt();
                            snapPosision = pos;
                            if (type.equals("1")) {
                                setCntList(pos - 1);
                            } else {
                                setCntList(pos - 5);
                            }
                            progressAdapter.notifyDataSetChanged();
                        }

                        if (pos <= 5) {
                            scrollTo(6, true);
                        }
                        if (pos >= (progressList.size() - 6)) {
                            scrollTo(progressList.size() - 7, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean scrollTo(int position, boolean smooth) {
        Log.e(TAG, "scrollTo : " + position);
        if (layer_cnt.getLayoutManager() != null) {
            if (smooth) {
                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(layer_cnt.getContext()) {
                    @Override
                    protected void onTargetFound(View targetView, RecyclerView.State state, RecyclerView.SmoothScroller.Action action) {
                        if (layer_cnt == null || layer_cnt.getLayoutManager() == null) {
                            return;
                        }
                        int[] snapDistances = helper.calculateDistanceToFinalSnap(layer_cnt.getLayoutManager(), targetView);
                        final int dx = snapDistances[0];
                        final int dy = snapDistances[1];
                        final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                        if (time > 0) {
                            action.update(dx, dy, time, mDecelerateInterpolator);
                        }
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        float scrollMsPerInch = 100f;
                        return scrollMsPerInch / displayMetrics.densityDpi;
                    }
                };
                if (smoothScroller != null) {
                    smoothScroller.setTargetPosition(position);
                    layer_cnt.getLayoutManager().startSmoothScroll(smoothScroller);
                    return true;
                }
            } else {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layer_cnt.getLayoutManager();
                if (linearLayoutManager != null) {
                    linearLayoutManager.scrollToPositionWithOffset(position, 0);
                }

                layer_cnt.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        RecyclerView.ViewHolder viewHolder = layer_cnt.findViewHolderForAdapterPosition(position);
                        if (viewHolder != null) {
                            int[] distances = helper.calculateDistanceToFinalSnap(layer_cnt.getLayoutManager(), viewHolder.itemView);
                            Log.e(TAG, "scroll) " + distances[0] + ":" + distances[1]);
                            layer_cnt.scrollBy(distances[0], distances[1]);

                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    public void setCntList(int center_cnt) {
        Log.e(TAG, "center_cnt : " + center_cnt);
        if (progressList == null) {
            progressList = new ArrayList<>();
        }
        progressList.clear();
        for (int i = min; i <= max; i++) {
            if (i == min) {
                for (int j = 0; j < 6; j++) {
                    progressList.add(new ProgressDTO(0, false));
                }
            }
            if (i == center_cnt) {
                progressList.add(new ProgressDTO(i, true));
            } else {
                progressList.add(new ProgressDTO(i, false));
            }
            if (i == max) {
                for (int j = 0; j < 6; j++) {
                    progressList.add(new ProgressDTO(0, false));
                }
            }
        }
    }

    @OnClick({R.id.btn_back, R.id.btn_self, R.id.btn_apply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_self:
                int dialog_type = Integer.parseInt(type);
                tabatimeDialog = new TabatimeDialog(this);
                if (!tabatimeDialog.isShowing()) {
                    tabatimeDialog.setDialogType(dialog_type);
                    if (dialog_type == 1) {
                        tabatimeDialog.setTitle(getResources().getString(R.string.dialog_title_time));
                        tabatimeDialog.setEditInfo(getResources().getString(R.string.time_limit));
                    } else {
                        tabatimeDialog.setTitle(getResources().getString(R.string.dialog_title_cnt));
                        tabatimeDialog.setEditInfo(getResources().getString(R.string.cnt_limit));
                    }
                    tabatimeDialog.closeClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tabatimeDialog.dismiss();
                        }
                    });
                    tabatimeDialog.applyClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int result = 0;
                            if (dialog_type == 1) {
                                int min = tabatimeDialog.getEtMin();
                                int sec = tabatimeDialog.getEtSec();
                                int exercise;
                                if (min == 0) {
                                    exercise = sec;
                                } else {
                                    exercise = (min * 60) + sec;
                                }
                                result = exercise;
                            } else if (dialog_type == 2) {
                                result = tabatimeDialog.getEtCnt();
                            }
                            if (result != 0) {
                                setData(result);
                            }
                            tabatimeDialog.dismiss();
                            onBackPressed();
                        }
                    });
                    tabatimeDialog.setCancelable(false);
                    tabatimeDialog.show();
                }
                break;
            case R.id.btn_apply:
                setData(selectCnt);
                onBackPressed();
                break;
        }
    }

    public void setData(int result) {
        if (getTitle.equals("exercise")) {
            Applications.preference.put(Preference.EXERCISE, result);
        } else if (getTitle.equals("rest")) {
            Applications.preference.put(Preference.REST, result);
        } else if (getTitle.equals("set")) {
            Applications.preference.put(Preference.SET, result);
        } else if (getTitle.equals("round")) {
            Applications.preference.put(Preference.ROUND, result);
        } else if (getTitle.equals("round_reset")) {
            Applications.preference.put(Preference.ROUND_RESET, result);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (tabatimeDialog != null && tabatimeDialog.isShowing()) {
                tabatimeDialog.dismiss();
                tabatimeDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}