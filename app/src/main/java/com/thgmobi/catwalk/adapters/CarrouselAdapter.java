package com.thgmobi.catwalk.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.models.Photo;

import java.net.URL;
import java.util.List;

public class CarrouselAdapter  extends PagerAdapter{

    List<Photo> lstPhotos;
    Context context;
    LayoutInflater layoutInflater;

    public CarrouselAdapter(List<Photo> lstPhotos, Context context) {
        this.lstPhotos = lstPhotos;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstPhotos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = layoutInflater.inflate(R.layout.card_item, container, false);
        ImageView imageView = view.findViewById(R.id.card_item_iv);
        TextView textView = view.findViewById(R.id.card_item_tv_temp);

        final Photo photo = lstPhotos.get(position);


        textView.setText(photo.getTemp());

        Picasso.get().load(photo.getUrlDownload())
                .into(imageView);
        container.addView(view);


        return view;
    }
}
