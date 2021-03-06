package Unlike.tabatmie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Unlike.tabatmie.Dto.RecordDTO;
import Unlike.tabatmie.R;
import Unlike.tabatmie.util.CommonUtil;

public class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<RecordDTO> recordList;

    private RecordAdapter.ItemClick itemClick;

    public interface ItemClick {
        void onItemClick(View view, int i);
    }

    public void setOnItemClick(RecordAdapter.ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public RecordAdapter(Context context, ArrayList<RecordDTO> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecordViewHolder recordViewHolder = (RecordViewHolder) holder;
        if (!recordList.isEmpty()) {
            recordViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClick != null) {
                        itemClick.onItemClick(v, position);
                    }
                }
            });

            String date = recordList.get(position).getRegDate();
            String current_date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
            if (date.equals(current_date)) {
                recordViewHolder.tv_date.setText(context.getResources().getString(R.string.today));
            } else {
                recordViewHolder.tv_date.setText(date);
            }
            recordViewHolder.tv_exercise_time.setText(CommonUtil.getTime(recordList.get(position).getExercise_time()));
            recordViewHolder.tv_exercise.setText(String.valueOf(recordList.get(position).getExercise()));
            recordViewHolder.tv_rest.setText(String.valueOf(recordList.get(position).getRest()));
            recordViewHolder.tv_set.setText(String.valueOf(recordList.get(position).getSet()));
            recordViewHolder.tv_round.setText(String.valueOf(recordList.get(position).getRound()));
        }
    }

    @Override
    public int getItemCount() {
        return recordList != null ? recordList.size() : 0;
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_checked;
        CheckBox btn_checked;
        LinearLayout layer_record;
        TextView tv_date, tv_exercise_time, tv_exercise, tv_rest, tv_set, tv_round;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_checked = (ImageView) itemView.findViewById(R.id.iv_checked);
            btn_checked = (CheckBox) itemView.findViewById(R.id.btn_checked);
            layer_record = (LinearLayout) itemView.findViewById(R.id.layer_record);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_exercise_time = (TextView) itemView.findViewById(R.id.tv_exercise_time);
            tv_exercise = (TextView) itemView.findViewById(R.id.tv_exercise);
            tv_rest = (TextView) itemView.findViewById(R.id.tv_rest);
            tv_set = (TextView) itemView.findViewById(R.id.tv_set);
            tv_round = (TextView) itemView.findViewById(R.id.tv_round);
        }
    }
}
