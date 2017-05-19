package tzcorp.redditchat;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by tony on 19/05/17.
 */

public class MessageAdapter extends ArrayAdapter<BasicMessage> {
    public MessageAdapter(Context context, int resource, List<BasicMessage> messages){
        super(context, resource, messages);
    }

}
