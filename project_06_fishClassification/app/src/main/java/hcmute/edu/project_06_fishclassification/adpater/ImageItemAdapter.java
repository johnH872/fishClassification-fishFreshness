package hcmute.edu.project_06_fishclassification.adpater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hcmute.edu.project_06_fishclassification.R;
import hcmute.edu.project_06_fishclassification.model.Image;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ImageAdapterViewHolder> {

    private List<Image> listImage;
    private Context mContext;

    public ImageItemAdapter(Context context, List<Image> listImage) {
        this.listImage = listImage;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ImageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_image_card, parent, false);
        return new ImageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapterViewHolder holder, int position) {
        Image image = listImage.get(position);
        if(image == null){
            return;
        }

        Glide.with(holder.fishImage.getContext())
                .load(image.getImageUrl())
                .placeholder(R.drawable.clown_fish)
                .error(R.drawable.clown_fish)
                .into(holder.fishImage);

        holder.fishName.setText(image.getName());
        holder.saveDate.setText(image.getSaveDate().toDate().toString());
    }

//    private void onClickGotoAlbum(Album item) {
//        Intent intent = new Intent(mContext, AlbumActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("object_album", item);
//        intent.putExtras(bundle);
//        mContext.startActivity(intent);
//    }

    @Override
    public int getItemCount() {
        if(listImage != null){
            return listImage.size();
        }
        return 0;
    }

    public class ImageAdapterViewHolder extends RecyclerView.ViewHolder{

        private ImageView fishImage;
        private TextView fishName;
        private TextView saveDate;

        public ImageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            saveDate = itemView.findViewById(R.id.tv_savedDate);
            fishImage = itemView.findViewById(R.id.fish_img);
            fishName = itemView.findViewById(R.id.tv_fishName);
        }
    }
}
