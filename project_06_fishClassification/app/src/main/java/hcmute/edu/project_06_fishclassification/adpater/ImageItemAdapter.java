package hcmute.edu.project_06_fishclassification.adpater;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import hcmute.edu.project_06_fishclassification.R;
import hcmute.edu.project_06_fishclassification.model.Image;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ImageAdapterViewHolder> {

    private List<Image> listImage;
    private List<String> docIDs;
    private List<String> imgUrl;
    private Context mContext;

    public ImageItemAdapter(Context context, List<Image> listImage, List<String> docIDs, List<String> imgUrl) {
        this.listImage = listImage;
        this.mContext = context;
        this.docIDs = docIDs;
        this.imgUrl = imgUrl;
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
        holder.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String docID = docIDs.get(position);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference ref = db.collection("saved_images").document(docID);
                ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext.getApplicationContext(), "Success delete document", Toast.LENGTH_LONG).show();
                            docIDs.remove(position);
                            listImage.remove(position);
                            changeCollectionCount();
                            deleteImgStorage(imgUrl.get(position));
                            notifyDataSetChanged();
                        }else {
                            Toast.makeText(mContext.getApplicationContext(), "Fail delete document", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    void changeCollectionCount(){
        Intent intent = new Intent();
        intent.setAction("_count");

        LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);
    }

    void deleteImgStorage(String url){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Picture","#deleted");
            }
        });
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
        private MaterialButton btn_del;

        public ImageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            saveDate = itemView.findViewById(R.id.tv_savedDate);
            fishImage = itemView.findViewById(R.id.fish_img);
            fishName = itemView.findViewById(R.id.tv_fishName);
            btn_del = itemView.findViewById(R.id.btn_del);
        }
    }
}
