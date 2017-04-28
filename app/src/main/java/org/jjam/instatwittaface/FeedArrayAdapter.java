package org.jjam.instatwittaface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Anpassad adapter för att få in rader i en lista
 * @author Jerry Pedersen, Jonas Remgård, Anton Nilsson, Mårten Persson
 */
public class FeedArrayAdapter extends ArrayAdapter<Post> {

    private Context context;
    private List<Post> feeds;

    public FeedArrayAdapter(Context context, int textViewResourceId, List<Post> objects) {
        super(context, textViewResourceId, objects);
        this.feeds = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_view, parent, false);
        TextView postCompany = (TextView) rowView.findViewById(R.id.tvCompany);

        TextView userNameTextView = (TextView) rowView.findViewById(R.id.userNameTv);
        TextView descriptionTextView = (TextView) rowView.findViewById(R.id.smallDescription);
        ImageView profilePicVew = (ImageView) rowView.findViewById(R.id.icon);

        if(feeds.get(position).getCompany().equals("Twitter")){
            descriptionTextView.setText(feeds.get(position).getText());
            postCompany.setText("@" + feeds.get(position).getCompany());
            userNameTextView.setText("@" + feeds.get(position).getUserName());
            profilePicVew.setVisibility(View.INVISIBLE);
            postCompany.setTextColor(getContext().getResources().getColor(R.color.colortwitter));
        }
        else if(feeds.get(position).getCompany().equals("Instagram")){
            profilePicVew.setImageBitmap(feeds.get(position).getSmallImageBitmap());
            descriptionTextView.setText(feeds.get(position).getText());
            userNameTextView.setText(feeds.get(position).getUserName());
            postCompany.setText("@" + feeds.get(position).getCompany());
            postCompany.setTextColor(getContext().getResources().getColor(R.color.colorinstagram));
        }
        else if(feeds.get(position).getCompany().equals("Facebook")){
            descriptionTextView.setText(feeds.get(position).getText());
            postCompany.setText("@" + feeds.get(position).getCompany());
            profilePicVew.setVisibility(View.INVISIBLE);
            postCompany.setTextColor(getContext().getResources().getColor(R.color.colorfacebook));
        }
        return rowView;
    }
}
