package tzcorp.snoochat.Activities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import tzcorp.snoochat.R;

/**
 * Created by tony on 05/06/17.
 */

public class MessageAdapter extends ArrayAdapter<BasicMessage> {
    public ArrayList<MessageAdapterListeners> listeners;

    public MessageAdapter(Context context, int resource, List<BasicMessage> messages){
        super(context, resource, messages);
        listeners = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.itemPhotoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.itemMessageTextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.itemNameTextView);

        BasicMessage message = getItem(position);
        //TODO: add photos later
        messageTextView.setVisibility((View.VISIBLE));
        photoImageView.setVisibility(View.GONE);
        messageTextView.setText(message.getText());


        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat date = new SimpleDateFormat("HH:mm");
        date.setTimeZone(tz);
        if (message.getUtcTime() != null) {
            String curTime = date.format(new Date(message.getUtcTime()));
            nameTextView.setText(message.getName()+ " " + curTime);
        } else {
            nameTextView.setText(message.getName());
        }



        return convertView;
    }

    public void addListeners(@NonNull MessageAdapterListeners listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListeners(@NonNull MessageAdapterListeners listener) {
        if (listeners.contains(listener)){
            listeners.remove(listener);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for(MessageAdapterListeners listener : listeners) {
            listener.messagesChanged();
        }
    }

    public interface MessageAdapterListeners{
        void messagesChanged();
    }
}
