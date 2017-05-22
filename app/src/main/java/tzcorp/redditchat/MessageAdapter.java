package tzcorp.redditchat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tony on 19/05/17.
 */

public class MessageAdapter extends ArrayAdapter<BasicMessage> {
    public MessageAdapter(Context context, int resource, List<BasicMessage> messages){
        super(context, resource, messages);
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

        nameTextView.setText(message.getName());
        return convertView;
    }
}
