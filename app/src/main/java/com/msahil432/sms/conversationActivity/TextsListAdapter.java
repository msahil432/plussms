package com.msahil432.sms.conversationActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.msahil432.sms.R;
import com.msahil432.sms.common.Event;
import com.msahil432.sms.database.SMS;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class TextsListAdapter extends RecyclerView.Adapter<TextsListAdapter.TextHolder> {

  private ArrayList<Text> smsList;
  private int totalSize;

  public TextsListAdapter(ArrayList<Text> list, int totalSize) {
    smsList = list;
    this.totalSize = totalSize;
  }

  @NonNull
  @Override
  public TextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_text, parent, false);
    return new TextHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull TextHolder holder, int position) {
    if(position>smsList.size()*2/3)
      if(smsList.size()<totalSize)
        EventBus.getDefault().post(Event.LOAD_MORE_TEXTS);

    holder.bind(smsList.get(position));
  }

  @Override
  public int getItemCount() {
    return smsList.size();
  }

  public void setList(ArrayList<Text> newList){
    smsList = newList;
    notifyDataSetChanged();
  }

  public void setTotalSize(int totalSize) {
    this.totalSize = totalSize;
  }

  public class TextHolder extends RecyclerView.ViewHolder{

    AppCompatTextView body;

    public TextHolder(@NonNull View itemView) {
      super(itemView);
      body = itemView.findViewById(R.id.text_body);
    }

    public void bind(Text t){
      body.setText(t.body);
    }
  }

}
